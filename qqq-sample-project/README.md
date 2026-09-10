# QQQ Sample Project

Reference application for QQQ 4.0: tables, related records, processes, widgets, the Material Dashboard 0.40.0, and a PicoCLI entry point. Requires Java 21 and Maven 3.8 or later. The default database is an in-memory H2 database populated with sample data at server startup.

## Build and run

From the QQQ repository root, install the framework modules, then build this separate sample:

```bash
mvn clean install
mvn -f qqq-sample-project/pom.xml clean verify
mvn -f qqq-sample-project/pom.xml exec:java \
  -Dexec.mainClass=com.kingsrook.sampleapp.SampleJavalinServer \
  -Dqqq.sample.mockAuthentication=true
```

Open <http://localhost:8000/>. Expand **People App**, open **Greetings App**, and select **Person**. Open a record and choose **Actions → Greet Interactive** to try the local mock process. Stop the server with Ctrl+C; restarting it recreates the sample database. The explicit `qqq.sample.mockAuthentication` option selects bundled mock authentication for local exploration. Without it, configure `OAUTH2_BASE_URL`, `OAUTH2_CLIENT_ID`, `OAUTH2_CLIENT_SECRET`, and `OAUTH2_SCOPES` in the process environment or a `.env` file. Mock authentication is for local sample data only.

To inspect the command-line interface:

```bash
mvn -f qqq-sample-project/pom.xml exec:java \
  -Dexec.mainClass=com.kingsrook.sampleapp.SampleCli -Dexec.args="--help" \
  -Dqqq.sample.mockAuthentication=true
```

## Source entry points

All paths below are under `src/main/java/com/kingsrook/sampleapp/`:

- `metadata/SampleMetaDataProvider.java`: database, tables, processes, and navigation.
- `SampleJavalinServer.java`: HTTP server and sample database initialization.
- `SampleCli.java`: command-line entry point.

This sample is built from the repository and is not published to Maven Central. See the [4.0 migration guide](../docs/migration/4.0.adoc) for API changes.

## License

GNU Affero General Public License v3.0; see the repository [LICENSE](../LICENSE).
