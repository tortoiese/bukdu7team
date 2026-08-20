package io.entry.recap.dto;

/** emailSent는 channel이 EMAIL이 아니거나 발송에 실패해도 항상 false로 채워진다 — 발송 여부와 무관하게 linked는 그대로 성공. */
public record LinkAccountResponse(boolean linked, boolean emailSent) {
}
