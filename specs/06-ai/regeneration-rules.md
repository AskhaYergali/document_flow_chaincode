# Regeneration Rules

## Hard Constraints

### 1. Single contract class
The entire chaincode MUST be a single Java class `DocumentContract` in package `com.example.document`. Do NOT split into multiple files, layers, or helper classes beyond private static methods within the contract class.

### 2. Contract annotation
The class MUST:
- Be annotated with `@Contract(name = "document")`
- Implement `ContractInterface`

### 3. Transaction annotations
- `CreateDocument` MUST be annotated with `@Transaction(intent = Transaction.TYPE.SUBMIT)`
- `GetDocument` MUST be annotated with `@Transaction(intent = Transaction.TYPE.EVALUATE)`
- `UpdateDocument` MUST be annotated with `@Transaction(intent = Transaction.TYPE.SUBMIT)`
- `SignDocument` MUST be annotated with `@Transaction(intent = Transaction.TYPE.SUBMIT)`

### 4. Method signatures
```java
public String CreateDocument(Context ctx, String payloadJson)
public String GetDocument(Context ctx, String documentId)
public String UpdateDocument(Context ctx, String payloadJson)
public String SignDocument(Context ctx, String payloadJson)
```
- Method names are PascalCase (Fabric convention for exported transactions)
- First parameter is always `Context ctx`
- Return type is `String` (JSON)

### 5. Payload transparency
`CreateDocument` MUST store the raw `payloadJson` string on the ledger, NOT a re-serialized version. The payload goes through deserialization only to extract `documentId`.

`UpdateDocument` and `SignDocument` perform read-modify-write on the stored document. They deserialize the existing state, apply changes (patch merge or signature append), and re-serialize the result back to the ledger.

### 6. Error type
All errors MUST be thrown as `ChaincodeException`. Do NOT use checked exceptions, custom exception classes, or return error strings.

### 7. Stateless class
The contract class MUST NOT have mutable instance fields. The Genson instance MUST be `private static final`.

### 8. Serialization library
Use Genson (`com.owlike:genson`), NOT Jackson, Gson, or manual JSON building.

### 9. Build output
The Gradle build MUST produce a shadow (fat) JAR with `mergeServiceFiles()` enabled. Without this, the Fabric `ContractRouter` cannot discover the contract class at runtime.

### 10. Entry point
`mainClass` MUST be `org.hyperledger.fabric.contract.ContractRouter`. Do NOT create a custom main class.

### 11. Gradle wrapper
The repository MUST contain a `gradlew` bash script at the root that runs Gradle inside a `gradle:8.7-jdk17` Docker container. Do NOT add a standard Gradle Wrapper (`gradle-wrapper.jar` / `gradle-wrapper.properties`). Do NOT require a local Gradle or JDK installation.

## Code Style
- No Lombok
- No Spring Framework
- No dependency injection
- No interfaces or abstractions beyond `ContractInterface`
- Private static helper methods for JSON parsing and type conversion
- Descriptive error messages that include the offending value (e.g., document ID)

## What NOT to Add
- Do NOT add delete transactions unless explicitly requested
- Do NOT add rich queries, composite keys, or pagination
- Do NOT add access control or MSP-based authorization
- Do NOT add event emission (chaincode events)
- Do NOT add private data collections
- Do NOT add custom serialization annotations on model classes
- Do NOT create separate model/DTO classes - use `Map<String, Object>`
- Do NOT add unit tests, test dependencies, or test source directories - tests are excluded from this project
