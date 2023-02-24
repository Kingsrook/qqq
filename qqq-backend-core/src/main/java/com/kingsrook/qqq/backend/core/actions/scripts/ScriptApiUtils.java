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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************

 $utils.query("order", null);
 $utils.query($utils.newQueryInput().withTable("order").withLimit(1).withShouldGenerateDisplayValues())
 *******************************************************************************/
public class ScriptApiUtils implements Serializable
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryInput newQueryInput()
   {
      return (new QueryInput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord newQRecord()
   {
      return (new QRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> query(String table, QQueryFilter filter) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(table);
      queryInput.setFilter(filter);
      return (new QueryAction().execute(queryInput).getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> query(QueryInput queryInput) throws QException
   {
      return (new QueryAction().execute(queryInput).getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void update(String table, List<QRecord> recordList) throws QException
   {
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(table);
      updateInput.setRecords(recordList);
      new UpdateAction().execute(updateInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void update(String table, QRecord record) throws QException
   {
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(table);
      updateInput.setRecords(List.of(record));
      new UpdateAction().execute(updateInput);
   }

}
