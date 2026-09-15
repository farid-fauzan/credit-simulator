package com.creditsimulator.io;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal parser for a flat JSON object (string/number values only, no
 * nesting), enough for the credit_simulator web service payload. Written by
 * hand to avoid pulling in an external JSON library.
 */
public final class SimpleJsonParser {

    private SimpleJsonParser() {
    }

    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        String body = json.trim();
        if (body.startsWith("{")) {
            body = body.substring(1);
        }
        if (body.endsWith("}")) {
            body = body.substring(0, body.length() - 1);
        }

        for (String pair : splitTopLevel(body)) {
            String trimmedPair = pair.trim();
            if (trimmedPair.isEmpty()) {
                continue;
            }
            int colonIndex = indexOfUnquotedColon(trimmedPair);
            String rawKey = trimmedPair.substring(0, colonIndex).trim();
            String rawValue = trimmedPair.substring(colonIndex + 1).trim();
            result.put(unquote(rawKey), unquote(rawValue));
        }
        return result;
    }

    private static String[] splitTopLevel(String body) {
        return body.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    private static int indexOfUnquotedColon(String pair) {
        boolean inQuotes = false;
        for (int i = 0; i < pair.length(); i++) {
            char c = pair.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ':' && !inQuotes) {
                return i;
            }
        }
        throw new IllegalArgumentException("JSON tidak valid, tidak ditemukan ':' pada: " + pair);
    }

    private static String unquote(String raw) {
        String trimmed = raw.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
