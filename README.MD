# Shell Connector

[![Actions Status](https://github.com/bonitasoft/bonita-connector-shell-script/workflows/Build/badge.svg)](https://github.com/bonitasoft/bonita-connector-shell-script/actions?query=workflow%3ABuild)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=bonitasoft_bonita-connector-shell-script&metric=alert_status)](https://sonarcloud.io/dashboard?id=bonitasoft_bonita-connector-shell-script)
[![GitHub release](https://img.shields.io/github/v/release/bonitasoft/bonita-connector-shell-script?color=blue&label=Release)](https://github.com/bonitasoft/bonita-connector-shell-script/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.bonitasoft.connectors/bonita-connector-shell-script.svg?label=Maven%20Central&color=orange)](https://search.maven.org/search?q=g:%22org.bonitasoft.connectors%22%20AND%20a:%22bonita-connector-shell-script%22)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

## Compatibility

| Connector         | Bonita         | Java         |
|-------------------|----------------|--------------|
| 2.x (`master`)    | 12.0 and later | 17 and later |
| 1.x (up to 1.1.3) | 7.13 to 11.x   | 11 and later |

The connector definition (`scripting-shell` 1.0.0) and its inputs/outputs are unchanged.

### Security manager (Bonita Docker image)

This connector starts an external process (the `interpreter`). A runtime running connectors under a Java security
manager that denies process execution cannot run it, whatever the connector version: the Bonita 12 Docker image
runs a ProGrade security manager (`PRO_GRADE=true` by default) whose policy denies
`java.io.FilePermission "<<ALL FILES>>", "execute"` to all code. The connector then fails with:

```
org.bonitasoft.engine.connector.ConnectorException: access denied ("java.io.FilePermission" "/bin/sh" "execute")
```

To use this connector with the Bonita Docker image, start the container with `PRO_GRADE=false` (or provide a policy
granting the `execute` permission on the interpreters you allow), after assessing the security impact: any process
using this connector can then run arbitrary commands with the rights of the Bonita user.

## Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`./mvnw`

## Release

In order to create a new release:
- On the release branch, make sure to update the pom version (remove the -SNAPSHOT)
- Run the [release](https://github.com/bonitasoft/bonita-connector-shell-script/actions/workflows/release.yml) action, set the version to releae as parameter
- Update the `master` with the next SNAPSHOT version.

Once this is done, update the [Bonita marketplace repository](https://github.com/bonitasoft/bonita-marketplace) with the new version of the connector.

## Contributing

We would love you to contribute, pull requests are welcome! Please see the [CONTRIBUTING.md](CONTRIBUTING.md) for more information.

## License

The sources and documentation in this project are released under the [GPLv2 License](LICENSE)
