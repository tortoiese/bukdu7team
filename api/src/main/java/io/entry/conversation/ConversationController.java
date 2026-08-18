package io.entry.conversation;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.conversation.dto.ConversationStartRequest;
import io.entry.conversation.dto.ConversationStartResponse;
import io.entry.conversation.dto.SendMessageRequest;
import io.entry.conversation.dto.SendMessageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ApiResponse<ConversationStartResponse> start(@Valid @RequestBody ConversationStartRequest request) {
        var data = conversationService.start(sessionId(), request.scanId());
        return ApiResponse.of(data, ApiMeta.basic());
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<SendMessageResponse> sendMessage(@PathVariable String id, @Valid @RequestBody SendMessageRequest request) {
        SendMessageResponse data = conversationService.sendMessage(sessionId(), id, request.text());
        return ApiResponse.of(data, ApiMeta.basic());
    }

    private UUID sessionId() {
        return UUID.fromString(RequestContext.sessionId());
    }
}
