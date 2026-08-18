package io.entry.persona;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.intent.UnresolvedCode;
import io.entry.persona.dto.SimulateRequest;
import io.entry.persona.dto.SimulationResultData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * D2 페르소나봇 시뮬레이션. disclaimer는 항상 포함하고 화면에서 숨기지 않는다(API_CONTRACT.md 11장).
 */
@Service
public class PersonaSimulationService {

    private static final String DISCLAIMER = "페르소나봇 응답은 실제 고객 행동이 아니라 대중 인식의 평균치입니다. 방향 판단에만 사용합니다.";

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final PersonaCatalog personaCatalog;
    private final ProductCatalog productCatalog;

    public PersonaSimulationService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper,
                                     PersonaCatalog personaCatalog, ProductCatalog productCatalog) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
        this.personaCatalog = personaCatalog;
        this.productCatalog = productCatalog;
    }

    public SimulationResultData simulate(String personaId, SimulateRequest request) {
        Persona persona = personaCatalog.get(personaId);
        Product product = productCatalog.get(request.productId());

        List<SimulationResultData.VariantResult> results = AiCallExecutor.callWithFallback(
                () -> aiClient.complete("persona-simulation", buildPrompt(persona, product, request)),
                raw -> parse(raw, request),
                () -> fallback(request)
        );

        return new SimulationResultData(UUID.randomUUID().toString(), results, DISCLAIMER);
    }

    private String buildPrompt(Persona persona, Product product, SimulateRequest request) {
        return promptLoader.load("persona-simulation")
                .replace("{{personaName}}", persona.name())
                .replace("{{personaDescription}}", persona.description())
                .replace("{{hypothesis}}", request.hypothesis())
                .replace("{{productDisplayName}}", product.displayName())
                .replace("{{productLine}}", product.line())
                .replace("{{productMaterial}}", product.material())
                .replace("{{productWeight}}", String.valueOf(product.weightGram()))
                .replace("{{variantA}}", request.variantA())
                .replace("{{variantB}}", request.variantB());
    }

    private List<SimulationResultData.VariantResult> parse(String raw, SimulateRequest request) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            List<SimulationResultData.VariantResult> results = new java.util.ArrayList<>();
            for (JsonNode item : node.path("results")) {
                results.add(new SimulationResultData.VariantResult(
                        item.get("variant").asText(),
                        item.get("saved").asBoolean(),
                        item.get("reason").asText(),
                        UnresolvedCode.valueOf(item.path("unresolved").asText("UNKNOWN"))));
            }
            if (results.isEmpty()) throw new IllegalStateException("결과가 비어 있습니다");
            return results;
        } catch (Exception e) {
            throw new AiUnavailableException("persona-simulation 응답 파싱 실패", e);
        }
    }

    private List<SimulationResultData.VariantResult> fallback(SimulateRequest request) {
        return List.of(
                new SimulationResultData.VariantResult(request.variantA(), true, "정보가 더 상세한 변형에 더 오래 머무는 경향을 기본값으로 가정합니다.", UnresolvedCode.UNKNOWN),
                new SimulationResultData.VariantResult(request.variantB(), false, "AI 응답을 받지 못해 규칙 기반 기본값을 사용했습니다.", UnresolvedCode.UNKNOWN)
        );
    }
}
