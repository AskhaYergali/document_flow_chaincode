# Invariants

## 1. Document uniqueness
A `documentId` can only be created once. Attempting to create a document with an existing `documentId` MUST fail with a `ChaincodeException`.

## 2. Payload transparency
The chaincode stores the payload JSON verbatim. It MUST NOT modify, enrich, transform, or normalize the payload before writing to the ledger. What goes in is exactly what comes out via GetDocument.

## 3. Minimal validation
The chaincode validates ONLY:
- The input is valid JSON
- The `documentId` field exists and is non-blank
- The `documentId` does not already exist on the ledger (for CreateDocument)

No other field validation is performed. Business validation is the caller's responsibility.

## 4. Document must exist before mutation
`UpdateDocument` and `SignDocument` MUST reject requests targeting a `documentId` that does not exist on the ledger. Only `CreateDocument` can introduce a new key.

## 5. No deletes
The chaincode provides no delete operation. Document records are permanent once created.

## 6. Patch must not overwrite documentId
`UpdateDocument` MUST reject a patch that contains a `documentId` key. The document identity is immutable after creation.

## 7. Signature append-only
`SignDocument` appends to the `signatures` array in the stored document. It MUST NOT remove or overwrite existing signature entries.

## 8. Stateless contract
The `DocumentContract` class has no mutable instance state. The `Genson` serializer is a static final singleton. All state is read from and written to the Fabric ledger via `ctx.getStub()`.

## 9. Single-argument convention
All SUBMIT transactions accept a single `payloadJson` string argument containing the full JSON payload. EVALUATE transactions accept individual typed arguments (e.g., `documentId`).
