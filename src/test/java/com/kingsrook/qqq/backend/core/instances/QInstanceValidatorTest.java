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


import java.util.HashMap;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    ** Test an instance with null backends - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateNullBackends()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.setBackends(null);
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("At least 1 backend must be defined", e);
      }
   }



   /*******************************************************************************
    ** Test an instance with empty map of backends - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateEmptyBackends()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.setBackends(new HashMap<>());
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("At least 1 backend must be defined", e);
      }
   }



   /*******************************************************************************
    ** Test an instance with null tables - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateNullTables()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.setTables(null);
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("At least 1 table must be defined", e);
      }
   }



   /*******************************************************************************
    ** Test an instance with empty map of tables - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateEmptyTables()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.setTables(new HashMap<>());
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("At least 1 table must be defined", e);
      }
   }



   /*******************************************************************************
    ** Test an instance where a table and a backend each have a name attribute that
    ** doesn't match the key that those objects have in the instance's maps - should throw.
    **
    *******************************************************************************/
   @Test
   public void test_validateInconsistentNames()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.getTable("person").setName("notPerson");
         qInstance.getBackend("default").setName("notDefault");
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("Inconsistent naming for table", e);
         assertReason("Inconsistent naming for backend", e);
      }
   }



   /*******************************************************************************
    ** Test that if a table has a null backend, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateTableWithoutBackend()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.getTable("person").setBackendName(null);
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("Missing backend name for table", e);
      }
   }



   /*******************************************************************************
    ** Test that if a table specifies a backend that doesn't exist, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateTableWithMissingBackend()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.getTable("person").setBackendName("notARealBackend");
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("Unrecognized backend", e);
      }
   }



   /*******************************************************************************
    ** Test that a table with no fields fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateTableWithNoFields()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.getTable("person").setFields(null);
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("At least 1 field", e);
      }

      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.getTable("person").setFields(new HashMap<>());
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("At least 1 field", e);
      }
   }



   /*******************************************************************************
    ** Test that if a field specifies a backend that doesn't exist, that it fails.
    **
    *******************************************************************************/
   @Test
   public void test_validateFieldWithMissingPossibleValueSource()
   {
      try
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.getTable("person").getField("homeState").setPossibleValueSourceName("not a real possible value source");
         new QInstanceValidator().validate(qInstance);
         fail("Should have thrown validationException");
      }
      catch(QInstanceValidationException e)
      {
         assertReason("Unrecognized possibleValueSourceName", e);
      }
   }



   /*******************************************************************************
    ** utility method for asserting that a specific reason string is found within
    ** the list of reasons in the QInstanceValidationException.
    **
    *******************************************************************************/
   private void assertReason(String reason, QInstanceValidationException e)
   {
      assertNotNull(e.getReasons());
      assertTrue(e.getReasons().stream().anyMatch(s -> s.contains(reason)));
   }
}
