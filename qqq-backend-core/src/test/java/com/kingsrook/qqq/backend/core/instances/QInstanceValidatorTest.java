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


import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
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
      assertValidationFailureReasons((qInstance) ->
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
      assertValidationFailureReasons((qInstance) ->
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
         },
         "Inconsistent naming for table",
         "Inconsistent naming for backend",
         "Inconsistent naming for process");
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
      assertValidationFailureReasons((qInstance) -> qInstance.getTable("person").getField("homeState").setPossibleValueSourceName("not a real possible value source"),
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
