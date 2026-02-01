# Document Flow Chaincode

## Purpose
Hyperledger Fabric chaincode (smart contract) for storing and retrieving document event records on the ledger.

## What it does
- Accepts a JSON payload representing a document event and stores it on the Fabric ledger keyed by `documentId`
- Provides query access to retrieve a stored document by its `documentId`
- Enforces that a document cannot be created twice (duplicate rejection)
- Supports partial document updates via a `patch` field (shallow merge into existing document)
- Records cryptographic signatures against existing documents (appended to a `signatures` array)

## What it does NOT do
- No business rule evaluation (approval workflows, status transitions, access control)
- No payload schema enforcement beyond requiring specific fields per transaction
- No document deletions
- No rich queries or pagination
- No private data collections
- No cross-chaincode calls

## Technology
- Java 17
- Hyperledger Fabric Chaincode Shim (`fabric-chaincode-shim:2.5.8`)
- Genson for JSON serialization/deserialization
- Gradle build with Shadow JAR plugin
- Deployed as external chaincode on fabric-samples/test-network

## Consumers
- `fabric-integration` service (Spring Boot) calls this chaincode via the Fabric Gateway SDK
- The integration service sends `DocumentPayload` JSON as a single string argument to `CreateDocument`
- The integration service calls `GetDocument` with a `documentId` string argument for queries
- The integration service calls `UpdateDocument` with a JSON payload containing `documentId`, `actor`, `timestamp`, and `patch`
- The integration service calls `SignDocument` with a JSON payload containing `documentId`, `actor`, `timestamp`, and `signature`
