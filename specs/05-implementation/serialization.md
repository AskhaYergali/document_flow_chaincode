# Serialization

## Library
Genson (`com.owlike:genson:1.6`)

## Instance
A single static final `Genson` instance is used across all transactions:
```java
private static final Genson genson = new Genson();
```
Default configuration - no custom serializers, deserializers, or converters.

## Deserialization (input)
Incoming `payloadJson` is deserialized to `Map<String, Object>`:
```java
Map<String, Object> m = genson.deserialize(json, Map.class);
```

### CreateDocument
- Deserialized only to extract `documentId` for key derivation and existence check
- The raw `payloadJson` string (not the re-serialized map) is stored on the ledger

### UpdateDocument
- Deserialized to extract and validate `documentId`, `actor`, `timestamp`, and `patch`
- The existing ledger value is also deserialized so `patch` fields can be merged into it
- The merged document is re-serialized and written back to the ledger

### SignDocument
- Deserialized to extract and validate `documentId`, `actor`, `timestamp`, and `signature` (with `hash` and `algo`)
- The existing ledger value is also deserialized so a signature entry can be appended to the `signatures` array
- The updated document is re-serialized and written back to the ledger

## Serialization (output)
Response objects are constructed as `HashMap<String, Object>` and serialized:
```java
Map<String, Object> resp = new HashMap<>();
resp.put("documentId", documentId);
resp.put("stored", true);   // or "updated", "signed"
return genson.serialize(resp);
```

Updated ledger state (for UpdateDocument and SignDocument) is also serialized via `genson.serialize(document)`.

## Helper Methods
```java
private static Map<String, Object> parseJson(String json)
```
- Parses JSON string to Map
- Returns empty HashMap if result is null
- Wraps parse errors in ChaincodeException

```java
private static String asString(Object v)
```
- Null-safe conversion of Object to String via `String.valueOf()`
- Returns null if input is null

```java
private static String requireString(Map<String, Object> map, String field)
```
- Extracts a field from the map via `asString()` and validates it is non-null and non-blank
- Throws `ChaincodeException("{field} is required")` if missing or blank
- Used by `UpdateDocument` and `SignDocument` for common required-field validation
