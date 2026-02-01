# Chaincode API

## Contract Name
`document`

## Transactions

### CreateDocument (SUBMIT)

Creates a new document record on the ledger.

**Intent:** `Transaction.TYPE.SUBMIT` (writes to ledger, requires endorsement + ordering)

**Signature:**
```
CreateDocument(ctx Context, payloadJson string) -> string
```

**Input:**
- `payloadJson` (string): JSON object containing at minimum a `documentId` field. All other fields are stored as-is.

**Expected payload structure** (from `fabric-integration` service):
```json
{
  "documentId": "DOC-001",
  "revisionHash": "abc123...",
  "action": "CREATE",
  "status": "ACTIVE",
  "actor": "user1",
  "timestamp": "2024-01-01T00:00:00Z",
  "attributes": { "key": "value" }
}
```
Note: The chaincode does NOT validate or enforce any fields except `documentId`. The full payload is stored verbatim.

**Behavior:**
1. Parse `payloadJson` as JSON map
2. Extract `documentId` from the parsed map
3. Validate that `documentId` is present and non-blank
4. Query ledger state for key `documentId`
5. If key already exists (non-empty value), reject with error: `"Document already exists: {documentId}"`
6. Store `payloadJson` as string state under key `documentId`
7. Return JSON response:
```json
{
  "documentId": "DOC-001",
  "stored": true
}
```

**Errors:**
| Condition | Error message |
|-----------|---------------|
| `documentId` missing or blank | `"documentId is required"` |
| Document already exists on ledger | `"Document already exists: {documentId}"` |
| Invalid JSON payload | `"Invalid JSON payload: {parse error}"` |

---

### GetDocument (EVALUATE)

Retrieves a document record from the ledger by its ID.

**Intent:** `Transaction.TYPE.EVALUATE` (read-only query, no endorsement/ordering)

**Signature:**
```
GetDocument(ctx Context, documentId string) -> string
```

**Input:**
- `documentId` (string): The document identifier to look up

**Behavior:**
1. Validate that `documentId` is present and non-blank
2. Query ledger state for key `documentId`
3. If key does not exist or value is empty, reject with error
4. Return the stored JSON string as-is (the original `payloadJson` from CreateDocument)

**Errors:**
| Condition | Error message |
|-----------|---------------|
| `documentId` missing or blank | `"documentId is required"` |
| Document not found on ledger | `"Document not found: {documentId}"` |

---

### UpdateDocument (SUBMIT)

Applies a partial update (patch) to an existing document on the ledger.

**Intent:** `Transaction.TYPE.SUBMIT` (writes to ledger, requires endorsement + ordering)

**Signature:**
```
UpdateDocument(ctx Context, payloadJson string) -> string
```

**Input:**
- `payloadJson` (string): JSON object containing the update instruction.

**Required payload structure:**
```json
{
  "documentId": "DOC-001",
  "actor": "user1",
  "timestamp": "2024-06-15T10:30:00Z",
  "patch": {
    "status": "REVIEWED",
    "attributes": { "reviewedBy": "manager1" }
  }
}
```

**Behavior:**
1. Parse `payloadJson` as JSON map
2. Extract and validate required fields: `documentId`, `actor`, `timestamp`, `patch`
3. Query ledger state for key `documentId`
4. If key does not exist (empty value), reject with error: `"Document not found: {documentId}"`
5. Deserialize the existing stored JSON into a map
6. Validate that `patch` does not attempt to overwrite `documentId`
7. Merge all `patch` fields into the existing document map (shallow merge — top-level keys in `patch` overwrite corresponding keys in the stored document)
8. Serialize the merged map back to JSON and store under key `documentId`
9. Return JSON response:
```json
{
  "documentId": "DOC-001",
  "updated": true
}
```

**Errors:**
| Condition | Error message |
|-----------|---------------|
| `documentId` missing or blank | `"documentId is required"` |
| `actor` missing or blank | `"actor is required"` |
| `timestamp` missing or blank | `"timestamp is required"` |
| `patch` missing or not an object | `"patch is required"` |
| `patch` contains `documentId` key | `"patch must not overwrite documentId"` |
| Document does not exist on ledger | `"Document not found: {documentId}"` |
| Invalid JSON payload | `"Invalid JSON payload: {parse error}"` |

---

### SignDocument (SUBMIT)

Records a cryptographic signature against an existing document on the ledger.

**Intent:** `Transaction.TYPE.SUBMIT` (writes to ledger, requires endorsement + ordering)

**Signature:**
```
SignDocument(ctx Context, payloadJson string) -> string
```

**Input:**
- `payloadJson` (string): JSON object containing the signature data.

**Required payload structure:**
```json
{
  "documentId": "DOC-001",
  "actor": "user1",
  "timestamp": "2024-06-15T11:00:00Z",
  "signature": {
    "hash": "a1b2c3d4e5f6...",
    "algo": "SHA-256"
  }
}
```

**Behavior:**
1. Parse `payloadJson` as JSON map
2. Extract and validate required fields: `documentId`, `actor`, `timestamp`, `signature` (with nested `hash` and `algo`)
3. Query ledger state for key `documentId`
4. If key does not exist (empty value), reject with error: `"Document not found: {documentId}"`
5. Deserialize the existing stored JSON into a map
6. Build a signature entry object: `{ "actor": actor, "timestamp": timestamp, "hash": hash, "algo": algo }`
7. Append the signature entry to the `signatures` array in the document (create the array if it does not exist)
8. Serialize the updated map back to JSON and store under key `documentId`
9. Return JSON response:
```json
{
  "documentId": "DOC-001",
  "signed": true
}
```

**Errors:**
| Condition | Error message |
|-----------|---------------|
| `documentId` missing or blank | `"documentId is required"` |
| `actor` missing or blank | `"actor is required"` |
| `timestamp` missing or blank | `"timestamp is required"` |
| `signature` missing or not an object | `"signature is required"` |
| `signature.hash` missing or blank | `"signature.hash is required"` |
| `signature.algo` missing or blank | `"signature.algo is required"` |
| Document does not exist on ledger | `"Document not found: {documentId}"` |
| Invalid JSON payload | `"Invalid JSON payload: {parse error}"` |

---

## Invocation from fabric-integration

### Submit (CreateDocument)
```java
// Using proposal/commit flow:
Proposal proposal = contract.newProposal("CreateDocument")
    .addArguments(payloadJson)
    .build();
Transaction endorsed = proposal.endorse();
SubmittedTransaction submitted = endorsed.submitAsync();
String txId = endorsed.getTransactionId();
```

### Query (GetDocument)
```java
byte[] result = contract.evaluateTransaction("GetDocument", documentId);
String documentJson = new String(result, StandardCharsets.UTF_8);
```

### Submit (UpdateDocument)
```java
Proposal proposal = contract.newProposal("UpdateDocument")
    .addArguments(payloadJson)
    .build();
Transaction endorsed = proposal.endorse();
SubmittedTransaction submitted = endorsed.submitAsync();
String txId = endorsed.getTransactionId();
```

### Submit (SignDocument)
```java
Proposal proposal = contract.newProposal("SignDocument")
    .addArguments(payloadJson)
    .build();
Transaction endorsed = proposal.endorse();
SubmittedTransaction submitted = endorsed.submitAsync();
String txId = endorsed.getTransactionId();
```
