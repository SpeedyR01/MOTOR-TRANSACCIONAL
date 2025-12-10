package util;

import model.PaymentTransaction;
import model.RefundTransaction;
import model.Transaction;

import java.util.Locale;

/**
 * Parseo JSON a Transaction sin usar Gson.
 * Funciona con JSON simple como:
 * {"type":"PAYMENT","amount":123.45,"payload":"abc","id":"xyz"}
 */
public final class JsonUtils {

    private JsonUtils() {}

    private static class TxDto {
        String id;
        String type;
        Double amount;
        String payload;
    }

    public static Transaction parseTransaction(String json) {
        if (json == null) return null;
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return null;

        TxDto dto = parseJsonManually(json);
        if (dto == null || dto.type == null) return null;

        String type = dto.type.trim().toUpperCase(Locale.ROOT);
        String id = (dto.id != null && !dto.id.trim().isEmpty())
                ? dto.id.trim()
                : util.IdGenerator.generate();

        double amount = (dto.amount != null) ? dto.amount : 0.0;
        String payload = dto.payload;

        switch (type) {
            case "PAYMENT":
                return new PaymentTransaction(id, amount, payload);
            case "REFUND":
                return new RefundTransaction(id, amount, payload);
            default:
                return null;
        }
    }

    /**
     * Parser manual muy simple para JSON llano.
     */
    private static TxDto parseJsonManually(String json) {
        TxDto dto = new TxDto();

        dto.type = extractString(json, "type");
        dto.id = extractString(json, "id");
        dto.payload = extractString(json, "payload");
        dto.amount = extractDouble(json, "amount");

        return dto;
    }

    private static String extractString(String json, String key) {
        try {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx == -1) return null;

            int start = json.indexOf("\"", idx + pattern.length());
            int end = json.indexOf("\"", start + 1);
            return json.substring(start + 1, end);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double extractDouble(String json, String key) {
        try {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx == -1) return null;

            int colon = json.indexOf(":", idx);
            int end = findNumberEnd(json, colon + 1);

            String raw = json.substring(colon + 1, end).trim();
            return Double.parseDouble(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static int findNumberEnd(String json, int start) {
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (!Character.isDigit(c) && c != '.' && c != '-') break;
            i++;
        }
        return i;
    }
}
