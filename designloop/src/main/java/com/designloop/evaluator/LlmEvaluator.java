package com.designloop.evaluator;

import com.designloop.dto.LlmFeedbackDto;
import com.designloop.model.Problem;
import com.designloop.model.Submission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Uses an LLM to judge the parts of a design that plain code cannot
 * judge: is responsibility well separated? Is coupling low? Does the
 * design follow SOLID principles? etc.
 *
 * We force the AI to answer in strict JSON (see the prompt below) and
 * parse it into LlmFeedbackDto. If the AI is unreachable, times out,
 * or returns something we can't parse, we catch that here and return
 * an "unavailable" result instead of crashing the app or losing the
 * learner's submission.
 *
 * This calls Groq's OpenAI-compatible chat completions endpoint
 * (https://api.groq.com/openai/v1/chat/completions) rather than a
 * provider-specific SDK, so it's just a plain HTTP POST with a
 * "Bearer" API key and an OpenAI-shaped request/response body.
 */
@Component
public class LlmEvaluator implements Evaluator {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-120b}")
    private String model;

    private static final int MAX_ATTEMPTS = 2;

    @Override
    public EvaluationResult evaluate(Problem problem, Submission submission) {
        EvaluationResult result = new EvaluationResult();

        if (apiKey == null || apiKey.isBlank()) {
            result.setAvailable(false);
            result.getProblems().add("AI evaluation was skipped because no API key is configured. Only the code-based checks below were run.");
            return result;
        }

        String prompt = buildPrompt(problem, submission);
        Exception lastError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                LlmFeedbackDto dto = callGroqAndParse(prompt);

                if (isGarbled(dto)) {
                    // Groq's gpt-oss models occasionally return a response
                    // with stray control characters splattered through the
                    // text (a known, intermittent issue on their side, not
                    // ours). Rather than show broken text to the learner,
                    // retry once for a clean response before giving up.
                    if (attempt < MAX_ATTEMPTS) {
                        continue;
                    }
                    throw new IllegalStateException("AI response looked garbled after retrying");
                }

                result.setScore(clamp(dto.getScore()));
                result.setStrengths(sanitizeList(dto.getStrengths()));
                result.setProblems(sanitizeList(dto.getProblems()));
                result.setSuggestions(sanitizeList(dto.getSuggestions()));
                result.setTradeoffsNote(sanitize(dto.getTradeoffs()));
                return result;

            } catch (Exception e) {
                lastError = e;
            }
        }

        // Every attempt failed (network error, bad JSON, garbled text, etc.)
        // -> we degrade gracefully instead of failing the whole request.
        result.setAvailable(false);
        result.getProblems().add("AI evaluation failed and was skipped this time (" + lastError.getClass().getSimpleName() + "). Only the code-based checks below were run. You can retry evaluation later.");
        return result;
    }

    private LlmFeedbackDto callGroqAndParse(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 1024,
                "temperature", 0.3,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions", request, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        String text = root.path("choices").get(0).path("message").path("content").asText();

        // The model sometimes wraps JSON in ```json fences even when asked
        // not to. Strip those before parsing.
        String cleaned = text.replace("```json", "").replace("```", "").trim();

        return objectMapper.readValue(cleaned, LlmFeedbackDto.class);
    }

    /**
     * Well-formed feedback items are single sentences/phrases with no
     * embedded line breaks. If a Groq gpt-oss response comes back with
     * stray '\n'/'\r' characters scattered inside a strength/problem/
     * suggestion/tradeoffs string, that's a sign of the corrupted-output
     * issue rather than a real design comment, so we flag it for retry.
     */
    private boolean isGarbled(LlmFeedbackDto dto) {
        return containsControlChars(dto.getTradeoffs())
                || (dto.getStrengths() != null && dto.getStrengths().stream().anyMatch(this::containsControlChars))
                || (dto.getProblems() != null && dto.getProblems().stream().anyMatch(this::containsControlChars))
                || (dto.getSuggestions() != null && dto.getSuggestions().stream().anyMatch(this::containsControlChars));
    }

    private boolean containsControlChars(String s) {
        return s != null && (s.contains("\n") || s.contains("\r"));
    }

    private List<String> sanitizeList(List<String> items) {
        if (items == null) return new ArrayList<>();
        List<String> cleaned = new ArrayList<>();
        for (String item : items) {
            String s = sanitize(item);
            if (!s.isBlank()) cleaned.add(s);
        }
        return cleaned;
    }

    private String sanitize(String s) {
        if (s == null) return "";
        // Collapse any stray line breaks / repeated whitespace so a single
        // feedback item never gets rendered as multiple bullet fragments.
        return s.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private int clamp(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }

    private String buildPrompt(Problem problem, Submission submission) {
        return """
                You are an expert software design reviewer helping a learner practice
                Low-Level Design (LLD).

                PROBLEM: %s
                DESCRIPTION: %s
                REQUIREMENTS: %s

                LEARNER'S SUBMISSION:
                Classes and responsibilities:
                %s

                Interfaces:
                %s

                Relationships:
                %s

                Design explanation:
                %s

                Trade-offs mentioned by learner:
                %s

                Evaluate this design for: separation of responsibilities, coupling,
                cohesion, SOLID principles, extensibility, abstraction, use of design
                patterns where appropriate, and clarity.

                Reply with ONLY valid JSON, no other text, no markdown fences, in
                exactly this shape:
                {
                  "score": <integer 0-100>,
                  "strengths": ["...", "..."],
                  "problems": ["specific, explain WHY it is a problem, e.g. 'ParkingLot is responsible for parking vehicles, calculating fees and processing payments - consider separating these responsibilities.'"],
                  "suggestions": ["concrete, actionable suggestions"],
                  "tradeoffs": "one short paragraph noting any reasonable trade-offs in this design"
                }
                """.formatted(
                problem.getTitle(),
                problem.getDescription(),
                problem.getRequirements(),
                nullSafe(submission.getClassesText()),
                nullSafe(submission.getInterfacesText()),
                nullSafe(submission.getRelationshipsText()),
                nullSafe(submission.getExplanationText()),
                nullSafe(submission.getTradeoffsText())
        );
    }

    private String nullSafe(String s) {
        return (s == null || s.isBlank()) ? "(not provided)" : s;
    }
}
