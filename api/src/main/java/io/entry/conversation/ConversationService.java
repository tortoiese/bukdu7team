package io.entry.conversation;

import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.CharacterId;
import io.entry.common.AppLocale;
import io.entry.common.EntryException;
import io.entry.conversation.ai.AiConversationContext;
import io.entry.conversation.ai.AiConversationReply;
import io.entry.conversation.ai.ConversationAiService;
import io.entry.conversation.dto.ConversationReplyData;
import io.entry.conversation.dto.ConversationStartData;
import io.entry.intent.UnresolvedCode;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import io.entry.session.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private static final int MAX_TURNS = 3;
    private static final CharacterId[] CHARACTERS = CharacterId.values();

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final ScanEventRepository scanEventRepository;
    private final ProductCatalog productCatalog;
    private final ConversationAiService aiService;
    private final SessionRepository sessionRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationMessageRepository messageRepository,
                               ScanEventRepository scanEventRepository,
                               ProductCatalog productCatalog,
                               ConversationAiService aiService,
                               SessionRepository sessionRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.aiService = aiService;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public ConversationStartData start(UUID sessionId, UUID scanId) {
        ScanEvent scan = scanEventRepository.findLockedById(scanId)
                .filter(event -> event.getSessionId().equals(sessionId))
                .orElseThrow(() -> EntryException.notFound("SCAN_NOT_FOUND", "스캔 기록을 찾을 수 없습니다."));

        Conversation conversation = conversationRepository.findBySessionIdAndScanId(sessionId, scanId)
                .orElseGet(() -> createConversation(sessionId, scan));
        return startData(conversation);
    }

    @Transactional
    public ReplyResult reply(UUID sessionId, UUID conversationId, String text) {
        Conversation conversation = conversationRepository.findLockedByIdAndSessionId(conversationId, sessionId)
                .orElseThrow(() -> EntryException.notFound("CONVERSATION_NOT_FOUND", "대화를 찾을 수 없습니다."));

        if (conversation.getTurnCount() >= MAX_TURNS) {
            return handoff(conversation);
        }

        Instant now = Instant.now();
        List<AiConversationContext.HistoryMessage> history = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                .skip(1)
                .map(message -> new AiConversationContext.HistoryMessage(message.getRole(), message.getText()))
                .toList();
        messageRepository.save(ConversationMessage.user(conversation.getId(), text, now));
        Product product = productCatalog.get(conversation.getProductId());
        AppLocale locale = sessionLocale(sessionId);
        AiConversationReply aiReply = aiService.reply(
                new AiConversationContext(product, conversation.getCharacter(), locale, history, text));
        conversation.increaseTurn(now);
        conversationRepository.save(conversation);
        messageRepository.save(ConversationMessage.character(conversation.getId(), aiReply.message(), now.plusMillis(1)));

        int remaining = MAX_TURNS - conversation.getTurnCount();
        ConversationReplyData data = new ConversationReplyData(
                new ConversationReplyData.Reply(conversation.getCharacter(), aiReply.message()),
                remaining,
                new ConversationReplyData.Extracted(aiReply.criteria(), aiReply.unresolved()),
                remaining == 0
        );
        return new ReplyResult(data, aiReply.aiUsed(), aiReply.fallback());
    }

    private Conversation createConversation(UUID sessionId, ScanEvent scan) {
        Product product = productCatalog.get(scan.getProductId());
        CharacterId character = CHARACTERS[Math.floorMod(product.productId().hashCode(), CHARACTERS.length)];
        Instant now = Instant.now();
        Conversation saved = conversationRepository.save(new Conversation(sessionId, scan.getId(), product.productId(), character, now));
        String greeting = switch (sessionLocale(sessionId)) {
            case EN -> "What is your main consideration about %s?".formatted(product.displayName());
            case JA -> "%sについて、最も気になる点は何ですか？".formatted(product.displayName());
            case ZH_HANT -> "關於%s，您最在意哪一點？".formatted(product.displayName());
            case KO -> "%s에 대해 어떤 점이 가장 고민되시나요?".formatted(product.displayName());
        };
        messageRepository.save(ConversationMessage.character(saved.getId(), greeting, now));
        return saved;
    }

    private ConversationStartData startData(Conversation conversation) {
        List<ConversationStartData.Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                .map(message -> new ConversationStartData.Message(
                        message.getRole(),
                        "CHARACTER".equals(message.getRole()) ? conversation.getCharacter() : null,
                        message.getText()))
                .toList();
        return new ConversationStartData(
                conversation.getId(),
                Math.max(0, MAX_TURNS - conversation.getTurnCount()),
                messages
        );
    }

    private ReplyResult handoff(Conversation conversation) {
        String message = switch (sessionLocale(conversation.getSessionId())) {
            case EN -> "You have used all three turns. Show this conversation to a store advisor if you would like to continue in person.";
            case JA -> "3回の対話が終了しました。続けて相談したい場合は、この対話を店頭スタッフにお見せください。";
            case ZH_HANT -> "三次對話已完成。若想繼續諮詢，可將這段對話給店內顧問查看。";
            case KO -> "대화 횟수를 모두 사용했어요. 원하시면 매장 직원에게 지금까지의 고민을 보여주고 이어서 상담할 수 있습니다.";
        };
        ConversationReplyData data = new ConversationReplyData(
                new ConversationReplyData.Reply(conversation.getCharacter(), message),
                0,
                new ConversationReplyData.Extracted(List.of(), UnresolvedCode.UNKNOWN),
                true
        );
        return new ReplyResult(data, false, false);
    }

    private AppLocale sessionLocale(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> session.getLocale())
                .orElse(AppLocale.KO);
    }

    public record ReplyResult(ConversationReplyData data, boolean aiUsed, boolean fallback) {
    }
}
