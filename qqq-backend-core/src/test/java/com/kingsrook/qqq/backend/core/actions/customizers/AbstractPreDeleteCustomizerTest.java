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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QWarningMessage;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for AbstractPreDeleteCustomizer
 *******************************************************************************/
class AbstractPreDeleteCustomizerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.withCustomizer(TableCustomizers.PRE_DELETE_RECORD.getRole(), new QCodeReference(PreDelete.class));

      TestUtils.insertRecords(table, List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Homer"),
         new QRecord().withValue("id", 2).withValue("firstName", "Marge"),
         new QRecord().withValue("id", 3).withValue("firstName", "Bart")
      ));

      ////////////////////////////////////////////////////////
      // try a delete that the pre-customizer should reject //
      ////////////////////////////////////////////////////////
      {
         DeleteInput deleteInput = new DeleteInput();
         deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         deleteInput.setPrimaryKeys(List.of(1, 2));
         DeleteOutput deleteOutput = new DeleteAction().execute(deleteInput);
         assertEquals(1, deleteOutput.getRecordsWithErrors().size());
         assertEquals(0, deleteOutput.getRecordsWithWarnings().size());
         assertEquals(1, deleteOutput.getRecordsWithErrors().get(0).getValue("id"));
         assertEquals(1, deleteOutput.getDeletedRecordCount());
         assertEquals("You may not delete a Homer.", deleteOutput.getRecordsWithErrors().get(0).getErrors().get(0).getMessage());

         GetInput getInput = new GetInput();
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         getInput.setPrimaryKey(1);
         GetOutput getOutput = new GetAction().execute(getInput);
         assertEquals("Homer", getOutput.getRecord().getValueString("firstName"));

         getInput.setPrimaryKey(2);
         getOutput = new GetAction().execute(getInput);
         assertNull(getOutput.getRecord());
      }

      ////////////////////////////////////////////////////////////
      // try a delete that the pre-customizer should warn about //
      ////////////////////////////////////////////////////////////
      {
         DeleteInput deleteInput = new DeleteInput();
         deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         deleteInput.setPrimaryKeys(List.of(3));
         DeleteOutput deleteOutput = new DeleteAction().execute(deleteInput);
         assertEquals(0, deleteOutput.getRecordsWithErrors().size());
         assertEquals(1, deleteOutput.getRecordsWithWarnings().size());
         assertEquals(3, deleteOutput.getRecordsWithWarnings().get(0).getValue("id"));
         assertEquals(1, deleteOutput.getDeletedRecordCount());
         assertEquals("It was a bad idea to delete Bart", deleteOutput.getRecordsWithWarnings().get(0).getWarnings().get(0).getMessage());

         GetInput getInput = new GetInput();
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         getInput.setPrimaryKey(3);
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNull(getOutput.getRecord());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class PreDelete extends AbstractPreDeleteCustomizer
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public List<QRecord> apply(List<QRecord> records)
      {
         for(QRecord record : records)
         {
            if(record.getValue("firstName").equals("Homer"))
            {
               record.addError(new BadInputStatusMessage("You may not delete a Homer."));
            }
            else if(record.getValue("firstName").equals("Bart"))
            {
               record.addWarning(new QWarningMessage("It was a bad idea to delete Bart"));
            }
         }

         return (records);
      }

   }

}