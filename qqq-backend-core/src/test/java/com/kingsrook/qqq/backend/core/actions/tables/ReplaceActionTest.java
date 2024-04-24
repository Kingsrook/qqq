/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.replace.ReplaceInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.replace.ReplaceOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for ReplaceAction
 *******************************************************************************/
class ReplaceActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithoutFilter() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;

      ///////////////////////////////
      // start with these 2 people //
      ///////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 1),
         new QRecord().withValue("firstName", "Mr.").withValue("lastName", "Burns")
      )));

      assertEquals(1, countByFirstName("Homer"));
      assertEquals(1, countByFirstName("Mr."));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now do a replace - updating one, inserting one, and (since it's not included in the list), deleting the other //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<QRecord> newPeople = List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 2),
         new QRecord().withValue("firstName", "Ned").withValue("lastName", "Flanders")
      );

      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("firstName", "lastName"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newPeople);
      replaceInput.setFilter(null);
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(1, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(1, replaceOutput.getUpdateOutput().getRecords().size());
      assertEquals(1, replaceOutput.getDeleteOutput().getDeletedRecordCount());

      //////////////////////////////
      // assert homer was updated //
      //////////////////////////////
      assertEquals(1, countByFirstName("Homer"));
      assertEquals(2, getNoOfShoes("Homer", "Simpson"));

      ///////////////////////////////////
      // assert Mr (burns) was deleted //
      ///////////////////////////////////
      assertEquals(0, countByFirstName("Mr."));

      /////////////////////////////
      // assert ned was inserted //
      /////////////////////////////
      assertEquals(1, countByFirstName("Ned"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOnlyInsertAndDelete() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;

      ///////////////////////////////
      // start with these 2 people //
      ///////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 1),
         new QRecord().withValue("firstName", "Marge").withValue("lastName", "Simpson").withValue("noOfShoes", 1)
      )));

      //////////////////////////////////////////
      // now do a replace that fully replaces //
      //////////////////////////////////////////
      List<QRecord> newPeople = List.of(
         new QRecord().withValue("firstName", "Ned").withValue("lastName", "Flanders"),
         new QRecord().withValue("firstName", "Maude").withValue("lastName", "Flanders")
      );

      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("firstName", "lastName"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newPeople);
      replaceInput.setFilter(null);
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(2, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(0, replaceOutput.getUpdateOutput().getRecords().size());
      assertEquals(2, replaceOutput.getDeleteOutput().getDeletedRecordCount());

      ///////////////////////////////////////
      // assert homer & marge were deleted //
      ///////////////////////////////////////
      assertEquals(0, countByFirstName("Homer"));
      assertEquals(0, countByFirstName("Marge"));

      //////////////////////////////////////
      // assert ned & maude were inserted //
      //////////////////////////////////////
      assertEquals(1, countByFirstName("Ned"));
      assertEquals(1, countByFirstName("Maude"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoKeysWithNullsNotMatchingAllowingDelete() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_TWO_KEYS;

      ////////////////////////////////
      // start with these 2 records //
      ////////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 3)
      )));

      ////////////////////////////////////////////////////
      // now do a replace action that just updates them //
      ////////////////////////////////////////////////////
      List<QRecord> newThings = List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 3)
      );

      //////////////////////////////
      // replace allowing deletes //
      //////////////////////////////
      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("key1", "key2"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newThings);
      replaceInput.setFilter(null);
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(1, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(1, replaceOutput.getUpdateOutput().getRecords().size());
      assertEquals(1, replaceOutput.getDeleteOutput().getDeletedRecordCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoKeysWithNullsNotMatchingNotAllowingDelete() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_TWO_KEYS;

      ////////////////////////////////
      // start with these 2 records //
      ////////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 3)
      )));

      ////////////////////////////////////////////////////
      // now do a replace action that just updates them //
      ////////////////////////////////////////////////////
      List<QRecord> newThings = List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 3)
      );

      /////////////////////////////////
      // replace disallowing deletes //
      /////////////////////////////////
      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("key1", "key2"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newThings);
      replaceInput.setFilter(null);
      replaceInput.setPerformDeletes(false);
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(1, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(1, replaceOutput.getUpdateOutput().getRecords().size());
      assertNull(replaceOutput.getDeleteOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoKeysWithNullMatching() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_TWO_KEYS;

      ////////////////////////////////
      // start with these 2 records //
      ////////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 3)
      )));

      ////////////////////////////////////////////////////
      // now do a replace action that just updates them //
      ////////////////////////////////////////////////////
      List<QRecord> newThings = List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 3)
      );

      ///////////////////////////////////////////////
      // replace treating null key values as equal //
      ///////////////////////////////////////////////
      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("key1", "key2"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newThings);
      replaceInput.setFilter(null);
      replaceInput.setAllowNullKeyValuesToEqual(true);
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(0, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(2, replaceOutput.getUpdateOutput().getRecords().size());
      assertEquals(0, replaceOutput.getDeleteOutput().getDeletedRecordCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOnlyUpdates() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;

      ///////////////////////////////
      // start with these 2 people //
      ///////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 1),
         new QRecord().withValue("firstName", "Marge").withValue("lastName", "Simpson").withValue("noOfShoes", 1)
      )));

      /////////////////////////////////////////////
      // now do a replace that just updates them //
      /////////////////////////////////////////////
      List<QRecord> newPeople = List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 2),
         new QRecord().withValue("firstName", "Marge").withValue("lastName", "Simpson").withValue("noOfShoes", 2)
      );

      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("firstName", "lastName"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newPeople);
      replaceInput.setFilter(null);
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(0, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(2, replaceOutput.getUpdateOutput().getRecords().size());
      assertEquals(0, replaceOutput.getDeleteOutput().getDeletedRecordCount());

      ///////////////////////////////////////
      // assert homer & marge were updated //
      ///////////////////////////////////////
      assertEquals(2, getNoOfShoes("Homer", "Simpson"));
      assertEquals(2, getNoOfShoes("Marge", "Simpson"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithFilter() throws QException
   {
      String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;

      /////////////////////////////////////
      // start w/ 3 simpsons and a burns //
      /////////////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 1),
         new QRecord().withValue("firstName", "Marge").withValue("lastName", "Simpson").withValue("noOfShoes", 2),
         new QRecord().withValue("firstName", "Bart").withValue("lastName", "Simpson").withValue("noOfShoes", 3),
         new QRecord().withValue("firstName", "Mr.").withValue("lastName", "Burns")
      )));

      assertEquals(1, countByFirstName("Homer"));
      assertEquals(1, countByFirstName("Marge"));
      assertEquals(1, countByFirstName("Bart"));
      assertEquals(1, countByFirstName("Mr."));

      /////////////////////////////////////////////////////////////////////////////////
      // now - we'll replace the simpsons only - note the filter in the ReplaceInput //
      // so even though Burns isn't in this list, he shouldn't be deleted.           //
      /////////////////////////////////////////////////////////////////////////////////
      List<QRecord> newPeople = List.of(
         new QRecord().withValue("firstName", "Homer").withValue("lastName", "Simpson").withValue("noOfShoes", 4),
         new QRecord().withValue("firstName", "Marge").withValue("lastName", "Simpson"),
         new QRecord().withValue("firstName", "Lisa").withValue("lastName", "Simpson").withValue("noOfShoes", 5)
      );

      ReplaceInput replaceInput = new ReplaceInput();
      replaceInput.setTableName(tableName);
      replaceInput.setKey(new UniqueKey("firstName", "lastName"));
      replaceInput.setOmitDmlAudit(true);
      replaceInput.setRecords(newPeople);
      replaceInput.setFilter(new QQueryFilter(new QFilterCriteria("lastName", QCriteriaOperator.EQUALS, "Simpson")));
      ReplaceOutput replaceOutput = new ReplaceAction().execute(replaceInput);

      assertEquals(1, replaceOutput.getInsertOutput().getRecords().size());
      assertEquals(2, replaceOutput.getUpdateOutput().getRecords().size());
      assertEquals(1, replaceOutput.getDeleteOutput().getDeletedRecordCount());

      //////////////////////////////
      // assert homer was updated //
      //////////////////////////////
      assertEquals(1, countByFirstName("Homer"));
      assertEquals(4, getNoOfShoes("Homer", "Simpson"));

      ///////////////////////////////
      // assert Marge was no-op'ed //
      ///////////////////////////////
      assertEquals(1, countByFirstName("Marge"));
      assertEquals(2, getNoOfShoes("Marge", "Simpson"));

      ////////////////////////////////////
      // assert Mr (burns) was no-op'ed //
      ////////////////////////////////////
      assertEquals(1, countByFirstName("Mr."));
      assertNull(getNoOfShoes("Mr.", "Burns"));

      /////////////////////////////
      // assert Bart was deleted //
      /////////////////////////////
      assertEquals(0, countByFirstName("Bart"));

      //////////////////////////////
      // assert Lisa was inserted //
      //////////////////////////////
      assertEquals(1, countByFirstName("Lisa"));
      assertEquals(5, getNoOfShoes("Lisa", "Simpson"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer countByFirstName(String firstName) throws QException
   {
      return new CountAction().execute(new CountInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withFilter(new QQueryFilter(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, firstName)))).getCount();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer getNoOfShoes(String firstName, String lastName) throws QException
   {
      return new GetAction().executeForRecord(new GetInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withUniqueKey(Map.of("firstName", firstName, "lastName", lastName))).getValueInteger("noOfShoes");
   }

}
