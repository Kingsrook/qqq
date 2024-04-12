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

package com.kingsrook.qqq.backend.core.model.savedreports;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableDefinition;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrGetInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.SavedReportToReportMetaDataAdapter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedReportTableCustomizer implements TableCustomizerInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> postQuery(QueryOrGetInputInterface queryInput, List<QRecord> records) throws QException
   {
      for(QRecord record : CollectionUtils.nonNullList(records))
      {
         String queryFilterJson = record.getValueString("queryFilterJson");
         String columnsJson     = record.getValueString("columnsJson");
         String pivotTableJson  = record.getValueString("pivotTableJson");

         if(StringUtils.hasContent(queryFilterJson))
         {
            try
            {
               QQueryFilter qQueryFilter  = SavedReportToReportMetaDataAdapter.getQQueryFilter(queryFilterJson);
               int          criteriaCount = CollectionUtils.nonNullList(qQueryFilter.getCriteria()).size();
               record.setDisplayValue("queryFilterJson", criteriaCount + " Filter" + StringUtils.plural(criteriaCount));
            }
            catch(Exception e)
            {
               record.setDisplayValue("queryFilterJson", "Invalid Filter...");
            }
         }

         if(StringUtils.hasContent(columnsJson))
         {
            try
            {
               ReportColumns reportColumns = SavedReportToReportMetaDataAdapter.getReportColumns(columnsJson);
               long columnCount = CollectionUtils.nonNullList(reportColumns.getColumns())
                  .stream().filter(rc -> BooleanUtils.isTrue(rc.getIsVisible()))
                  .count();

               record.setDisplayValue("columnsJson", columnCount + " Column" + StringUtils.plural((int) columnCount));
            }
            catch(Exception e)
            {
               record.setDisplayValue("columnsJson", "Invalid Columns...");
            }
         }

         if(StringUtils.hasContent(pivotTableJson))
         {
            try
            {
               PivotTableDefinition pivotTableDefinition = SavedReportToReportMetaDataAdapter.getPivotTableDefinition(pivotTableJson);
               int                  rowCount             = CollectionUtils.nonNullList(pivotTableDefinition.getRows()).size();
               int                  columnCount          = CollectionUtils.nonNullList(pivotTableDefinition.getColumns()).size();
               int                  valueCount           = CollectionUtils.nonNullList(pivotTableDefinition.getValues()).size();
               record.setDisplayValue("pivotTableJson", rowCount + " Row" + StringUtils.plural(rowCount) + ", " + columnCount + " Column" + StringUtils.plural(columnCount) + ", and " + valueCount + " Value" + StringUtils.plural(valueCount));
            }
            catch(Exception e)
            {
               record.setDisplayValue("pivotTableJson", "Invalid Pivot Table...");
            }
         }
      }

      return (records);
   }

}
