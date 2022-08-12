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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.customizers.CustomizerLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.Customizers;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Output for a query action
 **
 *******************************************************************************/
public class QueryOutput extends AbstractActionOutput implements Serializable
{
   private static final Logger LOG = LogManager.getLogger(QueryOutput.class);

   private QueryOutputStorageInterface storage;

   private Function<QRecord, QRecord> postQueryRecordCustomizer;



   /*******************************************************************************
    ** Construct a new query output, based on a query input (which will drive some
    ** of how our output is structured... e.g., if we pipe the output)
    *******************************************************************************/
   public QueryOutput(QueryInput queryInput)
   {
      if(queryInput.getRecordPipe() != null)
      {
         storage = new QueryOutputRecordPipe(queryInput.getRecordPipe());
      }
      else
      {
         storage = new QueryOutputList();
      }

      postQueryRecordCustomizer = (Function<QRecord, QRecord>) CustomizerLoader.getTableCustomizerFunction(queryInput.getTable(), Customizers.POST_QUERY_RECORD);
   }



   /*******************************************************************************
    ** Add a record to this output.  Note - we often don't care, in such a method,
    ** whether the record is "completed" or not (e.g., all of its values have been
    ** populated) - but - note in here - that this records MAY be going into a pipe
    ** that could be read asynchronously, at any time, by another thread - SO - only
    ** completely populated records should be passed into this method.
    *******************************************************************************/
   public void addRecord(QRecord record)
   {
      record = runPostQueryRecordCustomizer(record);
      storage.addRecord(record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord runPostQueryRecordCustomizer(QRecord record)
   {
      if(this.postQueryRecordCustomizer != null)
      {
         record = this.postQueryRecordCustomizer.apply(record);
      }
      return record;
   }



   /*******************************************************************************
    ** add a list of records to this output
    *******************************************************************************/
   public void addRecords(List<QRecord> records)
   {
      if(this.postQueryRecordCustomizer != null)
      {
         records.replaceAll(t -> this.postQueryRecordCustomizer.apply(t));
      }

      storage.addRecords(records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return storage.getRecords();
   }

}
