package util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.PaymentTransaction;
import model.RefundTransaction;
import model.Transaction;

import java.util.Locale;

/**
 * Parseo JSON a Transaction usando Gson.
 * JSON esperado mínimo: {"type":"PAYMENT","amount":123.45,"payload":"...","id":"optional-id"}
 */
public final class JsonUtils {
    private static final Gson gson = new Gson();

    private JsonUtils() {}

    private static class TxDto {
        String id;
        String type;
        Double amount;
        String payload;
    }

    public static Transaction parseTransaction(String json) {
        if (json == null) return null;
        try {
            TxDto dto = gson.fromJson(json, TxDto.class);
            if (dto == null || dto.type == null) return null;
            String type = dto.type.trim().toUpperCase(Locale.ROOT);
            String id = (dto.id != null && !dto.id.trim().isEmpty()) ? dto.id.trim() : util.IdGenerator.generate();
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
        } catch (JsonSyntaxException ex) {
            return null;
        }
    }
}