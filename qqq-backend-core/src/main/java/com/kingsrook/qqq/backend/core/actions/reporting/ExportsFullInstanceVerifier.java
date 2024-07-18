/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.reporting;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportDestination;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Utility for verifying that the ExportAction works for all tables, and all
 ** exposed joins.
 **
 ** Meant for use within a unit test, or maybe as part of an instance's boot-up/
 ** validation.
 *******************************************************************************/
public class ExportsFullInstanceVerifier
{
   private static final QLogger LOG = QLogger.getLogger(ExportsFullInstanceVerifier.class);

   private boolean filterForAtMostOneRowPerExport = true;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void verify(Collection<QTableMetaData> tables) throws QException
   {
      Map<Pair<String, String>, Exception> caughtExceptions = new LinkedHashMap<>();
      for(QTableMetaData table : tables)
      {
         if(table.isCapabilityEnabled(QContext.getQInstance().getBackendForTable(table.getName()), Capability.TABLE_QUERY))
         {
            LOG.info("Verifying Exports on table", logPair("tableName", table.getName()));

            //////////////////////////////////////////////
            // run the table by itself (no join fields) //
            //////////////////////////////////////////////
            runExport(table.getName(), Collections.emptyList(), "main-table-only", caughtExceptions);

            ///////////////////////////////////////////////////
            // run once w/ the fields from each exposed join //
            ///////////////////////////////////////////////////
            for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(table.getExposedJoins()))
            {
               runExport(table.getName(), List.of(exposedJoin), "join-" + exposedJoin.getLabel(), caughtExceptions);
            }

            /////////////////////////////////////////////////
            // run w/ all exposed joins (if there are any) //
            /////////////////////////////////////////////////
            if(CollectionUtils.nullSafeHasContents(table.getExposedJoins()))
            {
               runExport(table.getName(), table.getExposedJoins(), "all-joins", caughtExceptions);
            }
         }
      }

      //////////////////////////////////
      // log out an exceptions caught //
      //////////////////////////////////
      if(!caughtExceptions.isEmpty())
      {
         for(Map.Entry<Pair<String, String>, Exception> entry : caughtExceptions.entrySet())
         {
            LOG.info("Caught an exception verifying reports", entry.getValue(), logPair("tableName", entry.getKey().getA()), logPair("fieldName", entry.getKey().getB()));
         }
         throw (new QException("Reports Verification failed with " + caughtExceptions.size() + " exception" + StringUtils.plural(caughtExceptions.size())));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runExport(String tableName, List<ExposedJoin> exposedJoinList, String description, Map<Pair<String, String>, Exception> caughtExceptions)
   {
      try
      {
         ////////////////////////////////////////////////////////////////////////////////////
         // build the list of fieldNames to export - starting with all fields in the table //
         ////////////////////////////////////////////////////////////////////////////////////
         List<String> fieldNames = new ArrayList<>();
         for(QFieldMetaData field : QContext.getQInstance().getTable(tableName).getFields().values())
         {
            fieldNames.add(field.getName());
         }

         ///////////////////////////////////////////////////
         // add all fields from all exposed joins as well //
         ///////////////////////////////////////////////////
         for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(exposedJoinList))
         {
            QTableMetaData joinTable = QContext.getQInstance().getTable(exposedJoin.getJoinTable());
            for(QFieldMetaData field : joinTable.getFields().values())
            {
               fieldNames.add(joinTable.getName() + "." + field.getName());
            }
         }

         LOG.info("Verifying export", logPair("description", description), logPair("fieldCount", fieldNames.size()));

         QQueryFilter queryFilter = new QQueryFilter();

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if caller is okay with a filter that should limit the report to a small number of rows (could be more than 1 for to-many joins), then do so //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(filterForAtMostOneRowPerExport)
         {
            queryFilter.withCriteria(QContext.getQInstance().getTable(tableName).getPrimaryKeyField(), QCriteriaOperator.EQUALS, 1);
         }

         ExportInput exportInput = new ExportInput();
         exportInput.setTableName(tableName);
         exportInput.setFieldNames(fieldNames);
         exportInput.setReportDestination(new ReportDestination()
            .withReportOutputStream(new ByteArrayOutputStream())
            .withReportFormat(ReportFormat.CSV));
         exportInput.setQueryFilter(queryFilter);
         new ExportAction().execute(exportInput);
      }
      catch(QException e)
      {
         caughtExceptions.put(Pair.of(tableName, description), e);
      }
   }



   /*******************************************************************************
    ** Getter for filterForAtMostOneRowPerExport
    *******************************************************************************/
   public boolean getFilterForAtMostOneRowPerExport()
   {
      return (this.filterForAtMostOneRowPerExport);
   }



   /*******************************************************************************
    ** Setter for filterForAtMostOneRowPerExport
    *******************************************************************************/
   public void setFilterForAtMostOneRowPerExport(boolean filterForAtMostOneRowPerExport)
   {
      this.filterForAtMostOneRowPerExport = filterForAtMostOneRowPerExport;
   }



   /*******************************************************************************
    ** Fluent setter for filterForAtMostOneRowPerExport
    *******************************************************************************/
   public ExportsFullInstanceVerifier withFilterForAtMostOneRowPerExport(boolean filterForAtMostOneRowPerExport)
   {
      this.filterForAtMostOneRowPerExport = filterForAtMostOneRowPerExport;
      return (this);
   }


}
