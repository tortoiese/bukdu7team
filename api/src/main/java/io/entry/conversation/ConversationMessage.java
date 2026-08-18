package io.entry.conversation;

import io.entry.common.CharacterId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/** Conversation.messages 컬렉션의 값 타입. 캐릭터 발화 또는 고객 발화 1건. */
@Embeddable
public class ConversationMessage {

    public enum Role { CHARACTER, USER }

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private CharacterId character;

    private String text;

    protected ConversationMessage() {
    }

    public ConversationMessage(Role role, CharacterId character, String text) {
        this.role = role;
        this.character = character;
        this.text = text;
    }

    public Role getRole() {
        return role;
    }

    public CharacterId getCharacter() {
        return character;
    }

    public String getText() {
        return text;
    }
}
