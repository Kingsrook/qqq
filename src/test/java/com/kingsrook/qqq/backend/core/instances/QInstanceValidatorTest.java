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
    ** Test an instane with null backends - should throw.
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
    ** Test an instane with empty map of backends - should throw.
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
    ** Test an instane with null tables - should throw.
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
    ** Test an instane with empty map of tables - should throw.
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
    ** Test an instane where a table and a backend each have a name attribute that
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
