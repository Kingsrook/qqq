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


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableDefinition;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableGroupBy;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableValue;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.PermissionDeniedMessage;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.SavedReportToReportMetaDataAdapter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedReportTableCustomizer implements TableCustomizerInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> preInsert(InsertInput insertInput, List<QRecord> records, boolean isPreview) throws QException
   {
      return (preInsertOrUpdate(records));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> preUpdate(UpdateInput updateInput, List<QRecord> records, boolean isPreview, Optional<List<QRecord>> oldRecordList) throws QException
   {
      validateOwner(records, SavedReport.TABLE_NAME, "edit");
      return (preInsertOrUpdate(records));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> preDelete(DeleteInput deleteInput, List<QRecord> records, boolean isPreview) throws QException
   {
      validateOwner(records, SavedReport.TABLE_NAME, "delete");
      return (preInsertOrUpdate(records));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void validateOwner(List<QRecord> records, String tableName, String verb)
   {
      QTableMetaData tableMetaData = QContext.getQInstance().getTable(tableName);
      String         currentUserId = ObjectUtils.tryElse(() -> QContext.getQSession().getUser().getIdReference(), null);
      for(QRecord record : records)
      {
         if(record.getValue("userId") != null)
         {
            if(!record.getValue("userId").equals(currentUserId))
            {
               record.addError(new PermissionDeniedMessage("Only the owner of a " + tableMetaData.getLabel() + " may " + verb + " it."));
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> preInsertOrUpdate(List<QRecord> records)
   {
      for(QRecord record : CollectionUtils.nonNullList(records))
      {
         preValidateRecord(record);
      }
      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void preValidateRecord(QRecord record)
   {
      try
      {
         String tableName       = record.getValueString("tableName");
         String queryFilterJson = record.getValueString("queryFilterJson");
         String columnsJson     = record.getValueString("columnsJson");
         String pivotTableJson  = record.getValueString("pivotTableJson");

         Set<String> usedColumns = new HashSet<>();

         QTableMetaData table = QContext.getQInstance().getTable(tableName);
         if(table == null)
         {
            record.addError(new BadInputStatusMessage("Unrecognized table name: " + tableName));
         }

         if(StringUtils.hasContent(queryFilterJson))
         {
            try
            {
               /////////////////////////////////////////////////////////////////////////
               // validate that we can parse the filter, then prep it for the backend //
               /////////////////////////////////////////////////////////////////////////
               QQueryFilter filter = SavedReportToReportMetaDataAdapter.getQQueryFilter(queryFilterJson);
               filter.prepForBackend();
               record.setValue("queryFilterJson", filter);
            }
            catch(IOException e)
            {
               record.addError(new BadInputStatusMessage("Unable to parse queryFilterJson: " + e.getMessage()));
            }
         }

         boolean hadColumnParseError = false;
         if(StringUtils.hasContent(columnsJson))
         {
            try
            {
               /////////////////////////////////////////////////////////////////////////
               // make sure we can parse columns, and that we have at least 1 visible //
               /////////////////////////////////////////////////////////////////////////
               ReportColumns reportColumns = SavedReportToReportMetaDataAdapter.getReportColumns(columnsJson);
               for(ReportColumn column : reportColumns.extractVisibleColumns())
               {
                  usedColumns.add(column.getName());
               }
            }
            catch(IOException e)
            {
               record.addError(new BadInputStatusMessage("Unable to parse columnsJson: " + e.getMessage()));
               hadColumnParseError = true;
            }
         }

         if(usedColumns.isEmpty() && !hadColumnParseError)
         {
            record.addError(new BadInputStatusMessage("A Report must contain at least 1 column"));
         }

         if(StringUtils.hasContent(pivotTableJson))
         {
            try
            {
               /////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // make sure we can parse pivot table, and we have ... at least 1 ... row?  maybe that's all that's needed //
               /////////////////////////////////////////////////////////////////////////////////////////////////////////////
               PivotTableDefinition pivotTableDefinition          = SavedReportToReportMetaDataAdapter.getPivotTableDefinition(pivotTableJson);
               boolean              anyRows                       = false;
               boolean              missingAnyFieldNamesInRows    = false;
               boolean              missingAnyFieldNamesInColumns = false;
               boolean              missingAnyFieldNamesInValues  = false;
               boolean              missingAnyFunctionsInValues   = false;

               //////////////////
               // look at rows //
               //////////////////
               for(PivotTableGroupBy row : CollectionUtils.nonNullList(pivotTableDefinition.getRows()))
               {
                  anyRows = true;
                  if(StringUtils.hasContent(row.getFieldName()))
                  {
                     if(!usedColumns.contains(row.getFieldName()) && !hadColumnParseError)
                     {
                        record.addError(new BadInputStatusMessage("A pivot table row is using field (" + getFieldLabelElseName(table, row.getFieldName()) + ") which is not an active column on this report."));
                     }
                  }
                  else
                  {
                     missingAnyFieldNamesInRows = true;
                  }
               }

               if(!anyRows)
               {
                  record.addError(new BadInputStatusMessage("A Pivot Table must contain at least 1 row"));
               }

               /////////////////////
               // look at columns //
               /////////////////////
               for(PivotTableGroupBy column : CollectionUtils.nonNullList(pivotTableDefinition.getColumns()))
               {
                  if(StringUtils.hasContent(column.getFieldName()))
                  {
                     if(!usedColumns.contains(column.getFieldName()) && !hadColumnParseError)
                     {
                        record.addError(new BadInputStatusMessage("A pivot table column is using field (" + getFieldLabelElseName(table, column.getFieldName()) + ") which is not an active column on this report."));
                     }
                  }
                  else
                  {
                     missingAnyFieldNamesInColumns = true;
                  }
               }

               ////////////////////
               // look at values //
               ////////////////////
               for(PivotTableValue value : CollectionUtils.nonNullList(pivotTableDefinition.getValues()))
               {
                  if(StringUtils.hasContent(value.getFieldName()))
                  {
                     if(!usedColumns.contains(value.getFieldName()) && !hadColumnParseError)
                     {
                        record.addError(new BadInputStatusMessage("A pivot table value is using field (" + getFieldLabelElseName(table, value.getFieldName()) + ") which is not an active column on this report."));
                     }
                  }
                  else
                  {
                     missingAnyFieldNamesInValues = true;
                  }

                  if(value.getFunction() == null)
                  {
                     missingAnyFunctionsInValues = true;
                  }
               }

               ////////////////////////////////////////////////
               // errors based on missing things found above //
               ////////////////////////////////////////////////
               if(missingAnyFieldNamesInRows)
               {
                  record.addError(new BadInputStatusMessage("Missing field name for at least one pivot table row."));
               }

               if(missingAnyFieldNamesInColumns)
               {
                  record.addError(new BadInputStatusMessage("Missing field name for at least one pivot table column."));
               }

               if(missingAnyFieldNamesInValues)
               {
                  record.addError(new BadInputStatusMessage("Missing field name for at least one pivot table value."));
               }

               if(missingAnyFunctionsInValues)
               {
                  record.addError(new BadInputStatusMessage("Missing function for at least one pivot table value."));
               }
            }
            catch(IOException e)
            {
               record.addError(new BadInputStatusMessage("Unable to parse pivotTableJson: " + e.getMessage()));
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error validating a savedReport");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getFieldLabelElseName(QTableMetaData table, String fieldName)
   {
      try
      {
         GenerateReportAction.FieldAndJoinTable fieldAndJoinTable = GenerateReportAction.getFieldAndJoinTable(table, fieldName);
         return (fieldAndJoinTable.getLabel(table));
      }
      catch(Exception e)
      {
         return (fieldName);
      }
   }

}
