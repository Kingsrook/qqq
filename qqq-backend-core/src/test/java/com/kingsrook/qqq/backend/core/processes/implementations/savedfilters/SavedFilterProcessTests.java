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

package com.kingsrook.qqq.backend.core.processes.implementations.savedfilters;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.savedfilters.SavedFiltersMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.tables.QQQTablesMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for all saved filter processes
 *******************************************************************************/
class SavedFilterProcessTests extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      new SavedFiltersMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);
      new QQQTablesMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);

      String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;

      {
         ///////////////////////////////////////////
         // query - should be no filters to start //
         ///////////////////////////////////////////
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(QuerySavedFilterProcess.getProcessMetaData().getName());
         runProcessInput.addValue("tableName", tableName);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
         assertEquals(0, ((List<?>) runProcessOutput.getValues().get("savedFilterList")).size());
      }

      Integer savedFilterId;
      {
         ////////////////////////
         // store a new filter //
         ////////////////////////
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(StoreSavedFilterProcess.getProcessMetaData().getName());
         runProcessInput.addValue("label", "My Filter");
         runProcessInput.addValue("tableName", tableName);
         runProcessInput.addValue("filterJson", JsonUtils.toJson(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.EQUALS, 47))));
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
         List<QRecord>    savedFilterList  = (List<QRecord>) runProcessOutput.getValues().get("savedFilterList");
         assertEquals(1, savedFilterList.size());
         savedFilterId = savedFilterList.get(0).getValueInteger("id");
         assertNotNull(savedFilterId);
      }

      {
         ////////////////////////////////////
         // query - should find our filter //
         ////////////////////////////////////
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(QuerySavedFilterProcess.getProcessMetaData().getName());
         runProcessInput.addValue("tableName", tableName);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
         List<QRecord>    savedFilterList  = (List<QRecord>) runProcessOutput.getValues().get("savedFilterList");
         assertEquals(1, savedFilterList.size());
         assertEquals(1, savedFilterList.get(0).getValueInteger("id"));
         assertEquals("My Filter", savedFilterList.get(0).getValueString("label"));
      }

      {
         ///////////////////////
         // update our filter //
         ///////////////////////
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(StoreSavedFilterProcess.getProcessMetaData().getName());
         runProcessInput.addValue("id", savedFilterId);
         runProcessInput.addValue("label", "My Updated Filter");
         runProcessInput.addValue("tableName", tableName);
         runProcessInput.addValue("filterJson", JsonUtils.toJson(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.EQUALS, 47))));
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
         List<QRecord>    savedFilterList  = (List<QRecord>) runProcessOutput.getValues().get("savedFilterList");
         assertEquals(1, savedFilterList.size());
         assertEquals(1, savedFilterList.get(0).getValueInteger("id"));
         assertEquals("My Updated Filter", savedFilterList.get(0).getValueString("label"));
      }

      {
         ///////////////////////
         // delete our filter //
         ///////////////////////
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(DeleteSavedFilterProcess.getProcessMetaData().getName());
         runProcessInput.addValue("id", savedFilterId);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
      }

      {
         ////////////////////////////////////////
         // query - should be no filters again //
         ////////////////////////////////////////
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(QuerySavedFilterProcess.getProcessMetaData().getName());
         runProcessInput.addValue("tableName", tableName);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
         assertEquals(0, ((List<?>) runProcessOutput.getValues().get("savedFilterList")).size());
      }

   }

}