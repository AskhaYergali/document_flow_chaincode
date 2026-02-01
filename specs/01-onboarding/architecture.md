# Architecture

## Overview
Single-contract chaincode with one Java source file. No layering needed - chaincode is intentionally minimal.

## Package Structure
```
com.example.document/
└── DocumentContract.java       # The sole smart contract
```

## Build Environment
- Gradle 8.7 + JDK 17 via Docker (`gradle:8.7-jdk17`)
- The `gradlew` script at the repo root delegates all Gradle commands to a disposable Docker container
- No local Gradle or JDK installation required; only Docker

## Runtime
- Entry point: `org.hyperledger.fabric.contract.ContractRouter` (provided by the shim)
- The shim discovers `DocumentContract` via the `@Contract` annotation
- Runs as an external chaincode process or inside a Docker container managed by the peer

## Dependencies
| Artifact | Purpose |
|----------|---------|
| `fabric-chaincode-shim:2.5.8` | Fabric contract API, stub, annotations |
| `genson:1.6` | JSON serialization/deserialization |

## Contract Registration
- Contract name: `document`
- Registered via `@Contract(name = "document")` annotation
- Implements `ContractInterface`

## Data Flow

### CreateDocument
```
Client (fabric-integration)
    |
    | submitTransaction("CreateDocument", payloadJson)
    v
DocumentContract.CreateDocument(ctx, payloadJson)
    |
    | 1. Parse JSON
    | 2. Extract documentId
    | 3. Check ledger — must NOT exist
    | 4. Store payload as-is under documentId key
    | 5. Return JSON response {documentId, stored: true}
    v
Ledger State: key=documentId, value=payloadJson
```

### UpdateDocument
```
Client (fabric-integration)
    |
    | submitTransaction("UpdateDocument", payloadJson)
    v
DocumentContract.UpdateDocument(ctx, payloadJson)
    |
    | 1. Parse JSON
    | 2. Validate documentId, actor, timestamp, patch
    | 3. Read existing document from ledger — must exist
    | 4. Shallow-merge patch fields into existing document
    | 5. Serialize merged document and write back
    | 6. Return JSON response {documentId, updated: true}
    v
Ledger State: key=documentId, value=mergedJson
```

### SignDocument
```
Client (fabric-integration)
    |
    | submitTransaction("SignDocument", payloadJson)
    v
DocumentContract.SignDocument(ctx, payloadJson)
    |
    | 1. Parse JSON
    | 2. Validate documentId, actor, timestamp, signature (hash, algo)
    | 3. Read existing document from ledger — must exist
    | 4. Append signature entry to signatures array
    | 5. Serialize updated document and write back
    | 6. Return JSON response {documentId, signed: true}
    v
Ledger State: key=documentId, value=updatedJson
```
