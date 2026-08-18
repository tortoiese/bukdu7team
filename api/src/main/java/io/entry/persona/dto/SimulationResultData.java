package io.entry.persona.dto;

import io.entry.intent.UnresolvedCode;

import java.util.List;

public record SimulationResultData(String runId, List<VariantResult> results, String disclaimer) {
    public record VariantResult(String variant, boolean saved, String reason, UnresolvedCode unresolved) {
    }
}
