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

package com.kingsrook.qqq.backend.core.utils;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.person.addtopeoplesage.AddAge;
import com.kingsrook.qqq.backend.core.actions.processes.person.addtopeoplesage.GetAgeStatistics;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.MockAuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.authentication.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.mock.MockBackendModule;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.mock.MockBackendStep;


/*******************************************************************************
 ** Utility class for backend-core test classes
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String DEFAULT_BACKEND_NAME = "default";
   public static final String MEMORY_BACKEND_NAME  = "memory";

   public static final String APP_NAME_GREETINGS     = "greetingsApp";
   public static final String APP_NAME_PEOPLE        = "peopleApp";
   public static final String APP_NAME_MISCELLANEOUS = "miscellaneous";

   public static final String TABLE_NAME_PERSON = "person";
   public static final String TABLE_NAME_SHAPE  = "shape";

   public static final String PROCESS_NAME_GREET_PEOPLE             = "greet";
   public static final String PROCESS_NAME_GREET_PEOPLE_INTERACTIVE = "greetInteractive";
   public static final String PROCESS_NAME_ADD_TO_PEOPLES_AGE       = "addToPeoplesAge";
   public static final String TABLE_NAME_PERSON_FILE                = "personFile";
   public static final String TABLE_NAME_ID_AND_NAME_ONLY           = "idAndNameOnly";

   public static final String POSSIBLE_VALUE_SOURCE_STATE  = "state"; // enum-type
   public static final String POSSIBLE_VALUE_SOURCE_SHAPE  = "shape"; // table-type
   public static final String POSSIBLE_VALUE_SOURCE_CUSTOM = "custom"; // custom-type



   /*******************************************************************************
    ** Define the instance used in standard tests.
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineBackend());
      qInstance.addBackend(defineMemoryBackend());

      qInstance.addTable(defineTablePerson());
      qInstance.addTable(definePersonFileTable());
      qInstance.addTable(defineTableIdAndNameOnly());
      qInstance.addTable(defineTableShape());

      qInstance.addPossibleValueSource(defineStatesPossibleValueSource());
      qInstance.addPossibleValueSource(defineShapePossibleValueSource());
      qInstance.addPossibleValueSource(defineCustomPossibleValueSource());

      qInstance.addProcess(defineProcessGreetPeople());
      qInstance.addProcess(defineProcessGreetPeopleInteractive());
      qInstance.addProcess(defineProcessAddToPeoplesAge());
      qInstance.addProcess(new BasicETLProcess().defineProcessMetaData());
      qInstance.addProcess(new StreamedETLProcess().defineProcessMetaData());

      defineApps(qInstance);

      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void defineApps(QInstance qInstance)
   {
      qInstance.addApp(new QAppMetaData()
         .withName(APP_NAME_GREETINGS)
         .withChild(qInstance.getProcess(PROCESS_NAME_GREET_PEOPLE))
         .withChild(qInstance.getProcess(PROCESS_NAME_GREET_PEOPLE_INTERACTIVE)));

      qInstance.addApp(new QAppMetaData()
         .withName(APP_NAME_PEOPLE)
         .withChild(qInstance.getTable(TABLE_NAME_PERSON))
         .withChild(qInstance.getTable(TABLE_NAME_PERSON_FILE))
         .withChild(qInstance.getApp(APP_NAME_GREETINGS)));

      qInstance.addApp(new QAppMetaData()
         .withName(APP_NAME_MISCELLANEOUS)
         .withChild(qInstance.getTable(TABLE_NAME_ID_AND_NAME_ONLY))
         .withChild(qInstance.getProcess(BasicETLProcess.PROCESS_NAME)));
   }



   /*******************************************************************************
    ** Define the "states" possible value source used in standard tests
    **
    *******************************************************************************/
   private static QPossibleValueSource defineStatesPossibleValueSource()
   {
      return new QPossibleValueSource()
         .withName(POSSIBLE_VALUE_SOURCE_STATE)
         .withType(QPossibleValueSourceType.ENUM)
         .withEnumValues(List.of(new QPossibleValue<>(1, "IL"), new QPossibleValue<>(2, "MO")));
   }



   /*******************************************************************************
    ** Define the "shape" possible value source used in standard tests
    **
    *******************************************************************************/
   private static QPossibleValueSource defineShapePossibleValueSource()
   {
      return new QPossibleValueSource()
         .withName(POSSIBLE_VALUE_SOURCE_SHAPE)
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(TABLE_NAME_SHAPE);
   }



   /*******************************************************************************
    ** Define the "custom" possible value source used in standard tests
    **
    *******************************************************************************/
   private static QPossibleValueSource defineCustomPossibleValueSource()
   {
      return new QPossibleValueSource()
         .withName(POSSIBLE_VALUE_SOURCE_CUSTOM)
         .withType(QPossibleValueSourceType.CUSTOM)
         .withCustomCodeReference(new QCodeReference(CustomPossibleValueSource.class));
   }



   /*******************************************************************************
    ** Define the authentication used in standard tests - using 'mock' type.
    **
    *******************************************************************************/
   public static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("mock")
         .withType(QAuthenticationType.MOCK);
   }



   /*******************************************************************************
    ** Define the backend used in standard tests - using 'mock' type.
    *******************************************************************************/
   public static QBackendMetaData defineBackend()
   {
      return new QBackendMetaData()
         .withName(DEFAULT_BACKEND_NAME)
         .withBackendType(MockBackendModule.class);
   }



   /*******************************************************************************
    ** Define the in-memory backend used in standard tests
    *******************************************************************************/
   public static QBackendMetaData defineMemoryBackend()
   {
      return new QBackendMetaData()
         .withName(MEMORY_BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class);
   }



   /*******************************************************************************
    ** Define the 'person' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withBackendName(DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("homeStateId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_STATE))
         .withField(new QFieldMetaData("favoriteShapeId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_SHAPE))
         .withField(new QFieldMetaData("customValue", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_CUSTOM))
         ;
   }



   /*******************************************************************************
    ** Define the 'shape' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableShape()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_SHAPE)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withRecordLabelFields("name")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("type", QFieldType.STRING)) // todo PVS
         .withField(new QFieldMetaData("noOfSides", QFieldType.INTEGER))
         .withField(new QFieldMetaData("isPolygon", QFieldType.BOOLEAN)) // mmm, should be derived from type, no?
         ;
   }



   /*******************************************************************************
    ** Define a 2nd version of the 'person' table for this test (pretend it's backed by a file)
    *******************************************************************************/
   public static QTableMetaData definePersonFileTable()
   {
      return (new QTableMetaData()
         .withName(TABLE_NAME_PERSON_FILE)
         .withLabel("Person File")
         .withBackendName(DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withFields(TestUtils.defineTablePerson().getFields()));
   }



   /*******************************************************************************
    ** Define simple table with just an id and name
    *******************************************************************************/
   public static QTableMetaData defineTableIdAndNameOnly()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_ID_AND_NAME_ONLY)
         .withLabel("Id and Name Only")
         .withBackendName(DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
   }



   /*******************************************************************************
    ** Define the 'greet people' process
    *******************************************************************************/
   private static QProcessMetaData defineProcessGreetPeople()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_GREET_PEOPLE)
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
                  new QFieldMetaData("greetingSuffix", QFieldType.STRING)
               )))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName(TABLE_NAME_PERSON)
                  .withField(new QFieldMetaData("fullGreeting", QFieldType.STRING))
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
                  .withField(new QFieldMetaData("fullGreeting", QFieldType.STRING))
               )
               .withFieldList(List.of(new QFieldMetaData("outputMessage", QFieldType.STRING))))
         )

         .addStep(new QFrontendStepMetaData()
            .withName("results")
            .withFormField(new QFieldMetaData("outputMessage", QFieldType.STRING))
         );
   }



   /*******************************************************************************
    ** Define the "add to people's age" process
    **
    ** Works on a list of rows from the person table.
    ** - first function reports the current min & max age for all input rows.
    ** - user is then prompted for how much they want to add to everyone.
    ** - then the second function adds that value to their age, and shows the results.
    *******************************************************************************/
   private static QProcessMetaData defineProcessAddToPeoplesAge()
   {
      return new QProcessMetaData()
         .withName(PROCESS_NAME_ADD_TO_PEOPLES_AGE)
         .withTableName(TABLE_NAME_PERSON)
         .addStep(new QBackendStepMetaData()
            .withName("getAgeStatistics")
            .withCode(new QCodeReference()
               .withName(GetAgeStatistics.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP))
            .withInputData(new QFunctionInputMetaData()
               .withRecordListMetaData(new QRecordListMetaData().withTableName(TABLE_NAME_PERSON)))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName(TABLE_NAME_PERSON)
                  .withField(new QFieldMetaData("age", QFieldType.INTEGER)))
               .withFieldList(List.of(
                  new QFieldMetaData("minAge", QFieldType.INTEGER),
                  new QFieldMetaData("maxAge", QFieldType.INTEGER)))))
         .addStep(new QBackendStepMetaData()
            .withName("addAge")
            .withCode(new QCodeReference()
               .withName(AddAge.class.getName())
               .withCodeType(QCodeType.JAVA)
               .withCodeUsage(QCodeUsage.BACKEND_STEP))
            .withInputData(new QFunctionInputMetaData()
               .withFieldList(List.of(new QFieldMetaData("yearsToAdd", QFieldType.INTEGER))))
            .withOutputMetaData(new QFunctionOutputMetaData()
               .withRecordListMetaData(new QRecordListMetaData()
                  .withTableName(TABLE_NAME_PERSON)
                  .withField(new QFieldMetaData("newAge", QFieldType.INTEGER)))));

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QSession getMockSession()
   {
      MockAuthenticationModule mockAuthenticationModule = new MockAuthenticationModule();
      return (mockAuthenticationModule.createSession(null, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecord> queryTable(String tableName) throws QException
   {
      return (queryTable(TestUtils.defineInstance(), tableName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecord> queryTable(QInstance instance, String tableName) throws QException
   {
      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(TestUtils.getMockSession());
      queryInput.setTableName(tableName);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (queryOutput.getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertDefaultShapes(QInstance qInstance) throws QException
   {
      List<QRecord> shapeRecords = List.of(
         new QRecord().withTableName(TABLE_NAME_SHAPE).withValue("id", 1).withValue("name", "Triangle"),
         new QRecord().withTableName(TABLE_NAME_SHAPE).withValue("id", 2).withValue("name", "Square"),
         new QRecord().withTableName(TABLE_NAME_SHAPE).withValue("id", 3).withValue("name", "Circle"));

      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TABLE_NAME_SHAPE);
      insertInput.setRecords(shapeRecords);
      new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvHeader()
   {
      return ("""
         "id","createDate","modifyDate","firstName","lastName","birthDate","email"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvHeaderUsingLabels()
   {
      return ("""
         "Id","Create Date","Modify Date","First Name","Last Name","Birth Date","Email"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvRow1()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","John","Doe","1980-01-01","john@doe.com"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvRow2()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","Jane","Doe","1981-01-01","john@doe.com"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomPossibleValueSource implements QCustomPossibleValueProvider
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public QPossibleValue<?> getPossibleValue(Serializable idValue)
      {
         return (new QPossibleValue<>(idValue, "Custom[" + idValue + "]"));
      }
   }
}
