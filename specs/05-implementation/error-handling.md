# Error Handling

## Exception Type
All errors are thrown as `org.hyperledger.fabric.shim.ChaincodeException` with a descriptive message string.

## Error Catalog

| Error | Transaction | Condition | Message format |
|-------|------------|-----------|---------------|
| Missing documentId | CreateDocument | `documentId` field is null or blank in parsed payload | `"documentId is required"` |
| Duplicate document | CreateDocument | Key already exists on ledger with non-empty value | `"Document already exists: {documentId}"` |
| Invalid JSON | CreateDocument | `payloadJson` cannot be parsed as JSON | `"Invalid JSON payload: {exception message}"` |
| Missing documentId | GetDocument | `documentId` argument is null or blank | `"documentId is required"` |
| Not found | GetDocument | Key does not exist or has empty value on ledger | `"Document not found: {documentId}"` |
| Missing documentId | UpdateDocument | `documentId` field is null or blank in parsed payload | `"documentId is required"` |
| Missing actor | UpdateDocument | `actor` field is null or blank in parsed payload | `"actor is required"` |
| Missing timestamp | UpdateDocument | `timestamp` field is null or blank in parsed payload | `"timestamp is required"` |
| Missing patch | UpdateDocument | `patch` field is null or not a JSON object | `"patch is required"` |
| Patch overwrites ID | UpdateDocument | `patch` contains a `documentId` key | `"patch must not overwrite documentId"` |
| Not found | UpdateDocument | Key does not exist or has empty value on ledger | `"Document not found: {documentId}"` |
| Invalid JSON | UpdateDocument | `payloadJson` cannot be parsed as JSON | `"Invalid JSON payload: {exception message}"` |
| Missing documentId | SignDocument | `documentId` field is null or blank in parsed payload | `"documentId is required"` |
| Missing actor | SignDocument | `actor` field is null or blank in parsed payload | `"actor is required"` |
| Missing timestamp | SignDocument | `timestamp` field is null or blank in parsed payload | `"timestamp is required"` |
| Missing signature | SignDocument | `signature` field is null or not a JSON object | `"signature is required"` |
| Missing hash | SignDocument | `signature.hash` is null or blank | `"signature.hash is required"` |
| Missing algo | SignDocument | `signature.algo` is null or blank | `"signature.algo is required"` |
| Not found | SignDocument | Key does not exist or has empty value on ledger | `"Document not found: {documentId}"` |
| Invalid JSON | SignDocument | `payloadJson` cannot be parsed as JSON | `"Invalid JSON payload: {exception message}"` |

## JSON Parsing
JSON parsing errors are caught in the `parseJson()` helper method. Any exception during `genson.deserialize()` is wrapped in a `ChaincodeException` with the original error message included.

If parsing succeeds but returns `null`, an empty `HashMap` is used as fallback (the `documentId` check will then fail with "documentId is required").

## How Errors Propagate to Callers
- `ChaincodeException` causes the Fabric peer to return an error response to the Gateway client
- The `fabric-integration` service receives these as `EndorseException` (for submit) or `GatewayException` (for evaluate)
- The integration service maps these to `EndorsementFailedException` or `FabricUnavailableException`

## No Error Codes
The chaincode uses plain string messages only. It does not use `ChaincodeException(String message, String payload)` or `ChaincodeException(String message, byte[] payload)` variants for structured error data.
