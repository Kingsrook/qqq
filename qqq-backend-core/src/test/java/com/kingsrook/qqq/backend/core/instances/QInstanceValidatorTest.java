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
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
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
   public void test_validateNullTables()
   {
      assertValidationFailureReasonsAllowingExtraReasons((qInstance) ->
         {
            qInstance.setTables(null);
            qInstance.setProcesses(null);
         },
         "At least 1 table must be defined");
   }



   /*******************************************************************************
    ** Test an instance with empty map of tables - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateEmptyTables()
   {
      assertValidationFailureReasonsAllowingExtraReasons((qInstance) ->
         {
            qInstance.setTables(new HashMap<>());
            qInstance.setProcesses(new HashMap<>());
         },
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
         },
         "Inconsistent naming for table",
         "Inconsistent naming for backend",
         "Inconsistent naming for process",
         "Inconsistent naming for possibleValueSource"
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
      assertValidationFailureReasons((qInstance) -> qInstance.addTable(table), "Missing a label");
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
   void testPossibleValueSourceMissingType()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_STATE).setType(null),
         "Missing type for possibleValueSource");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueSourceMissingIdType()
   {
      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_STATE).setIdType(null),
         "Missing an idType for possibleValueSource");
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
            possibleValueSource.setCustomCodeReference(new QCodeReference());
            possibleValueSource.setEnumValues(null);
         },
         "should not have a tableName",
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
            possibleValueSource.setCustomCodeReference(new QCodeReference());
            possibleValueSource.setEnumValues(List.of(new QPossibleValue<>("test")));
         },
         "should not have enum values",
         "should not have a customCodeReference",
         "is missing a tableName");

      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_SHAPE).setTableName("Not a table"),
         "Unrecognized table");
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
            possibleValueSource.setCustomCodeReference(null);
            possibleValueSource.setEnumValues(List.of(new QPossibleValue<>("test")));
         },
         "should not have enum values",
         "should not have a tableName",
         "is missing a customCodeReference");

      assertValidationFailureReasons((qInstance) -> qInstance.getPossibleValueSource(TestUtils.POSSIBLE_VALUE_SOURCE_CUSTOM).setCustomCodeReference(new QCodeReference()),
         "not a possibleValueProvider");
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
            assertEquals(reasons.length, e.getReasons().size(), "Expected number of validation failure reasons\nExpected: " + String.join(",", reasons) + "\nActual: " + e.getReasons());
         }

         for(String reason : reasons)
         {
            assertReason(reason, e);
         }
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
}
