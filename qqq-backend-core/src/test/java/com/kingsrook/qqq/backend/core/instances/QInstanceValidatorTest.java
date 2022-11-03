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

package com.kingsrook.qqq.backend.core.instances;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaDeleteStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for QInstanceValidator.
 **
 *******************************************************************************/
class QInstanceValidatorTest
{

   /*******************************************************************************
    ** Test a valid instance - should just pass
    **
    *******************************************************************************/
   @Test
   public void test_validatePass() throws QInstanceValidationException
   {
      new QInstanceValidator().validate(TestUtils.defineInstance());
   }



   /*******************************************************************************
    ** make sure we don't re-validate if already validated
    **
    *******************************************************************************/
   @Test
   public void test_doNotReValidate() throws QInstanceValidationException
   {
      QInstance qInstance = TestUtils.defineInstance();
      qInstance.setHasBeenValidated(new QInstanceValidationKey());
      qInstance.setBackends(null);
      new QInstanceValidator().validate(qInstance);
   }



   /*******************************************************************************
    ** Test an instance with null backends - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateNullBackends()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.setBackends(null),
         "At least 1 backend must be defined");
   }



   /*******************************************************************************
    ** Test an instance with empty map of backends - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateEmptyBackends()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.setBackends(new HashMap<>()),
         "At least 1 backend must be defined");
   }



   /*******************************************************************************
    ** Test an instance with null tables - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateNullTablesAndProcesses()
   {
      assertValidationFailureReasons((qInstance) ->
         {
            qInstance.setTables(null);
            qInstance.setProcesses(null);
         },
         true,
         "At least 1 table must be defined");
   }



   /*******************************************************************************
    ** Test an instance with empty map of tables - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateEmptyTablesAndProcesses()
   {
      assertValidationFailureReasons((qInstance) ->
         {
            qInstance.setTables(new HashMap<>());
            qInstance.setProcesses(new HashMap<>());
         },
         true,
         "At least 1 table must be defined");
   }



   /*******************************************************************************
    ** Test an instance where a table and a backend each have a name attribute that
    ** doesn't match the key that those objects have in the instance's maps - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateInconsistentNames()
   {
      assertValidationFailureReasonsAllowingExtraReasons((qInstance) ->
         {
            qInstance.getTable("person").setName("notPerson");
            qInstance.getBackend("default").setName("notDefault");
            qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).setName("notGreetPeople");
            qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_STATE).setName("notStates");
            qInstance.getAutomationProvider(TestUtils.POLLING_AUTOMATION).setName("notPolling");
         },
         "Inconsistent naming for table",
         "Inconsistent naming for backend",
         "Inconsistent naming for process",
         "Inconsistent naming for possibleValueSource",
         "Inconsistent naming for automationProvider"
      );
   }



   /*******************************************************************************
    ** Test that if a table has a null backend, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateTableWithoutBackend()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").setBackendName(null),
         "Missing backend name for table");
   }



   /*******************************************************************************
    ** Test that if a table specifies a backend that doesn't exist, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateTableWithMissingBackend()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").setBackendName("notARealBackend"),
         "Unrecognized backend");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_validateTableBadRecordFormatField()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withRecordLabelFields("notAField"),
         "not a field");
   }



   /*******************************************************************************
    ** Test that if a process specifies a table that doesn't exist, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateProcessWithMissingTable()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).setTableName("notATableName"),
         "Unrecognized table");
   }



   /*******************************************************************************
    ** Test that a process with a step that is a private class fails
    **
    *******************************************************************************/
   @Test
   public void test_validateProcessWithPrivateStep()
   {
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         ExtractViaQueryStep.class,
         TestPrivateClass.class,
         LoadViaDeleteStep.class,
         new HashMap<>()
      );
      process.setName("testProcess");
      process.setLabel("Test Process");
      process.setTableName(TestUtils.defineTablePerson().getName());

      assertValidationFailureReasons((qInstance) -> qInstance.addProcess(process),
         "is not public");
   }



   /*******************************************************************************
    ** Test that a process with a step that does not have a no-args constructor fails
    **
    *******************************************************************************/
   @Test
   public void test_validateProcessWithNoArgsConstructorStep()
   {
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         ExtractViaQueryStep.class,
         TestNoArgsConstructorClass.class,
         LoadViaDeleteStep.class,
         new HashMap<>()
      );
      process.setName("testProcess");
      process.setLabel("Test Process");
      process.setTableName(TestUtils.defineTablePerson().getName());

      assertValidationFailureReasons((qInstance) -> qInstance.addProcess(process),
         "parameterless constructor");
   }



   /*******************************************************************************
    ** Test that a process with a step that is an abstract class fails
    **
    *******************************************************************************/
   @Test
   public void test_validateProcessWithAbstractStep()
   {
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         ExtractViaQueryStep.class,
         TestAbstractClass.class,
         LoadViaDeleteStep.class,
         new HashMap<>()
      );
      process.setName("testProcess");
      process.setLabel("Test Process");
      process.setTableName(TestUtils.defineTablePerson().getName());

      assertValidationFailureReasons((qInstance) -> qInstance.addProcess(process),
         "because it is abstract");
   }



   /*******************************************************************************
    ** Test that a process with no steps fails
    **
    *******************************************************************************/
   @Test
   public void test_validateProcessWithNoSteps()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).setStepList(Collections.emptyList()),
         "At least 1 step");

      assertValidationFailureReasons((qInstance) -> qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).setStepList(null),
         "At least 1 step");
   }



   /*******************************************************************************
    ** Test that a process step with an empty string name fails
    **
    *******************************************************************************/
   @Test
   public void test_validateProcessStepWithEmptyName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).getStepList().get(0).setName(""),
         "Missing name for a step");

      assertValidationFailureReasons((qInstance) -> qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE).getStepList().get(1).setName(null),
         "Missing name for a step");
   }



   /*******************************************************************************
    ** Test that a table with no fields fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateTableWithNoFields()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").setFields(null),
         "At least 1 field");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").setFields(new HashMap<>()),
         "At least 1 field");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableCustomizers()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference()),
         "missing a code reference name", "missing a code type");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(null, QCodeType.JAVA, null)),
         "missing a code reference name");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference("", QCodeType.JAVA, null)),
         "missing a code reference name");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference("Test", null, null)),
         "missing a code type");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference("Test", QCodeType.JAVA, QCodeUsage.CUSTOMIZER)),
         "Class for Test could not be found");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(CustomizerWithNoVoidConstructor.class, QCodeUsage.CUSTOMIZER)),
         "Instance of " + CustomizerWithNoVoidConstructor.class.getSimpleName() + " could not be created");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(CustomizerThatIsNotAFunction.class, QCodeUsage.CUSTOMIZER)),
         "CodeReference is not of the expected type");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(CustomizerFunctionWithIncorrectTypeParameters.class, QCodeUsage.CUSTOMIZER)),
         "Error validating customizer type parameters");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(CustomizerFunctionWithIncorrectTypeParameter1.class, QCodeUsage.CUSTOMIZER)),
         "Error validating customizer type parameters");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(CustomizerFunctionWithIncorrectTypeParameter2.class, QCodeUsage.CUSTOMIZER)),
         "Error validating customizer type parameters");

      assertValidationSuccess((qInstance) -> qInstance.getTable("person").withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(CustomizerValid.class, QCodeUsage.CUSTOMIZER)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerWithNoVoidConstructor
   {
      public CustomizerWithNoVoidConstructor(boolean b)
      {

      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerWithOnlyPrivateConstructor
   {
      private CustomizerWithOnlyPrivateConstructor()
      {

      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerThatIsNotAFunction
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerFunctionWithIncorrectTypeParameters implements Function<String, String>
   {
      @Override
      public String apply(String s)
      {
         return null;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerFunctionWithIncorrectTypeParameter1 implements Function<String, QRecord>
   {
      @Override
      public QRecord apply(String s)
      {
         return null;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerFunctionWithIncorrectTypeParameter2 implements Function<QRecord, String>
   {
      @Override
      public String apply(QRecord s)
      {
         return "Test";
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CustomizerValid implements Function<QRecord, QRecord>
   {
      @Override
      public QRecord apply(QRecord record)
      {
         return null;
      }
   }



   /*******************************************************************************
    ** Test that if a field specifies a backend that doesn't exist, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateFieldWithMissingPossibleValueSource()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").getField("homeStateId").setPossibleValueSourceName("not a real possible value source"),
         "Unrecognized possibleValueSourceName");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testChildrenWithBadParentAppName()
   {
      String[] reasons = new String[] { "Unrecognized parent app", "does not have its parent app properly set" };
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON).setParentAppName("notAnApp"), reasons);
      assertValidationFailureReasons((qInstance) -> qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).setParentAppName("notAnApp"), reasons);
      assertValidationFailureReasons((qInstance) -> qInstance.getApp(TestUtils.APP_NAME_GREETINGS).setParentAppName("notAnApp"), reasons);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppCircularReferences()
   {
      assertValidationFailureReasonsAllowingExtraReasons((qInstance) ->
      {
         QAppMetaData miscApp      = qInstance.getApp(TestUtils.APP_NAME_MISCELLANEOUS);
         QAppMetaData greetingsApp = qInstance.getApp(TestUtils.APP_NAME_GREETINGS);

         miscApp.withChild(greetingsApp);
         greetingsApp.withChild(miscApp);
      }, "Circular app reference");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionsMissingName()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection(null, "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "Missing a name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionsMissingLabel()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", null, new QIcon("person"), Tier.T1, List.of("id")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationSuccess((qInstance) -> qInstance.addTable(table));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionDuplicateName()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("section1", "Section 2", new QIcon("person"), Tier.T2, List.of("name")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "more than 1 section named");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionDuplicateLabel()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("section2", "Section 1", new QIcon("person"), Tier.T2, List.of("name")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "more than 1 section labeled");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionsNoFields()
   {
      QTableMetaData table1 = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of()))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table1), "section1 does not have any fields", "field id is not listed in any field sections");

      QTableMetaData table2 = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, null))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table2), "section1 does not have any fields", "field id is not listed in any field sections");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionsUnrecognizedFieldName()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id", "od")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "not a field on this table");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionsDuplicatedFieldName()
   {
      QTableMetaData table1 = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id", "id")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table1), "more than once");

      QTableMetaData table2 = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("section2", "Section 2", new QIcon("person"), Tier.T2, List.of("id")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table2), "more than once");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldNotInAnySections()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "not listed in any field sections");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldSectionsMultipleTier1()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("section2", "Section 2", new QIcon("person"), Tier.T1, List.of("name")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "more than 1 section listed as Tier 1");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppSectionsMissingName()
   {
      QAppMetaData app = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection(null, "Section 1", new QIcon("person"), List.of("test"), null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app), "Missing a name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppSectionsMissingLabel()
   {
      ///////////////////////////////////////////////////////////////////////////////////
      // the enricher makes a label from the name, so, we'll just make them both null. //
      ///////////////////////////////////////////////////////////////////////////////////
      QAppMetaData app = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection(null, null, new QIcon("person"), List.of("test"), null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app), "Missing a label", "Missing a name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppSectionsNoFields()
   {
      QAppMetaData app1 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of(), List.of(), null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app1), "section1 does not have any children", "child test is not listed in any app sections");

      QAppMetaData app2 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), null, null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app2), "section1 does not have any children", "child test is not listed in any app sections");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppSectionsUnrecognizedFieldName()
   {
      QAppMetaData app1 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of("test", "tset"), null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app1), "not a child of this app");
      QAppMetaData app2 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of("test"), List.of("tset"), null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app2), "not a child of this app");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppSectionsDuplicatedFieldName()
   {
      QAppMetaData app1 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of("test", "test"), null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app1), "more than once");

      QAppMetaData app2 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of("test"), null, null))
         .withSection(new QAppSection("section2", "Section 2", new QIcon("person"), List.of("test"), null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app2), "more than once");

      QAppMetaData app3 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of("test"), List.of("test"), null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app3), "more than once");

      QAppMetaData app4 = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), null, List.of("test", "test"), null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app4), "more than once");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testChildNotInAnySections()
   {
      QTableMetaData table = new QTableMetaData().withName("test")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withSection(new QFieldSection("section1", "Section 1", new QIcon("person"), Tier.T1, List.of("id")))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "not listed in any field sections");

      QAppMetaData app = new QAppMetaData().withName("test")
         .withChild(new QTableMetaData().withName("tset"))
         .withChild(new QTableMetaData().withName("test"))
         .withSection(new QAppSection("section1", "Section 1", new QIcon("person"), List.of("test"), null, null));
      assertValidationFailureReasons((qInstance) -> qInstance.addApp(app), "not listed in any app sections");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueSourceMissingType()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_STATE).setType(null),
         "Missing type for possibleValueSource");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueSourceMisConfiguredEnum()
   {
      assertValidationFailureReasons((qInstance) -> {
            QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_STATE);
            possibleValueSource.setTableName("person");
            possibleValueSource.setSearchFields(List.of("id"));
            possibleValueSource.setOrderByFields(List.of(new QFilterOrderBy("id")));
            possibleValueSource.setCustomCodeReference(new QCodeReference());
            possibleValueSource.setEnumValues(null);
            possibleValueSource.setType(QPossibleValueSourceType.ENUM);
         },
         "should not have a tableName",
         "should not have searchFields",
         "should not have orderByFields",
         "should not have a customCodeReference",
         "is missing enum values");

      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_STATE).setEnumValues(new ArrayList<>()),
         "is missing enum values");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueSourceMisConfiguredTable()
   {
      assertValidationFailureReasons((qInstance) -> {
            QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE);
            possibleValueSource.setTableName(null);
            possibleValueSource.setSearchFields(null);
            possibleValueSource.setOrderByFields(new ArrayList<>());
            possibleValueSource.setCustomCodeReference(new QCodeReference());
            possibleValueSource.setEnumValues(List.of(new QPossibleValue<>("test")));
            possibleValueSource.setType(QPossibleValueSourceType.TABLE);
         },
         "should not have enum values",
         "should not have a customCodeReference",
         "is missing a tableName",
         "is missing searchFields",
         "is missing orderByFields");

      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE).setTableName("Not a table"),
         "Unrecognized table");

      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE).setSearchFields(List.of("id", "notAField", "name")),
         "unrecognized searchField: notAField");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueSourceMisConfiguredCustom()
   {
      assertValidationFailureReasons((qInstance) -> {
            QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_CUSTOM);
            possibleValueSource.setTableName("person");
            possibleValueSource.setSearchFields(List.of("id"));
            possibleValueSource.setOrderByFields(List.of(new QFilterOrderBy("id")));
            possibleValueSource.setCustomCodeReference(null);
            possibleValueSource.setEnumValues(List.of(new QPossibleValue<>("test")));
            possibleValueSource.setType(QPossibleValueSourceType.CUSTOM);
         },
         "should not have enum values",
         "should not have a tableName",
         "should not have searchFields",
         "should not have orderByFields",
         "is missing a customCodeReference");

      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_CUSTOM).setCustomCodeReference(new QCodeReference()),
         "not a possibleValueProvider",
         "missing a code reference name",
         "missing a code type");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAutomationProviderType()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getAutomationProvider(TestUtils.POLLING_AUTOMATION).setType(null),
         "Missing type for automationProvider");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationProviderName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().setProviderName(null),
         "is missing a providerName");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().setProviderName(""),
         "is missing a providerName");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().setProviderName("notARealProvider"),
         "unrecognized providerName");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationStatusTracking()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().setStatusTracking(null),
         "do not have statusTracking");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationStatusTrackingType()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().getStatusTracking().setType(null),
         "statusTracking is missing a type");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationStatusTrackingFieldName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().getStatusTracking().setFieldName(null),
         "missing its fieldName");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().getStatusTracking().setFieldName(""),
         "missing its fieldName");

      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().getStatusTracking().setFieldName("notARealField"),
         "not a defined field");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationActionsNames()
   {
      assertValidationFailureReasons((qInstance) -> getAction0(qInstance).setName(null),
         "action missing a name");

      assertValidationFailureReasons((qInstance) -> getAction0(qInstance).setName(""),
         "action missing a name");

      assertValidationFailureReasons((qInstance) ->
         {
            List<TableAutomationAction> actions = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().getActions();
            actions.add(actions.get(0));
         },
         "more than one action named");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationActionTriggerEvent()
   {
      assertValidationFailureReasons((qInstance) -> getAction0(qInstance).setTriggerEvent(null),
         "missing a triggerEvent");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationActionCodeReference()
   {
      assertValidationFailureReasons((qInstance) -> getAction0(qInstance).setCodeReference(new QCodeReference()),
         "missing a code reference name", "missing a code type");

      assertValidationFailureReasons((qInstance) -> getAction0(qInstance).setCodeReference(new QCodeReference(TestUtils.CustomPossibleValueSource.class)),
         "is not of the expected type");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationActionProcessName()
   {
      assertValidationFailureReasons((qInstance) ->
         {
            TableAutomationAction action = getAction0(qInstance);
            action.setCodeReference(null);
            action.setProcessName("notAProcess");
         },
         "unrecognized processName");

      assertValidationFailureReasons((qInstance) ->
         {
            TableAutomationAction action = getAction0(qInstance);
            action.setCodeReference(null);
            action.setProcessName(TestUtils.PROCESS_NAME_GREET_PEOPLE);
         },
         "different table");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationActionCodeReferenceAndProcessName()
   {
      assertValidationFailureReasons((qInstance) ->
         {
            TableAutomationAction action = getAction0(qInstance);
            action.setCodeReference(null);
            action.setProcessName(null);
         },
         "missing both");

      assertValidationFailureReasons((qInstance) ->
         {
            TableAutomationAction action = getAction0(qInstance);
            action.setCodeReference(new QCodeReference(TestUtils.CheckAge.class));
            action.setProcessName(TestUtils.PROCESS_NAME_INCREASE_BIRTHDATE);
         },
         "has both");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableAutomationActionFilter()
   {
      assertValidationFailureReasons((qInstance) ->
         {
            TableAutomationAction action = getAction0(qInstance);
            action.setFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria())
            );
         },
         "without a field name", "without an operator");

      assertValidationFailureReasons((qInstance) ->
         {
            TableAutomationAction action = getAction0(qInstance);
            action.setFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("notAField", QCriteriaOperator.EQUALS, Collections.emptyList()))
            );
         },
         "unrecognized field");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeyNoFields()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).withUniqueKey(new UniqueKey()),
         "uniqueKey with no fields");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeyDuplicatedField()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).withUniqueKey(new UniqueKey().withFieldName("id").withFieldName("id")),
         "the same field multiple times");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeyInvalidField()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).withUniqueKey(new UniqueKey().withFieldName("notAField")),
         "unrecognized field name: notAField");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeyDuplicatedUniqueKeys()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
            .withUniqueKey(new UniqueKey().withFieldName("id"))
            .withUniqueKey(new UniqueKey().withFieldName("id")),
         "more than one uniqueKey with the same set of fields");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidUniqueKeys()
   {
      assertValidationSuccess((qInstance) -> qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withUniqueKey(new UniqueKey().withFieldName("id"))
         .withUniqueKey(new UniqueKey().withFieldName("firstName").withFieldName("lastName")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueProviderName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getQueueProvider(TestUtils.DEFAULT_QUEUE_PROVIDER).withName(null),
         "Inconsistent naming for queueProvider");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueueProvider(TestUtils.DEFAULT_QUEUE_PROVIDER).withName(""),
         "Inconsistent naming for queueProvider");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueueProvider(TestUtils.DEFAULT_QUEUE_PROVIDER).withName("wrongName"),
         "Inconsistent naming for queueProvider");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueProviderType()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getQueueProvider(TestUtils.DEFAULT_QUEUE_PROVIDER).withType(null),
         "Missing type for queueProvider");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueProviderSQSAttributes()
   {
      assertValidationFailureReasons((qInstance) ->
         {
            SQSQueueProviderMetaData queueProvider = (SQSQueueProviderMetaData) qInstance.getQueueProvider(TestUtils.DEFAULT_QUEUE_PROVIDER);
            queueProvider.setAccessKey(null);
            queueProvider.setSecretKey("");
            queueProvider.setRegion(null);
            queueProvider.setBaseURL("");
         },
         "Missing accessKey", "Missing secretKey", "Missing region", "Missing baseURL");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withName(null),
         "Inconsistent naming for queue");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withName(""),
         "Inconsistent naming for queue");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withName("wrongName"),
         "Inconsistent naming for queue");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueQueueProviderName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withProviderName(null),
         "Unrecognized queue providerName");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withProviderName(""),
         "Unrecognized queue providerName");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withProviderName("wrongName"),
         "Unrecognized queue providerName");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueQueueName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withQueueName(null),
         "Missing queueName for queue");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withQueueName(""),
         "Missing queueName for queue");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueueProcessName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withProcessName(null),
         "Missing processName for queue");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withProcessName(""),
         "Missing processName for queue");

      assertValidationFailureReasons((qInstance) -> qInstance.getQueue("testSQSQueue").withProcessName("notAProcess"),
         "Unrecognized processName for queue:");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReportName()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).withName(null),
         "Inconsistent naming for report");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).withName(""),
         "Inconsistent naming for report");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).withName("wrongName"),
         "Inconsistent naming for report");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReportNoDataSources()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).withDataSources(null),
         "At least 1 data source",
         "unrecognized dataSourceName");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).withDataSources(new ArrayList<>()),
         "At least 1 data source",
         "unrecognized dataSourceName");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReportDataSourceNames()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).setName(null),
         "Missing name for a dataSource",
         "unrecognized dataSourceName");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).setName(""),
         "Missing name for a dataSource",
         "unrecognized dataSourceName");

      assertValidationFailureReasons((qInstance) ->
         {
            List<QReportDataSource> dataSources = new ArrayList<>(qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources());
            dataSources.add(dataSources.get(0));
            qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).setDataSources(dataSources);
         },
         "More than one dataSource with name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReportDataSourceTables()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).setSourceTable("notATable"),
         "is not a table in this instance");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).setSourceTable(null),
         "does not have a sourceTable");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).setSourceTable(""),
         "does not have a sourceTable");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReportDataSourceTablesFilter()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).getQueryFilter().getCriteria().get(0).setFieldName(null),
         "Missing fieldName for a criteria");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).getQueryFilter().getCriteria().get(0).setFieldName("notAField"),
         "is not a field in this table");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).getQueryFilter().getCriteria().get(0).setOperator(null),
         "Missing operator for a criteria");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).getQueryFilter().withOrderBy(new QFilterOrderBy(null)),
         "Missing fieldName for an orderBy");

      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).getQueryFilter().withOrderBy(new QFilterOrderBy("notAField")),
         "is not a field in this table");

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReportDataSourceStaticDataSupplier()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0).withStaticDataSupplier(new QCodeReference()),
         "has both a sourceTable and a staticDataSupplier");

      assertValidationFailureReasons((qInstance) ->
         {
            QReportDataSource dataSource = qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0);
            dataSource.setSourceTable(null);
            dataSource.setStaticDataSupplier(new QCodeReference(null, QCodeType.JAVA, null));
         },
         "missing a code reference name");

      assertValidationFailureReasons((qInstance) ->
         {
            QReportDataSource dataSource = qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON).getDataSources().get(0);
            dataSource.setSourceTable(null);
            dataSource.setStaticDataSupplier(new QCodeReference(ArrayList.class, null));
         },
         "is not of the expected type");

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private TableAutomationAction getAction0(QInstance qInstance)
   {
      return qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().getActions().get(0);
   }



   /*******************************************************************************
    ** Run a little setup code on a qInstance; then validate it, and assert that it
    ** failed validation with reasons that match the supplied vararg-reasons (but allow
    ** more reasons - e.g., helpful when one thing we're testing causes other errors).
    *******************************************************************************/
   private void assertValidationFailureReasonsAllowingExtraReasons(Consumer<QInstance> setup, String... reasons)
   {
      assertValidationFailureReasons(setup, true, reasons);
   }



   /*******************************************************************************
    ** Run a little setup code on a qInstance; then validate it, and assert that it
    ** failed validation with reasons that match the supplied vararg-reasons (and
    ** require that exact # of reasons).
    *******************************************************************************/
   private void assertValidationFailureReasons(Consumer<QInstance> setup, String... reasons)
   {
      assertValidationFailureReasons(setup, false, reasons);
   }



   /*******************************************************************************
    ** Implementation for the overloads of this name.
    *******************************************************************************/
   private void assertValidationFailureReasons(Consumer<QInstance> setup, boolean allowExtraReasons, String... reasons)
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         setup.accept(qInstance);
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         if(!allowExtraReasons)
         {
            int noOfReasons = e.getReasons() == null ? 0 : e.getReasons().size();
            assertEquals(reasons.length, noOfReasons, "Expected number of validation failure reasons.\nExpected reasons: " + String.join(",", reasons)
               + "\nActual reasons: " + (noOfReasons > 0 ? String.join("\n", e.getReasons()) : "--"));
         }

         for(String reason : reasons)
         {
            assertReason(reason, e);
         }
      }
   }



   /*******************************************************************************
    ** Assert that an instance is valid!
    *******************************************************************************/
   private void assertValidationSuccess(Consumer<QInstance> setup)
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         setup.accept(qInstance);
         new QInstanceValidator().validate(qInstance);
      }
      catch(QInstanceValidationException e)
      {
         fail("Expected no validation errors, but received: " + e.getMessage());
      }
   }



   /*******************************************************************************
    ** utility method for asserting that a specific reason string is found within
    ** the list of reasons in the QInstanceValidationException.
    **
    *******************************************************************************/
   private void assertReason(String reason, QInstanceValidationException e)
   {
      assertNotNull(e.getReasons(), "Expected there to be a reason for the failure (but there was not)");
      assertThat(e.getReasons())
         .withFailMessage("Expected any of:\n%s\nTo match: [%s]", e.getReasons(), reason)
         .anyMatch(s -> s.contains(reason));
   }



   ///////////////////////////////////////////////
   // test classes for validating process steps //
   ///////////////////////////////////////////////
   public abstract class TestAbstractClass extends AbstractTransformStep implements BackendStep
   {
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
      }
   }



   ///////////////////////////////////////////////
   //                                           //
   ///////////////////////////////////////////////
   private class TestPrivateClass extends AbstractTransformStep implements BackendStep
   {
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
      }



      @Override
      public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
      {
         return null;
      }
   }



   ///////////////////////////////////////////////
   //                                           //
   ///////////////////////////////////////////////
   public class TestNoArgsConstructorClass extends AbstractTransformStep implements BackendStep
   {
      public TestNoArgsConstructorClass(int i)
      {

      }



      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
      }



      @Override
      public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
      {
         return null;
      }
   }
}

