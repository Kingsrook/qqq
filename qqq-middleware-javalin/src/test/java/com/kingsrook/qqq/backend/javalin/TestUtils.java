/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.javalin;


import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.AssociatedScript;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.processes.implementations.mock.MockBackendStep;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.apache.commons.io.IOUtils;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Utility methods for unit tests.
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String BACKEND_NAME_MEMORY = "memory";

   public static final String TABLE_NAME_PERSON = "person";

   public static final String PROCESS_NAME_GREET_PEOPLE_INTERACTIVE = "greetInteractive";
   public static final String PROCESS_NAME_SIMPLE_SLEEP             = "simpleSleep";
   public static final String PROCESS_NAME_SIMPLE_THROW             = "simpleThrow";
   public static final String PROCESS_NAME_SLEEP_INTERACTIVE        = "sleepInteractive";

   public static final String STEP_NAME_SLEEPER = "sleeper";
   public static final String STEP_NAME_THROWER = "thrower";

   public static final String SCREEN_0 = "screen0";
   public static final String SCREEN_1 = "screen1";



   /*******************************************************************************
    ** Prime a test database (e.g., h2, in-memory)
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static void primeTestDatabase() throws Exception
   {
      ConnectionManager connectionManager          = new ConnectionManager();
      Connection        connection                 = connectionManager.getConnection(TestUtils.defineDefaultH2Backend());
      InputStream       primeTestDatabaseSqlStream = TestUtils.class.getResourceAsStream("/prime-test-database.sql");
      assertNotNull(primeTestDatabaseSqlStream);
      List<String> lines = (List<String>) IOUtils.readLines(primeTestDatabaseSqlStream);
      lines = lines.stream().filter(line -> !line.startsWith("-- ")).toList();
      String joinedSQL = String.join("\n", lines);
      for(String sql : joinedSQL.split(";"))
      {
         QueryManager.executeUpdate(connection, sql);
      }
   }



   /*******************************************************************************
    ** Run an SQL Query in the test database
    **
    *******************************************************************************/
   public static void runTestSql(String sql, QueryManager.ResultSetProcessor resultSetProcessor) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection        connection        = connectionManager.getConnection(defineDefaultH2Backend());
      QueryManager.executeStatement(connection, sql, resultSetProcessor);
   }



   /*******************************************************************************
    ** Define the q-instance for testing (h2 rdbms and 'person' table)
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineDefaultH2Backend());
      qInstance.addTable(defineTablePerson());
      qInstance.addJoin(definePersonJoinPartnerPerson());
      qInstance.addProcess(defineProcessGreetPeople());
      qInstance.addProcess(defineProcessGreetPeopleInteractive());
      qInstance.addProcess(defineProcessSimpleSleep());
      qInstance.addProcess(defineProcessScreenThenSleep());
      qInstance.addProcess(defineProcessSimpleThrow());
      qInstance.addReport(definePersonsReport());
      qInstance.addPossibleValueSource(definePossibleValueSourcePerson());
      defineWidgets(qInstance);

      qInstance.addBackend(defineMemoryBackend());
      try
      {
         new ScriptsMetaDataProvider().defineAll(qInstance, defineMemoryBackend().getName(), null);
      }
      catch(Exception e)
      {
         throw new IllegalStateException("Error adding script tables to instance");
      }

      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void defineWidgets(QInstance qInstance)
   {
      qInstance.addWidget(new QWidgetMetaData()
         .withName(PersonsByCreateDateBarChart.class.getSimpleName())
         .withCodeReference(new QCodeReference(PersonsByCreateDateBarChart.class, null)));
   }



   /*******************************************************************************
    ** Define the authentication used in standard tests - using 'mock' type.
    **
    *******************************************************************************/
   private static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("mock")
         .withType(QAuthenticationType.MOCK);
   }



   /*******************************************************************************
    ** Define the h2 rdbms backend
    **
    *******************************************************************************/
   public static RDBMSBackendMetaData defineDefaultH2Backend()
   {
      RDBMSBackendMetaData rdbmsBackendMetaData = new RDBMSBackendMetaData()
         .withVendor("h2")
         .withHostName("mem")
         .withDatabaseName("test_database")
         .withUsername("sa")
         .withPassword("");
      rdbmsBackendMetaData.setName("default");
      return (rdbmsBackendMetaData);
   }



   /*******************************************************************************
    ** Define the memory-only backend
    **
    *******************************************************************************/
   public static QBackendMetaData defineMemoryBackend()
   {
      return new QBackendMetaData()
         .withBackendType("memory")
         .withName(BACKEND_NAME_MEMORY);
   }



   /*******************************************************************************
    ** Define the person table
    **
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("firstName", "lastName")
         .withBackendName(defineDefaultH2Backend().getName())
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name"))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name"))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("partnerPersonId", QFieldType.INTEGER).withBackendName("partner_person_id").withPossibleValueSourceName(TABLE_NAME_PERSON))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("testScriptId", QFieldType.INTEGER).withBackendName("test_script_id"))
         .withField(new QFieldMetaData("photo", QFieldType.BLOB).withBackendName("photo"))
         .withField(new QFieldMetaData("photoFileName", QFieldType.STRING).withBackendName("photo_file_name"))
         .withAssociatedScript(new AssociatedScript()
            .withFieldName("testScriptId")
            .withScriptTypeId(1)
            .withScriptTester(new QCodeReference(TestScriptAction.class, QCodeUsage.SCRIPT_TESTER)));

      qTableMetaData.getField("photo")
         .withIsHeavy(true)
         .withFieldAdornment(new FieldAdornment(AdornmentType.FILE_DOWNLOAD)
            .withValue(AdornmentType.FileDownloadValues.DEFAULT_MIME_TYPE, "image")
            .withValue(AdornmentType.FileDownloadValues.FILE_NAME_FIELD, "photoFileName"));

      return (qTableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData definePersonJoinPartnerPerson()
   {
      return (new QJoinMetaData()
         .withLeftTable(TABLE_NAME_PERSON)
         .withRightTable(TABLE_NAME_PERSON)
         .withType(JoinType.MANY_TO_ONE)
         .withName("PersonJoinPartnerPerson")
         .withJoinOn(new JoinOn("partnerPersonId", "id")));
   }



   /*******************************************************************************
    ** Define the 'greet people' process
    *******************************************************************************/
   private static QProcessMetaData defineProcessGreetPeople()
   {
      return new QProcessMetaData()
         .withName("greet")
         .withTableName(TABLE_NAME_PERSON)
         .addStep(new QBackendStepMetaData()
            .withName("prepare")
            .withCode(new QCodeReference()
               .withName(MockBackendStep.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP)) // todo - needed, or implied in this context?
            .withInputData(new QFunctionInputMetaData()
               .withRecordListMetaData(new QRecordListMetaData().withTableName(TABLE_NAME_PERSON))
               .withFieldList(List.of(
                  new QFieldMetaData("greetingPrefix", QFieldType.STRING),
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING),
                  new QFieldMetaData("partnerPersonId", QFieldType.INTEGER).withBackendName("partner_person_id").withPossibleValueSourceName(TABLE_NAME_PERSON)
               )))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName(TABLE_NAME_PERSON)
                  .addField(new QFieldMetaData("fullGreeting", QFieldType.STRING))
               )
               .withFieldList(List.of(new QFieldMetaData("outputMessage", QFieldType.STRING))))
         );
   }



   /*******************************************************************************
    ** Define an interactive version of the 'greet people' process
    *******************************************************************************/
   private static QProcessMetaData defineProcessGreetPeopleInteractive()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_GREET_PEOPLE_INTERACTIVE)
         .withTableName(TABLE_NAME_PERSON)

         .addStep(new QFrontendStepMetaData()
            .withName("setup")
            .withFormField(new QFieldMetaData("greetingPrefix", QFieldType.STRING))
            .withFormField(new QFieldMetaData("greetingSuffix", QFieldType.STRING))
         )

         .addStep(new QBackendStepMetaData()
            .withName("doWork")
            .withCode(new QCodeReference()
               .withName(MockBackendStep.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP)) // todo - needed, or implied in this context?
            .withInputData(new QFunctionInputMetaData()
               .withRecordListMetaData(new QRecordListMetaData().withTableName(TABLE_NAME_PERSON))
               .withFieldList(List.of(
                  new QFieldMetaData("greetingPrefix", QFieldType.STRING),
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING)
               )))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName(TABLE_NAME_PERSON)
                  .addField(new QFieldMetaData("fullGreeting", QFieldType.STRING))
               )
               .withFieldList(List.of(new QFieldMetaData("outputMessage", QFieldType.STRING))))
         )

         .addStep(new QFrontendStepMetaData()
            .withName("results")
            .withFormField(new QFieldMetaData("outputMessage", QFieldType.STRING))
         );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QPossibleValueSource definePossibleValueSourcePerson()
   {
      return (new QPossibleValueSource()
         .withName(TABLE_NAME_PERSON)
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(TABLE_NAME_PERSON)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_PARENS_ID)
         .withOrderByField("id")
      );
   }



   /*******************************************************************************
    ** Define a process with just one step that sleeps
    *******************************************************************************/
   private static QProcessMetaData defineProcessSimpleSleep()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_SIMPLE_SLEEP)
         .withIsHidden(true)
         .addStep(SleeperStep.getMetaData());
   }



   /*******************************************************************************
    ** Define a process with a screen, then a sleep step
    *******************************************************************************/
   private static QProcessMetaData defineProcessScreenThenSleep()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_SLEEP_INTERACTIVE)
         .addStep(new QFrontendStepMetaData()
            .withName(SCREEN_0)
            .withFormField(new QFieldMetaData("outputMessage", QFieldType.STRING)))
         .addStep(SleeperStep.getMetaData())
         .addStep(new QFrontendStepMetaData()
            .withName(SCREEN_1)
            .withFormField(new QFieldMetaData("outputMessage", QFieldType.STRING)));
   }



   /*******************************************************************************
    ** Define a process with just one step that sleeps and then throws
    *******************************************************************************/
   private static QProcessMetaData defineProcessSimpleThrow()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_SIMPLE_THROW)
         .addStep(ThrowerStep.getMetaData());
   }



   /*******************************************************************************
    ** Testing backend step - just sleeps however long you ask it to (or, throws if
    ** you don't provide a number of seconds to sleep).
    *******************************************************************************/
   public static class SleeperStep implements BackendStep
   {
      public static final String FIELD_SLEEP_MILLIS = "sleepMillis";



      /*******************************************************************************
       ** Execute the backend step - using the request as input, and the result as output.
       **
       ******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         try
         {
            Thread.sleep(runBackendStepInput.getValueInteger(FIELD_SLEEP_MILLIS));
            runBackendStepOutput.addValue("didSleep", true);
         }
         catch(InterruptedException e)
         {
            throw (new QException("Interrupted while sleeping..."));
         }
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public static QBackendStepMetaData getMetaData()
      {
         return (new QBackendStepMetaData()
            .withName(STEP_NAME_SLEEPER)
            .withCode(new QCodeReference()
               .withName(SleeperStep.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP))
            .withInputData(new QFunctionInputMetaData()
               .addField(new QFieldMetaData(SleeperStep.FIELD_SLEEP_MILLIS, QFieldType.INTEGER))));
      }
   }



   /*******************************************************************************
    ** Testing backend step - just throws an exception after however long you ask it to sleep.
    *******************************************************************************/
   public static class ThrowerStep implements BackendStep
   {
      public static final String FIELD_SLEEP_MILLIS = "sleepMillis";



      /*******************************************************************************
       ** Execute the backend step - using the request as input, and the result as output.
       **
       ******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         int sleepMillis;
         try
         {
            sleepMillis = runBackendStepInput.getValueInteger(FIELD_SLEEP_MILLIS);
         }
         catch(QValueException qve)
         {
            sleepMillis = 50;
         }

         try
         {
            Thread.sleep(sleepMillis);
         }
         catch(InterruptedException e)
         {
            throw (new QException("Interrupted while sleeping..."));
         }

         throw (new QException("I always throw."));
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public static QBackendStepMetaData getMetaData()
      {
         return (new QBackendStepMetaData()
            .withName(STEP_NAME_THROWER)
            .withCode(new QCodeReference()
               .withName(ThrowerStep.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP))
            .withInputData(new QFunctionInputMetaData()
               .addField(new QFieldMetaData(ThrowerStep.FIELD_SLEEP_MILLIS, QFieldType.INTEGER))));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QReportMetaData definePersonsReport()
   {
      return new QReportMetaData()
         .withName("personsReport")
         .withInputField(new QFieldMetaData("firstNamePrefix", QFieldType.STRING))
         .withDataSource(new QReportDataSource()
            .withSourceTable(TABLE_NAME_PERSON)
            .withQueryFilter(new QQueryFilter(new QFilterCriteria("firstName", QCriteriaOperator.STARTS_WITH, "${input.firstNamePrefix}")))
         )
         .withView(new QReportView()
            .withType(ReportType.TABLE)
            .withColumns(List.of(
               new QReportField("id"),
               new QReportField("firstName"),
               new QReportField("lastName")
            ))
         );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertRecords(QInstance qInstance, QTableMetaData table, List<QRecord> records) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(table.getName());
      insertInput.setRecords(records);
      new InsertAction().execute(insertInput);
   }

}
