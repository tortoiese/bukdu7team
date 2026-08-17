package io.entry.intent;

public record IntentSignal(
        IntentStage stage,
        String comparisonAxis,
        UnresolvedCode unresolved,
        double confidence,
        String rationale,
        boolean aiUsed
) {
}
