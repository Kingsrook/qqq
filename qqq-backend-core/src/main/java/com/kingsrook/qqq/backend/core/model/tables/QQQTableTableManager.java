/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.tables;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrGetInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.utils.GeneralProcessUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Utility class for accessing QQQTable records (well, just their ids at this time)
 ** Takes care of inserting upon a miss, and dealing with the cache table.
 *******************************************************************************/
public class QQQTableTableManager
{
   private static final QLogger LOG = QLogger.getLogger(QQQTableTableManager.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer getQQQTableId(QInstance qInstance, String tableName) throws QException
   {
      /////////////////////////////
      // look in the cache table //
      /////////////////////////////
      GetInput getInput = new GetInput();
      getInput.setTableName(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME);
      getInput.setUniqueKey(MapBuilder.of("name", tableName));
      GetOutput getOutput = new GetAction().execute(getInput);

      ////////////////////////
      // upon cache miss... //
      ////////////////////////
      if(getOutput.getRecord() == null)
      {
         QTableMetaData tableMetaData = qInstance.getTable(tableName);
         if(tableMetaData == null)
         {
            LOG.info("No such table", logPair("tableName", tableName));
            return (null);
         }

         ///////////////////////////////////////////////////////
         // insert the record (into the table, not the cache) //
         ///////////////////////////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(QQQTable.TABLE_NAME);
         insertInput.setRecords(List.of(new QRecord().withValue("name", tableName).withValue("label", tableMetaData.getLabel())));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);

         ///////////////////////////////////
         // repeat the get from the cache //
         ///////////////////////////////////
         getOutput = new GetAction().execute(getInput);
      }

      return getOutput.getRecord().getValueInteger("id");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static List<QRecord> setRecordLinksToRecordsFromTableDynamicForPostQuery(QueryOrGetInputInterface queryInput, List<QRecord> records, String tableIdField, String recordIdField) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////
      // note, this is a second copy of this logic (first being in standard process traces). //
      // let the rule of 3 apply if we find ourselves copying it again                       //
      /////////////////////////////////////////////////////////////////////////////////////////
      if(queryInput.getShouldGenerateDisplayValues())
      {
         ///////////////////////////////////////////////////////////////////////////////////////////
         // for records with a table id value - look up that table name, then set a display-value //
         // for the Link type adornment, to the inputRecordId record within that table.           //
         ///////////////////////////////////////////////////////////////////////////////////////////
         Set<Serializable> tableIds = records.stream().map(r -> r.getValue(tableIdField)).filter(Objects::nonNull).collect(Collectors.toSet());
         if(!tableIds.isEmpty())
         {
            Map<Serializable, QRecord> tableMap = GeneralProcessUtils.loadTableToMap(QQQTable.TABLE_NAME, "id", new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, tableIds)));

            for(QRecord record : records)
            {
               QRecord qqqTableRecord = tableMap.get(record.getValue(tableIdField));
               if(qqqTableRecord != null && record.getValue(recordIdField) != null)
               {
                  record.setDisplayValue(recordIdField + ":" + AdornmentType.LinkValues.TO_RECORD_FROM_TABLE_DYNAMIC, qqqTableRecord.getValueString("name"));
               }
            }
         }
      }

      return (records);
   }

}
