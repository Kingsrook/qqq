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
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableDefinition;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldDisplayBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.SavedReportToReportMetaDataAdapter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedReportJsonFieldDisplayValueFormatter implements FieldDisplayBehavior<SavedReportJsonFieldDisplayValueFormatter>
{
   private static SavedReportJsonFieldDisplayValueFormatter savedReportJsonFieldDisplayValueFormatter = null;



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private SavedReportJsonFieldDisplayValueFormatter()
   {

   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static SavedReportJsonFieldDisplayValueFormatter getInstance()
   {
      if(savedReportJsonFieldDisplayValueFormatter == null)
      {
         savedReportJsonFieldDisplayValueFormatter = new SavedReportJsonFieldDisplayValueFormatter();
      }
      return (savedReportJsonFieldDisplayValueFormatter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public SavedReportJsonFieldDisplayValueFormatter getDefault()
   {
      return getInstance();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field)
   {
      for(QRecord record : CollectionUtils.nonNullList(recordList))
      {
         if(field.getName().equals("queryFilterJson"))
         {
            String queryFilterJson = record.getValueString("queryFilterJson");
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
         }

         if(field.getName().equals("columnsJson"))
         {
            String columnsJson = record.getValueString("columnsJson");
            if(StringUtils.hasContent(columnsJson))
            {
               try
               {
                  ReportColumns reportColumns = SavedReportToReportMetaDataAdapter.getReportColumns(columnsJson);
                  int           columnCount   = reportColumns.extractVisibleColumns().size();

                  record.setDisplayValue("columnsJson", columnCount + " Column" + StringUtils.plural(columnCount));
               }
               catch(Exception e)
               {
                  record.setDisplayValue("columnsJson", "Invalid Columns...");
               }
            }
         }

         if(field.getName().equals("pivotTableJson"))
         {
            String pivotTableJson = record.getValueString("pivotTableJson");
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
      }
   }

}
