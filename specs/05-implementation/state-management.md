# State Management

## Ledger State Model

The chaincode uses the Fabric world state (key-value store) via `ctx.getStub()`.

### State Keys
| Key | Value | Written by | Read by |
|-----|-------|-----------|---------|
| `{documentId}` | Full JSON payload string | `CreateDocument`, `UpdateDocument`, `SignDocument` | `GetDocument`, `UpdateDocument`, `SignDocument` |

### Key Format
- Plain string, equal to the `documentId` extracted from the payload
- No composite keys
- No key prefixes or namespaces
- Examples: `"DOC-001"`, `"doc-abc-123"`

### Value Format
- Raw JSON string, stored exactly as received in `payloadJson` argument
- No transformation, wrapping, or enrichment
- Read back with `getStringState()` and returned as-is

## State Operations

### Write (CreateDocument)
```java
ctx.getStub().putStringState(documentId, payloadJson);
```
- Called only after confirming the key does not already exist
- Stores the caller-provided JSON verbatim

### Read (GetDocument)
```java
String data = ctx.getStub().getStringState(documentId);
```
- Returns `null` or empty string if key does not exist
- Both `null` and blank are treated as "not found"

### Read-Modify-Write (UpdateDocument)
```java
String existing = ctx.getStub().getStringState(documentId);
// deserialize existing → merge patch fields → serialize → write back
ctx.getStub().putStringState(documentId, updatedJson);
```
- Called only after confirming the key already exists
- Reads existing document, applies shallow merge of `patch` fields, writes back the merged result

### Read-Modify-Write (SignDocument)
```java
String existing = ctx.getStub().getStringState(documentId);
// deserialize existing → append to signatures array → serialize → write back
ctx.getStub().putStringState(documentId, updatedJson);
```
- Called only after confirming the key already exists
- Reads existing document, appends a new entry to the `signatures` array (creates array if absent), writes back

### Existence Check (CreateDocument)
```java
String existing = ctx.getStub().getStringState(documentId);
if (existing != null && !existing.isBlank()) {
    // Document already exists - reject
}
```

### Existence Check (UpdateDocument, SignDocument)
```java
String existing = ctx.getStub().getStringState(documentId);
if (existing == null || existing.isBlank()) {
    // Document does not exist - reject
}
```

## Not Used
- `putState()` / `getState()` (byte[] variants) - string variants used instead
- `delState()` - no delete operation
- Composite keys (`createCompositeKey`)
- Rich queries (`getQueryResult`)
- Key range queries (`getStateByRange`)
- Private data collections
- Transient data
- State-based endorsement policies
