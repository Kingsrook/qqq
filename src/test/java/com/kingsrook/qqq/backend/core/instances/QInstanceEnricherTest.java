package com.kingsrook.qqq.backend.core.instances;


import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 **
 *******************************************************************************/
class QInstanceEnricherTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_nullTableLabelComesFromName()
   {
      QInstance qInstance = TestUtils.defineInstance();
      QTableMetaData personTable = qInstance.getTable("person");
      personTable.setLabel(null);
      assertNull(personTable.getLabel());
      new QInstanceEnricher().enrich(qInstance);
      assertEquals("Person", personTable.getLabel());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_nullNameGivesNullLabel()
   {
      QInstance qInstance = TestUtils.defineInstance();
      QTableMetaData personTable = qInstance.getTable("person");
      personTable.setLabel(null);
      personTable.setName(null);
      assertNull(personTable.getLabel());
      assertNull(personTable.getName());
      new QInstanceEnricher().enrich(qInstance);
      assertNull(personTable.getLabel());
      assertNull(personTable.getName());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_nullFieldLabelComesFromName()
   {
      QInstance qInstance = TestUtils.defineInstance();
      QFieldMetaData idField = qInstance.getTable("person").getField("id");
      idField.setLabel(null);
      assertNull(idField.getLabel());
      new QInstanceEnricher().enrich(qInstance);
      assertEquals("Id", idField.getLabel());
   }

}