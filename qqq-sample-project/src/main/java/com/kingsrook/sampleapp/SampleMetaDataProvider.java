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

package com.kingsrook.sampleapp;


import java.util.List;
import java.util.Objects;
import com.amazonaws.regions.Regions;
import com.kingsrook.qqq.backend.core.actions.dashboard.QuickSightChartRenderer;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QuickSightChartMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.modules.authentication.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.general.LoadInitialRecordsStep;
import com.kingsrook.qqq.backend.core.processes.implementations.mock.MockBackendStep;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.Cardinality;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.RecordFormat;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.sampleapp.dashboard.widgets.PersonsByCreateDateBarChart;
import io.github.cdimascio.dotenv.Dotenv;


/*******************************************************************************
 **
 *******************************************************************************/
public class SampleMetaDataProvider
{
   public static boolean USE_MYSQL = true;

   public static final String RDBMS_BACKEND_NAME = "rdbms";
   public static final String FILESYSTEM_BACKEND_NAME = "filesystem";

   public static final String APP_NAME_GREETINGS = "greetingsApp";
   public static final String APP_NAME_PEOPLE = "peopleApp";
   public static final String APP_NAME_MISCELLANEOUS = "miscellaneous";

   public static final String PROCESS_NAME_GREET = "greet";
   public static final String PROCESS_NAME_GREET_INTERACTIVE = "greetInteractive";
   public static final String PROCESS_NAME_SIMPLE_SLEEP = "simpleSleep";
   public static final String PROCESS_NAME_SIMPLE_THROW = "simpleThrow";
   public static final String PROCESS_NAME_SLEEP_INTERACTIVE = "sleepInteractive";

   public static final String TABLE_NAME_PERSON = "person";
   public static final String TABLE_NAME_CARRIER = "carrier";
   public static final String TABLE_NAME_CITY = "city";

   public static final String STEP_NAME_SLEEPER = "sleeper";
   public static final String STEP_NAME_THROWER = "thrower";

   public static final String SCREEN_0 = "screen0";
   public static final String SCREEN_1 = "screen1";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance() throws QException
   {
      QInstance qInstance = new QInstance();

      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineRdbmsBackend());
      qInstance.addBackend(defineFilesystemBackend());
      qInstance.addTable(defineTableCarrier());
      qInstance.addTable(defineTablePerson());
      qInstance.addTable(defineTableCityFile());
      qInstance.addProcess(defineProcessGreetPeople());
      qInstance.addProcess(defineProcessGreetPeopleInteractive());
      qInstance.addProcess(defineProcessSimpleSleep());
      qInstance.addProcess(defineProcessScreenThenSleep());
      qInstance.addProcess(defineProcessSimpleThrow());

      defineWidgets(qInstance);

      defineApps(qInstance);

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

      Dotenv dotenv = Dotenv.configure()
         .ignoreIfMissing()
         .systemProperties()
         .load();
      QWidgetMetaDataInterface quickSightChartMetaData = new QuickSightChartMetaData()
         .withAccountId(System.getProperty("QUICKSIGHT_ACCCOUNT_ID"))
         .withAccessKey(System.getProperty("QUICKSIGHT_ACCESS_KEY"))
         .withSecretKey(System.getProperty("QUICKSIGHT_SECRET_KEY"))
         .withUserArn(System.getProperty("QUICKSIGHT_USER_ARN"))
         .withDashboardId("9e452e78-8509-4c81-bb7f-967abfc356da")
         .withRegion(Regions.US_EAST_2.getName())
         .withName(QuickSightChartRenderer.class.getSimpleName())
         .withLabel("Example Quicksight Chart")
         .withCodeReference(new QCodeReference(QuickSightChartRenderer.class, null));

      qInstance.addWidget(quickSightChartMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void defineApps(QInstance qInstance)
   {
      qInstance.addApp(new QAppMetaData()
         .withName(APP_NAME_GREETINGS)
         .withIcon(new QIcon().withName("emoji_people"))
         .withChild(qInstance.getProcess(PROCESS_NAME_GREET)
            .withIcon(new QIcon().withName("emoji_people")))
         .withChild(qInstance.getTable(TABLE_NAME_PERSON)
            .withIcon(new QIcon().withName("person")))
         .withChild(qInstance.getTable(TABLE_NAME_CITY)
            .withIcon(new QIcon().withName("location_city")))
         .withChild(qInstance.getProcess(PROCESS_NAME_GREET_INTERACTIVE))
         .withIcon(new QIcon().withName("waving_hand"))
         .withWidgets(List.of
            (
               PersonsByCreateDateBarChart.class.getSimpleName(),
               QuickSightChartRenderer.class.getSimpleName()
            ))
      );

      qInstance.addApp(new QAppMetaData()
         .withName(APP_NAME_PEOPLE)
         .withIcon(new QIcon().withName("person"))
         .withChild(qInstance.getApp(APP_NAME_GREETINGS)));

      qInstance.addApp(new QAppMetaData()
         .withName(APP_NAME_MISCELLANEOUS)
         .withIcon(new QIcon().withName("stars"))
         .withChild(qInstance.getTable(TABLE_NAME_CARRIER).withIcon(new QIcon("local_shipping")))
         .withChild(qInstance.getProcess(PROCESS_NAME_SIMPLE_SLEEP))
         .withChild(qInstance.getProcess(PROCESS_NAME_SLEEP_INTERACTIVE))
         .withChild(qInstance.getProcess(PROCESS_NAME_SIMPLE_THROW)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QAuthenticationMetaData defineAuthentication()
   {
      return (new QAuthenticationMetaData()
         .withName("mock")
         .withType(QAuthenticationType.MOCK));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static RDBMSBackendMetaData defineRdbmsBackend()
   {
      if(USE_MYSQL)
      {
         Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties()
            .load();
         return new RDBMSBackendMetaData()
            .withName(RDBMS_BACKEND_NAME)
            .withVendor(System.getProperty("RDBMS_VENDOR"))
            .withHostName(System.getProperty("RDBMS_HOSTNAME"))
            .withPort(Integer.valueOf(Objects.requireNonNull(System.getProperty("RDBMS_PORT"))))
            .withDatabaseName(System.getProperty("RDBMS_DATABASE_NAME"))
            .withUsername(System.getProperty("RDBMS_USERNAME"))
            .withPassword(System.getProperty("RDBMS_PASSWORD"));
      }
      else
      {
         return (new RDBMSBackendMetaData()
            .withName(RDBMS_BACKEND_NAME)
            .withVendor("h2")
            .withHostName("mem")
            .withDatabaseName("test_database")
            .withUsername("sa"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static FilesystemBackendMetaData defineFilesystemBackend()
   {
      return new FilesystemBackendMetaData()
         .withBasePath("/tmp/sample-filesystem")
         .withName(FILESYSTEM_BACKEND_NAME);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTableCarrier()
   {
      QTableMetaData table = new QTableMetaData();
      table.setName(TABLE_NAME_CARRIER);
      table.setBackendName(RDBMS_BACKEND_NAME);
      table.setPrimaryKeyField("id");
      table.setRecordLabelFormat("%s");
      table.setRecordLabelFields(List.of("name"));

      table.addField(new QFieldMetaData("id", QFieldType.INTEGER));

      table.addField(new QFieldMetaData("name", QFieldType.STRING)
         .withIsRequired(true));

      table.addField(new QFieldMetaData("company_code", QFieldType.STRING) // todo PVS
         .withLabel("Company")
         .withIsRequired(true)
         .withBackendName("company_code"));

      table.addField(new QFieldMetaData("service_level", QFieldType.STRING) // todo PVS
         .withLabel("Service Level")
         .withIsRequired(true));

      table.addSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1, List.of("id", "name")));
      table.addSection(new QFieldSection("basicInfo", "Basic Info", new QIcon("dataset"), Tier.T2, List.of("company_code", "service_level")));

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withBackendName(RDBMS_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("firstName", "lastName")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date").withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date").withIsEditable(false))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name").withIsRequired(true))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name").withIsRequired(true))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("isEmployed", QFieldType.BOOLEAN).withBackendName("is_employed"))
         .withField(new QFieldMetaData("annualSalary", QFieldType.DECIMAL).withBackendName("annual_salary").withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("daysWorked", QFieldType.INTEGER).withBackendName("days_worked").withDisplayFormat(DisplayFormat.COMMAS))

         .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1, List.of("id", "firstName", "lastName")))
         .withSection(new QFieldSection("basicInfo", "Basic Info", new QIcon("dataset"), Tier.T2, List.of("email", "birthDate")))
         .withSection(new QFieldSection("employmentInfo", "Employment Info", new QIcon("work"), Tier.T2, List.of("isEmployed", "annualSalary", "daysWorked")))
         .withSection(new QFieldSection("dates", "Dates", new QIcon("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));

      QInstanceEnricher.setInferredFieldBackendNames(qTableMetaData);

      return (qTableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTableCityFile()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_CITY)
         .withLabel("Cities")
         .withIsHidden(true)
         .withBackendName(FILESYSTEM_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("state", QFieldType.STRING)) // todo - state PVS.
         .withBackendDetails(new FilesystemTableBackendDetails()
            .withBasePath("cities")
            .withCardinality(Cardinality.MANY)
            .withRecordFormat(RecordFormat.CSV)
         );
   }



   /*******************************************************************************
    ** Define the 'greet people' process
    *******************************************************************************/
   private static QProcessMetaData defineProcessGreetPeople()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_GREET)
         .withLabel("Greet People")
         .withTableName(TABLE_NAME_PERSON)
         .withIsHidden(true)
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
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING)
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
         .withName(PROCESS_NAME_GREET_INTERACTIVE)
         .withTableName(TABLE_NAME_PERSON)

         .addStep(LoadInitialRecordsStep.defineMetaData(TABLE_NAME_PERSON))

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
            .withViewField(new QFieldMetaData("noOfPeopleGreeted", QFieldType.INTEGER))
            .withViewField(new QFieldMetaData("outputMessage", QFieldType.STRING))
            .withRecordListField(new QFieldMetaData("id", QFieldType.INTEGER))
            .withRecordListField(new QFieldMetaData("firstName", QFieldType.STRING))
            // .withRecordListField(new QFieldMetaData(MockBackendStep.FIELD_MOCK_VALUE, QFieldType.STRING))
            .withRecordListField(new QFieldMetaData("greetingMessage", QFieldType.STRING))
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
               .withField(new QFieldMetaData(ThrowerStep.FIELD_SLEEP_MILLIS, QFieldType.INTEGER))));
      }
   }

}
