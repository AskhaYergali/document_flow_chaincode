# Validation

## Input Validation

### CreateDocument
| Check | Condition | Error |
|-------|-----------|-------|
| JSON parseable | `payloadJson` must be valid JSON deserializable to a `Map<String, Object>` | `"Invalid JSON payload: {details}"` |
| documentId present | `documentId` field must exist in the parsed map and be non-null | `"documentId is required"` |
| documentId non-blank | `documentId` string value must not be blank (empty or whitespace) | `"documentId is required"` |
| documentId unique | No existing ledger state for this key | `"Document already exists: {documentId}"` |

### GetDocument
| Check | Condition | Error |
|-------|-----------|-------|
| documentId non-null | `documentId` argument must not be null | `"documentId is required"` |
| documentId non-blank | `documentId` argument must not be blank | `"documentId is required"` |
| Document exists | Ledger state for key must exist and be non-empty | `"Document not found: {documentId}"` |

### UpdateDocument
| Check | Condition | Error |
|-------|-----------|-------|
| JSON parseable | `payloadJson` must be valid JSON deserializable to a `Map<String, Object>` | `"Invalid JSON payload: {details}"` |
| documentId present | `documentId` field must exist in the parsed map and be non-null | `"documentId is required"` |
| documentId non-blank | `documentId` string value must not be blank | `"documentId is required"` |
| actor present | `actor` field must exist and be non-blank | `"actor is required"` |
| timestamp present | `timestamp` field must exist and be non-blank | `"timestamp is required"` |
| patch present | `patch` field must exist and be a `Map` (JSON object) | `"patch is required"` |
| patch safe | `patch` must not contain a `documentId` key | `"patch must not overwrite documentId"` |
| Document exists | Ledger state for key must exist and be non-empty | `"Document not found: {documentId}"` |

### SignDocument
| Check | Condition | Error |
|-------|-----------|-------|
| JSON parseable | `payloadJson` must be valid JSON deserializable to a `Map<String, Object>` | `"Invalid JSON payload: {details}"` |
| documentId present | `documentId` field must exist in the parsed map and be non-null | `"documentId is required"` |
| documentId non-blank | `documentId` string value must not be blank | `"documentId is required"` |
| actor present | `actor` field must exist and be non-blank | `"actor is required"` |
| timestamp present | `timestamp` field must exist and be non-blank | `"timestamp is required"` |
| signature present | `signature` field must exist and be a `Map` (JSON object) | `"signature is required"` |
| signature.hash present | `hash` field inside `signature` must exist and be non-blank | `"signature.hash is required"` |
| signature.algo present | `algo` field inside `signature` must exist and be non-blank | `"signature.algo is required"` |
| Document exists | Ledger state for key must exist and be non-empty | `"Document not found: {documentId}"` |

## What is NOT validated
- Payload schema (no required fields besides `documentId`)
- Field types or formats (timestamps, enums, etc.)
- Payload size limits
- Caller identity or MSP-based access control
- Document status transitions or business rules
