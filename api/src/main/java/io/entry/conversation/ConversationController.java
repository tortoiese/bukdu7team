package io.entry.conversation;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.conversation.dto.ConversationMessageRequest;
import io.entry.conversation.dto.ConversationReplyData;
import io.entry.conversation.dto.ConversationStartData;
import io.entry.conversation.dto.ConversationStartRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ApiResponse<ConversationStartData> start(@Valid @RequestBody ConversationStartRequest request) {
        ConversationStartData data = conversationService.start(sessionId(), request.scanId());
        return ApiResponse.of(data, ApiMeta.basic());
    }

    @PostMapping("/{conversationId}/messages")
    public ApiResponse<ConversationReplyData> reply(@PathVariable UUID conversationId,
                                                     @Valid @RequestBody ConversationMessageRequest request) {
        ConversationService.ReplyResult result = conversationService.reply(sessionId(), conversationId, request.text());
        return ApiResponse.of(result.data(), ApiMeta.ai(result.aiUsed(), result.fallback()));
    }

    private UUID sessionId() {
        return UUID.fromString(RequestContext.sessionId());
    }
}
