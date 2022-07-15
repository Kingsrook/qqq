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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.processes.implementations.mock.MockBackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.general.LoadInitialRecordsStep;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.Cardinality;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.RecordFormat;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class SampleMetaDataProvider
{
   public static final String MYSQL_BACKEND_NAME      = "mysql";
   public static final String FILESYSTEM_BACKEND_NAME = "filesystem";

   public static final String PROCESS_NAME_GREET             = "greet";
   public static final String PROCESS_NAME_GREET_INTERACTIVE = "greetInteractive";
   public static final String PROCESS_NAME_SIMPLE_SLEEP      = "simpleSleep";
   public static final String PROCESS_NAME_SIMPLE_THROW      = "simpleThrow";
   public static final String PROCESS_NAME_SLEEP_INTERACTIVE = "sleepInteractive";

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
      qInstance.addBackend(defineMysqlBackend());
      qInstance.addBackend(defineFilesystemBackend());
      qInstance.addTable(defineTableCarrier());
      qInstance.addTable(defineTablePerson());
      qInstance.addTable(defineTableCityFile());
      qInstance.addProcess(defineProcessGreetPeople());
      qInstance.addProcess(defineProcessGreetPeopleInteractive());
      qInstance.addProcess(defineProcessSimpleSleep());
      qInstance.addProcess(defineProcessScreenThenSleep());
      qInstance.addProcess(defineProcessSimpleThrow());

      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("Anonymous")
         .withType("fullyAnonymous");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendMetaData defineMysqlBackend()
   {
      return new RDBMSBackendMetaData()
         .withVendor("mysql")
         .withHostName("127.0.0.1")
         .withPort(3306)
         .withDatabaseName("qqq")
         .withUsername("root")
         .withPassword("")
         .withName(MYSQL_BACKEND_NAME);
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
      table.setName("carrier");
      table.setBackendName(MYSQL_BACKEND_NAME);
      table.setPrimaryKeyField("id");

      table.addField(new QFieldMetaData("id", QFieldType.INTEGER));

      table.addField(new QFieldMetaData("name", QFieldType.STRING));

      table.addField(new QFieldMetaData("company_code", QFieldType.STRING) // todo enum
         .withLabel("Company")
         .withBackendName("comp_code"));

      table.addField(new QFieldMetaData("service_level", QFieldType.STRING)); // todo enum

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName("person")
         .withLabel("Person")
         .withBackendName(MYSQL_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name").withIsRequired(true))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name").withIsRequired(true))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTableCityFile()
   {
      return new QTableMetaData()
         .withName("city")
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
         .withTableName("person")
         .withIsHidden(true)
         .addStep(new QBackendStepMetaData()
            .withName("prepare")
            .withCode(new QCodeReference()
               .withName(MockBackendStep.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP)) // todo - needed, or implied in this context?
            .withInputData(new QFunctionInputMetaData()
               .withRecordListMetaData(new QRecordListMetaData().withTableName("person"))
               .withFieldList(List.of(
                  new QFieldMetaData("greetingPrefix", QFieldType.STRING),
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING)
               )))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName("person")
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
         .withTableName("person")

         .addStep(LoadInitialRecordsStep.defineMetaData("person"))

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
               .withRecordListMetaData(new QRecordListMetaData().withTableName("person"))
               .withFieldList(List.of(
                  new QFieldMetaData("greetingPrefix", QFieldType.STRING),
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING)
               )))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName("person")
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
               .addField(new QFieldMetaData(ThrowerStep.FIELD_SLEEP_MILLIS, QFieldType.INTEGER))));
      }
   }



   public static class NoopBackendStep implements BackendStep
   {
      public NoopBackendStep()
      {

      }



      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         //////////
         // noop //
         //////////
      }
   }

}
