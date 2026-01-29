package com.example.document;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.util.HashMap;
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
        // Store the whole payload as-is under documentId
        ctx.getStub().putStringState(documentId, payloadJson);

        // Return something useful (your service can treat this as response)
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
}
