# Bonita Shell Connector

## Project Overview

- **Name**: Bonita Shell Connector
- **Artifact**: `org.bonitasoft.connectors:bonita-connector-shell-script`
- **Version**: 1.1.4-SNAPSHOT
- **Description**: Bonita connector that executes an arbitrary shell script by writing it to a temporary file and invoking a configurable interpreter (bash, sh, cmd, PowerShell, etc.).
- **License**: GPL-2.0
- **Tech stack**: Java 11, Maven, Bonita Engine 7.14.0, JUnit 4, Mockito 1.x, AssertJ

## Build Commands

```bash
# Full build with tests (default goal)
./mvnw clean verify

# Skip tests
./mvnw clean verify -DskipTests

# Run tests only
./mvnw test

# Check license headers (validate phase)
./mvnw validate

# Apply/format license headers
./mvnw license:format

# Package connector ZIP
./mvnw clean package

# Deploy to Maven Central (requires GPG key)
./mvnw clean deploy -Pdeploy
```

The build produces:
- `target/bonita-connector-shell-script-<version>.jar`
- `target/bonita-connector-shell-script-<version>-*.zip` — Bonita connector assembly

## Architecture

### Class hierarchy

```
AbstractConnector (bonita-common)
  └── ShellConnector          # Single connector class; all shell execution logic
```

### Key patterns

- **Minimal design**: The entire implementation is one class (`ShellConnector`).
- **Script file lifecycle**: The script string is written to a `File.createTempFile()`, made executable, executed via `ProcessBuilder`, then deleted in a `cleanUp()` step. `deleteOnExit()` is also set as a safety net.
- **Interpreter dispatch**: The file extension is derived from the `interpreter` input (`.bat` for `cmd`, `.ps1` for `powershell`, `.sh` for `sh`/`bash`).
- **Process invocation**: `ProcessBuilder([interpreter, parameter, scriptPath]).redirectErrorStream(true)` — stderr is merged into stdout.
- **Input parameters**:
  - `interpreter` (String) — path or name of the shell interpreter (e.g., `/bin/bash`, `cmd`); mandatory.
  - `parameter` (String) — interpreter flag to accept a script file (e.g., `-c` for bash, `/C` for cmd); mandatory.
  - `script` (String) — the script body; mandatory.
- **Output parameters**:
  - `result` (String) — combined stdout+stderr output of the process.
  - `exitStatus` (Integer) — process exit code.
- **License check**: `license-maven-plugin` enforces the GPL header on all `.java` files at `validate`.

## Testing Strategy

- Framework: JUnit 4 + AssertJ + Mockito 1.x
- Tests cover input validation (null/empty interpreter, parameter, script), correct error message formatting, and (on Unix) actual script execution producing expected output and exit code.
- OS-dependent tests (actual process execution) may be skipped or behave differently on Windows.
- Coverage enforced via JaCoCo.
- SonarCloud project key: `bonitasoft_bonita-connector-shell-script`.

## Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer(s)]
```

Common types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `ci`.

Examples:
```
fix: delete temp script file even when process throws
feat: support PowerShell .ps1 extension detection
chore: upgrade Mockito to 5.x
```

## Release Process

1. Remove `-SNAPSHOT` from `version` in `pom.xml`.
2. Update `shell.def.version` if the connector definition changed.
3. Commit: `chore: release X.Y.Z`.
4. Tag: `git tag X.Y.Z`.
5. Deploy to Maven Central: `./mvnw clean deploy -Pdeploy` (requires GPG key and Central credentials in `~/.m2/settings.xml`).
6. Push tag: `git push origin X.Y.Z`.
7. Bump to next `-SNAPSHOT` and commit: `chore: prepare next development iteration`.
