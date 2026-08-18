package io.entry.conversation;

import io.entry.common.CharacterId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/** Conversation.messages 컬렉션의 값 타입. 캐릭터 발화 또는 고객 발화 1건. */
@Embeddable
public class ConversationMessage {

    public enum Role { CHARACTER, USER }

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    // 컬럼명 character는 H2/ANSI SQL 예약 타입명(CHARACTER)과 충돌해 INSERT 시 SQL 오류를 일으킨다.
    @Enumerated(EnumType.STRING)
    @Column(name = "character_id")
    private CharacterId characterId;

    private String text;

    protected ConversationMessage() {
    }

    public ConversationMessage(Role role, CharacterId characterId, String text) {
        this.role = role;
        this.characterId = characterId;
        this.text = text;
    }

    public Role getRole() {
        return role;
    }

    public CharacterId getCharacter() {
        return characterId;
    }

    public String getText() {
        return text;
    }
}
