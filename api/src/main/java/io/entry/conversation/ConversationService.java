package io.entry.conversation;

import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.EntryException;
import io.entry.conversation.dto.ConversationStartResponse;
import io.entry.conversation.dto.SendMessageResponse;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * P6 대화(최대 3턴). 판매하지 않고 결정 기준 정리에 한정한다. 대화를 시작하지 않아도
 * P1의 정보는 그대로 남는다 — 대화는 관문이 아니라 선택지다(PROMPTS.md #9).
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ScanEventRepository scanEventRepository;
    private final ProductCatalog productCatalog;
    private final AiConversationService aiConversationService;

    public ConversationService(ConversationRepository conversationRepository, ScanEventRepository scanEventRepository,
                                ProductCatalog productCatalog, AiConversationService aiConversationService) {
        this.conversationRepository = conversationRepository;
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.aiConversationService = aiConversationService;
    }

    @Transactional
    public ConversationStartResponse start(UUID sessionId, String scanId) {
        ScanEvent scan = scanEventRepository.findById(UUID.fromString(scanId))
                .filter(s -> s.getSessionId().equals(sessionId))
                .orElseThrow(() -> EntryException.notFound("SCAN_NOT_FOUND", "스캔 기록을 찾을 수 없습니다."));

        Conversation conversation = new Conversation(sessionId, scanId, scan.getProductId(), Instant.now());
        conversationRepository.save(conversation);

        return new ConversationStartResponse(conversation.getId().toString(), conversation.turnsRemaining(), List.of());
    }

    @Transactional
    public SendMessageResponse sendMessage(UUID sessionId, String conversationId, String text) {
        Conversation conversation = conversationRepository.findByIdAndSessionId(UUID.fromString(conversationId), sessionId)
                .orElseThrow(() -> EntryException.notFound("CONVERSATION_NOT_FOUND", "대화를 찾을 수 없습니다."));

        if (conversation.turnsExhausted()) {
            return new SendMessageResponse(null, 0, new SendMessageResponse.Extracted(List.of(), null), true);
        }

        conversation.addMessage(new ConversationMessage(ConversationMessage.Role.USER, null, text));
        conversation.useTurn();

        Product product = productCatalog.get(conversation.getProductId());
        AiConversationService.Result result = aiConversationService.reply(
                product, conversation.getMessages(), text, conversation.getTurnsUsed());

        conversation.addMessage(new ConversationMessage(ConversationMessage.Role.CHARACTER, result.character(), result.message()));
        conversationRepository.save(conversation);

        boolean handoffSuggested = conversation.turnsExhausted();
        return new SendMessageResponse(
                new SendMessageResponse.Reply(result.character(), result.message()),
                conversation.turnsRemaining(),
                new SendMessageResponse.Extracted(result.criteria(), result.unresolved()),
                handoffSuggested
        );
    }
}
