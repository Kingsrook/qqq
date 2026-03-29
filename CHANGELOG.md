# Changelog

All notable changes to QQQ will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.40.0] - 2026-03-29

### Breaking Changes
- **TableBasedAuthenticationModule** - Removed SHA1 backward compatibility for password hashing. Only SHA256 format (`sha256:iterations:salt:hash`) is now supported. Users with legacy SHA1-hashed passwords must reset their passwords.

### Added
- **Field Functions** - Virtual computed fields with backend-specific implementations (core, RDBMS, PostgreSQL, MongoDB). Supports `StringLength`, `WeekdayOfDate`, and more.
- **OAuth2 externalBaseUrl** - Split internal/external URL deployments for Kubernetes environments where pods cannot reach LoadBalancer VIPs
- **Collapsible Elements** - New `Collapsible` metadata class applied to `QFieldSection` and `QWidgetMetaData`
- **SpotBugs + PMD** - Static analysis integrated into CI pipeline via `qqq-orb/static_analysis` job
- **Branding** - `accentColorLight` and `gravatarDefault` fields added to branding metadata
- **Virtual Fields as PVS** - Virtual fields supported in possible value sources and `TableMetaData` API
- **Join Validation** - `JoinGraph` tracks flipped joins, `matchesJoinPath` considers flipped joins for matching
- **Saved Views** - `QuerySavedViewProcess` single-view mode adds quick-view attributes

### Fixed
- **JoinsContext** - Clone incoming `QueryJoin` objects to prevent shared-state mutation; prevent double-flip of multi-hop security join metadata; exclude implicit security lock joins from join cross product
- **MemoryRecordStore** - Clear stale display values on queried records; add virtual fields after stripping unrecognized fields; use `JoinsContext` query joins for cross product
- **RDBMS** - Paginate over primary keys in `doDeleteList` to avoid oversized queries; fix param binding in set-operation queries with virtual fields; fix order-by for grouped fields with field functions
- **Joins** - Normalize self-joins correctly by comparing fields; flipped joins respect `joinOn` fields
- **TableMetaDataAction** - Exclude deeply-nested exposed joins from response
- **MetaData Production** - Add class name tie-breaking for stable producer ordering

### Changed
- **Theme Refactor** - `QThemeMetaData` moved from `qqq-backend-core` to `qqq-frontend-material-dashboard` (frontend-specific)
- **License** - Migrated from AGPL-3.0 to Apache-2.0

### Security
- **Jetty 11.0.26** - Upgraded from 11.0.25 to fix HTTP/2 vulnerability (HIGH)
- **WireMock 3.13.2** - Upgraded from 3.13.0 to fix commons-fileupload vulnerability (HIGH)
- **commons-lang 2.x removed** - Migrated to commons-lang3 3.20.0 (no fix available for 2.x MEDIUM CVE)
- **iq80 snappy excluded** - Excluded vulnerable snappy from checkstyle plugin dependencies (MEDIUM)
- **mysql-connector-j 8.4.0** - Migrated from deprecated mysql:mysql-connector-java 8.0.30 (HIGH)
- **protobuf-java 3.25.5** - Override to fix DoS vulnerability (HIGH)
- **PasswordHasher SHA1 removed** - Eliminated weak cryptographic algorithm (CodeQL alert)
- **RapiDoc XSS hardened** - Strengthened URL validation with origin checking

### Notes
- commons-lang3 alert dismissed - already at 3.20.0
- commons-beanutils alerts dismissed - already at fix version 1.11.0
- jetty-http alerts dismissed - requires Jetty 12.x (Javalin 7.x)

## [0.35.0] - 2025-12-28

### Changed

#### Platform
- **Java 21 LTS** - Migrated from Java 17 to Java 21 LTS

#### Dependencies - Major Updates
- **JUnit Jupiter** 5.8.1 → 6.0.1 (major version upgrade)
- **Checkstyle** 10.16.0 → 12.2.0 (major version upgrade)
- **Mockito** 5.14.2 → 5.21.0
- **ByteBuddy** 1.15.4 → 1.18.3

#### Dependencies - Build Plugins
- maven-compiler-plugin 3.10.1 → 3.14.1
- maven-surefire-plugin 3.5.3 → 3.5.4
- maven-jar-plugin 3.4.2 → 3.5.0
- central-publishing-maven-plugin 0.8.0 → 0.9.0

#### Dependencies - Runtime
- MongoDB Driver 5.5.1 → 5.6.2
- SQLite JDBC 3.47.1.0 → 3.51.1.0
- AWS SDK BOM 2.40.13 → 2.40.16
- AWS Lambda Java Core 1.2.3 → 1.4.0
- AWS Lambda Java Events 3.14.0 → 3.16.1
- AWS Lambda Runtime Interface Client 2.6.0 → 2.8.7
- Commons Validator 1.9.0 → 1.10.1
- Jakarta Mail 2.0.1 → 2.0.2
- Angus Activation 2.0.2 → 2.0.3
- Nashorn Core 15.6 → 15.7
- SLF4J API 2.0.16 → 2.0.17
- Kotlin Stdlib 2.2.21 → 2.3.0

#### Dependencies - Test
- H2 Database 2.2.220 → 2.4.240
- Unirest Java 3.13.12 → 3.14.5

### Added
- Enhanced documentation wiki with comprehensive guides
- Improved GitHub templates and contribution process
- Better cross-references between documentation pages
- Health check middleware module (qqq-middleware-health)

## [0.27.0] - 2024-01-XX

### Added
- Comprehensive documentation wiki
- Enhanced development workflow guides
- Improved testing and code review standards
- Better contribution guidelines and templates

### Changed
- Streamlined documentation for QQQ framework developers
- Improved cross-references between wiki pages
- Enhanced GitHub project structure and templates

## [0.26.1] - 2024-01-XX

### Fixed
- Various bug fixes and improvements
- Enhanced stability and performance

## [0.26.0] - 2024-01-XX

### Added
- Core QQQ framework capabilities
- Backend modules for RDBMS, filesystem, MongoDB, SQLite
- Middleware support for Javalin, PicoCLI, Lambda, Slack
- React dashboard framework with Material-UI
- Comprehensive testing and quality standards

### Changed
- Initial public release of QQQ framework
- Established development workflow and standards
- Created modular architecture foundation

---

## 📚 For Detailed Information

**📖 [Complete Documentation Wiki](https://github.com/Kingsrook/qqq/wiki)** - Start here for comprehensive guides

- **[🏠 Home](https://github.com/Kingsrook/qqq/wiki/Home)** - Project overview and quick start
- **[🚀 Release Flow](https://github.com/Kingsrook/qqq/wiki/Release-Flow)** - Detailed release process
- **[🏷️ Changelog & Tagging](https://github.com/Kingsrook/qqq/wiki/Changelog-and-Tagging)** - Commit conventions and release notes
- **[🔧 Developer Onboarding](https://github.com/Kingsrook/qqq/wiki/Developer-Onboarding)** - Setup and contribution guide

## 🔄 Version Compatibility

QQQ follows semantic versioning:
- **MAJOR** versions may contain breaking changes
- **MINOR** versions add new functionality (backward compatible)
- **PATCH** versions contain bug fixes (backward compatible)

For detailed compatibility information, see [Compatibility Matrix](https://github.com/Kingsrook/qqq/wiki/Compatibility-Matrix).

---

**Thank you for using QQQ!** 🚀
