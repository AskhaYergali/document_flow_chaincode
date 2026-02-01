package com.example.document;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Contract(name = "document")
public class DocumentContract implements ContractInterface {

    private static final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String CreateDocument(Context ctx, String payloadJson) {
        Map<String, Object> payload = parseJson(payloadJson);

        String documentId = asString(payload.get("documentId"));
        if (documentId == null || documentId.isBlank()) {
            throw new ChaincodeException("documentId is required");
        }

        String existing = ctx.getStub().getStringState(documentId);
        if (existing != null && !existing.isBlank()) {
            throw new ChaincodeException("Document already exists: " + documentId);
        }

        ctx.getStub().putStringState(documentId, payloadJson);

        Map<String, Object> resp = new HashMap<>();
        resp.put("documentId", documentId);
        resp.put("stored", true);
        return genson.serialize(resp);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetDocument(Context ctx, String documentId) {
        if (documentId == null || documentId.isBlank()) {
            throw new ChaincodeException("documentId is required");
        }
        String data = ctx.getStub().getStringState(documentId);
        if (data == null || data.isBlank()) {
            throw new ChaincodeException("Document not found: " + documentId);
        }
        return data;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String UpdateDocument(Context ctx, String payloadJson) {
        Map<String, Object> payload = parseJson(payloadJson);

        String documentId = requireString(payload, "documentId");
        requireString(payload, "actor");
        requireString(payload, "timestamp");

        Object patchObj = payload.get("patch");
        if (!(patchObj instanceof Map)) {
            throw new ChaincodeException("patch is required");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> patch = (Map<String, Object>) patchObj;

        if (patch.containsKey("documentId")) {
            throw new ChaincodeException("patch must not overwrite documentId");
        }

        String existing = ctx.getStub().getStringState(documentId);
        if (existing == null || existing.isBlank()) {
            throw new ChaincodeException("Document not found: " + documentId);
        }

        Map<String, Object> document = parseJson(existing);
        document.putAll(patch);

        ctx.getStub().putStringState(documentId, genson.serialize(document));

        Map<String, Object> resp = new HashMap<>();
        resp.put("documentId", documentId);
        resp.put("updated", true);
        return genson.serialize(resp);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String SignDocument(Context ctx, String payloadJson) {
        Map<String, Object> payload = parseJson(payloadJson);

        String documentId = requireString(payload, "documentId");
        String actor = requireString(payload, "actor");
        String timestamp = requireString(payload, "timestamp");

        Object sigObj = payload.get("signature");
        if (!(sigObj instanceof Map)) {
            throw new ChaincodeException("signature is required");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> signature = (Map<String, Object>) sigObj;

        String hash = asString(signature.get("hash"));
        if (hash == null || hash.isBlank()) {
            throw new ChaincodeException("signature.hash is required");
        }
        String algo = asString(signature.get("algo"));
        if (algo == null || algo.isBlank()) {
            throw new ChaincodeException("signature.algo is required");
        }

        String existing = ctx.getStub().getStringState(documentId);
        if (existing == null || existing.isBlank()) {
            throw new ChaincodeException("Document not found: " + documentId);
        }

        Map<String, Object> document = parseJson(existing);

        Map<String, Object> entry = new HashMap<>();
        entry.put("actor", actor);
        entry.put("timestamp", timestamp);
        entry.put("hash", hash);
        entry.put("algo", algo);

        @SuppressWarnings("unchecked")
        List<Object> signatures = document.get("signatures") instanceof List
                ? (List<Object>) document.get("signatures")
                : new ArrayList<>();
        signatures.add(entry);
        document.put("signatures", signatures);

        ctx.getStub().putStringState(documentId, genson.serialize(document));

        Map<String, Object> resp = new HashMap<>();
        resp.put("documentId", documentId);
        resp.put("signed", true);
        return genson.serialize(resp);
    }

    private static Map<String, Object> parseJson(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = genson.deserialize(json, Map.class);
            return m != null ? m : new HashMap<>();
        } catch (Exception e) {
            throw new ChaincodeException("Invalid JSON payload: " + e.getMessage());
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static String requireString(Map<String, Object> map, String field) {
        String value = asString(map.get(field));
        if (value == null || value.isBlank()) {
            throw new ChaincodeException(field + " is required");
        }
        return value;
    }
}
