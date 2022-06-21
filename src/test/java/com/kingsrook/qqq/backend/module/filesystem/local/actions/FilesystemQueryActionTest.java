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

package com.kingsrook.qqq.backend.module.filesystem.local.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilesystemQueryActionTest extends FilesystemActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      super.primeFilesystem();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   public void afterEach() throws Exception
   {
      super.cleanFilesystem();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testQuery1() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      QueryResult  queryResult  = new FilesystemQueryAction().execute(queryRequest);
      Assertions.assertEquals(3, queryResult.getRecords().size(), "Unfiltered query should find all rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryRequest initQueryRequest() throws QInstanceValidationException
   {
      QueryRequest queryRequest = new QueryRequest();
      queryRequest.setInstance(TestUtils.defineInstance());
      queryRequest.setTableName(TestUtils.defineLocalFilesystemCSVPersonTable().getName());
      return queryRequest;
   }

}