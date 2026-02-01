# Build

## Build System
Gradle 8.7 with Shadow JAR plugin for producing a fat JAR. Gradle runs inside a Docker container via the `gradlew` wrapper script — no local Gradle installation is required.

## Project Coordinates
- **Group:** `com.example`
- **Artifact:** `document`
- **Version:** `1.0`
- **Root project name:** `document` (defined in `settings.gradle`)

## Java Version
Java 17. Enforced both by the Gradle toolchain in `build.gradle` and by the Docker image (`gradle:8.7-jdk17`) used in the `gradlew` wrapper. No local JDK is required.

## Dependencies
| Dependency | Version | Purpose |
|-----------|---------|---------|
| `org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim` | `2.5.8` | Fabric contract API, annotations, stub |
| `com.owlike:genson` | `1.6` | JSON serialization/deserialization |

## Repositories
- Maven Central
- JitPack (`https://jitpack.io`)

## Plugins
| Plugin | Version | Purpose |
|--------|---------|---------|
| `java` | (built-in) | Java compilation |
| `application` | (built-in) | Application entry point |
| `com.github.johnrengelman.shadow` | `8.1.1` | Fat JAR packaging |

## Application Entry Point
```
mainClass = 'org.hyperledger.fabric.contract.ContractRouter'
```
This is the Fabric shim's built-in router that discovers `@Contract` annotated classes.

## Gradle Wrapper (`gradlew`)

The project includes a `gradlew` bash script at the repository root. It is **not** the standard Gradle Wrapper (no `gradle-wrapper.jar` or `gradle-wrapper.properties`). Instead it delegates to Docker:

```bash
#!/usr/bin/env bash
set -euo pipefail
if [ ! -d ".gradle-wrapper" ]; then
  mkdir -p .gradle-wrapper
fi
exec docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace -w /workspace \
  gradle:8.7-jdk17 gradle "$@"
```

### How it works
1. Creates a `.gradle-wrapper` directory if absent (reserved for future cache use)
2. Runs `gradle` inside a disposable Docker container (`gradle:8.7-jdk17`)
3. Mounts the project root as `/workspace`
4. Runs as the host user's UID/GID so generated files are owned by the developer
5. Passes all arguments through to `gradle`

### Prerequisites
- Docker must be installed and the current user must be able to run `docker run`
- No local Gradle or JDK installation is needed

## Build Commands
```bash
# Build fat JAR
./gradlew shadowJar
# Output: build/libs/document-1.0.jar

# Build distribution
./gradlew installDist
```

## Shadow JAR Configuration
- `archiveClassifier` set to empty string (produces `document-1.0.jar`, not `document-1.0-all.jar`)
- `mergeServiceFiles()` is critical - merges `META-INF/services/*` files so the Fabric contract router can discover the contract class
- `DuplicatesStrategy.INCLUDE` on `jar` task
- `DuplicatesStrategy.EXCLUDE` on all `Copy` tasks to prevent Gradle 8 warnings

## Distribution Tasks
`distTar`, `distZip`, `installDist`, and `startScripts` all depend on `shadowJar` to ensure the fat JAR is used instead of the plain JAR with separate lib directory.

## Encoding
All Java compilation tasks use UTF-8 encoding.

## Testing
No test dependencies or test framework is included. Tests are excluded from this project. The `fabric-integration` service handles end-to-end testing.
