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

package com.kingsrook.qqq.backend.core.actions.audits;


import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditInput;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Audit for a standard DML (Data Manipulation Language) activity - e.g.,
 ** insert, edit, or delete.
 *******************************************************************************/
public class DMLAuditAction extends AbstractQActionFunction<DMLAuditInput, DMLAuditOutput>
{
   private static final QLogger LOG = QLogger.getLogger(DMLAuditAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DMLAuditOutput execute(DMLAuditInput input) throws QException
   {
      DMLAuditOutput           output           = new DMLAuditOutput();
      AbstractTableActionInput tableActionInput = input.getTableActionInput();
      List<QRecord>            recordList       = input.getRecordList();
      List<QRecord>            oldRecordList    = input.getOldRecordList();
      QTableMetaData           table            = tableActionInput.getTable();
      long                     start            = System.currentTimeMillis();
      DMLType                  dmlType          = getDMLType(tableActionInput);

      try
      {
         AuditLevel auditLevel = getAuditLevel(tableActionInput);
         if(auditLevel == null || auditLevel.equals(AuditLevel.NONE))
         {
            /////////////////////////////////////////////
            // return with noop for null or level NONE //
            /////////////////////////////////////////////
            return (output);
         }

         String                        contextSuffix = "";
         Optional<AbstractActionInput> actionInput   = QContext.getFirstActionInStack();
         if(actionInput.isPresent() && actionInput.get() instanceof RunProcessInput runProcessInput)
         {
            String           processName = runProcessInput.getProcessName();
            QProcessMetaData process     = QContext.getQInstance().getProcess(processName);
            if(process != null)
            {
               contextSuffix = " during process: " + process.getLabel();
            }
         }

         AuditInput auditInput = new AuditInput();
         if(auditLevel.equals(AuditLevel.RECORD) || (auditLevel.equals(AuditLevel.FIELD) && !dmlType.supportsFields))
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // make many simple audits (no details) for RECORD level                                                  //
            // or for FIELD level, but on a DML type that doesn't support field-level details (e.g., DELETE or OTHER) //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            for(QRecord record : recordList)
            {
               AuditAction.appendToInput(auditInput, table.getName(), record.getValueInteger(table.getPrimaryKeyField()), getRecordSecurityKeyValues(table, record), "Record was " + dmlType.pastTenseVerb + contextSuffix);
            }
         }
         else if(auditLevel.equals(AuditLevel.FIELD))
         {
            Map<Serializable, QRecord> oldRecordMap = buildOldRecordMap(table, oldRecordList);

            ///////////////////////////////////////////////////////////////////
            // do many audits, all with field level details, for FIELD level //
            ///////////////////////////////////////////////////////////////////
            QPossibleValueTranslator qPossibleValueTranslator = new QPossibleValueTranslator(QContext.getQInstance(), QContext.getQSession());
            qPossibleValueTranslator.translatePossibleValuesInRecords(table, CollectionUtils.mergeLists(recordList, oldRecordList));

            //////////////////////////////////////////
            // sort the field names by their labels //
            //////////////////////////////////////////
            List<String> sortedFieldNames = table.getFields().keySet().stream()
               .sorted(Comparator.comparing(fieldName -> table.getFields().get(fieldName).getLabel()))
               .toList();

            QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());

            //////////////////////////////////////////////
            // build single audit input for each record //
            //////////////////////////////////////////////
            for(QRecord record : recordList)
            {
               QRecord oldRecord = oldRecordMap.get(ValueUtils.getValueAsFieldType(primaryKeyField.getType(), record.getValue(primaryKeyField.getName())));

               List<QRecord> details = new ArrayList<>();
               for(String fieldName : sortedFieldNames)
               {
                  if(!record.getValues().containsKey(fieldName))
                  {
                     ////////////////////////////////////////////////////////////////////////////////////////////////
                     // if the stored record doesn't have this field name, then don't audit anything about it      //
                     // this is to deal with our Patch style updates not looking like every field was cleared out. //
                     ////////////////////////////////////////////////////////////////////////////////////////////////
                     continue;
                  }

                  if(fieldName.equals("modifyDate") || fieldName.equals("createDate") || fieldName.equals("automationStatus"))
                  {
                     continue;
                  }

                  QFieldMetaData field        = table.getField(fieldName);
                  Serializable   value        = ValueUtils.getValueAsFieldType(field.getType(), record.getValue(fieldName));
                  Serializable   oldValue     = oldRecord == null ? null : ValueUtils.getValueAsFieldType(field.getType(), oldRecord.getValue(fieldName));
                  QRecord        detailRecord = null;

                  if(oldRecord == null)
                  {
                     if(DMLType.INSERT.equals(dmlType) && value == null)
                     {
                        continue;
                     }

                     String formattedValue = getFormattedValueForAuditDetail(record, fieldName, field, value);
                     detailRecord = new QRecord().withValue("message", "Set " + field.getLabel() + " to " + formattedValue);
                     detailRecord.withValue("newValue", formattedValue);
                  }
                  else
                  {
                     if(!Objects.equals(oldValue, value))
                     {
                        String formattedValue    = getFormattedValueForAuditDetail(record, fieldName, field, value);
                        String formattedOldValue = getFormattedValueForAuditDetail(oldRecord, fieldName, field, oldValue);

                        if(oldValue == null)
                        {
                           detailRecord = new QRecord().withValue("message", "Set " + field.getLabel() + " to " + formatFormattedValueForDetailMessage(field, formattedValue));
                           detailRecord.withValue("newValue", formattedValue);
                        }
                        else if(value == null)
                        {
                           detailRecord = new QRecord().withValue("message", "Removed " + formatFormattedValueForDetailMessage(field, formattedOldValue) + " from " + field.getLabel());
                           detailRecord.withValue("oldValue", formattedOldValue);
                        }
                        else
                        {
                           detailRecord = new QRecord().withValue("message", "Changed " + field.getLabel() + " from " + formatFormattedValueForDetailMessage(field, formattedOldValue) + " to " + formatFormattedValueForDetailMessage(field, formattedValue));
                           detailRecord.withValue("oldValue", formattedOldValue);
                           detailRecord.withValue("newValue", formattedValue);
                        }
                     }
                  }

                  if(detailRecord != null)
                  {
                     detailRecord.withValue("fieldName", fieldName);
                     details.add(detailRecord);
                  }
               }

               if(details.isEmpty())
               {
                  details.add(new QRecord().withValue("message", "No fields values were changed."));
               }

               AuditAction.appendToInput(auditInput, table.getName(), record.getValueInteger(table.getPrimaryKeyField()), getRecordSecurityKeyValues(table, record), "Record was " + dmlType.pastTenseVerb + contextSuffix, details);
            }
         }

         // new AuditAction().executeAsync(auditInput); // todo async??? maybe get that from rules???
         new AuditAction().execute(auditInput);
         long end = System.currentTimeMillis();
         LOG.debug("Audit performance", logPair("auditLevel", String.valueOf(auditLevel)), logPair("recordCount", recordList.size()), logPair("millis", (end - start)));
      }
      catch(Exception e)
      {
         LOG.error("Error performing DML audit", e, logPair("type", String.valueOf(dmlType)), logPair("table", table.getName()));
      }

      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getFormattedValueForAuditDetail(QRecord record, String fieldName, QFieldMetaData field, Serializable value)
   {
      String formattedValue = null;
      if(value != null)
      {
         if(field.getType().equals(QFieldType.DATE_TIME) && value instanceof Instant instant)
         {
            formattedValue = QValueFormatter.formatDateTimeWithZone(instant.atZone(ZoneId.of(Objects.requireNonNullElse(QContext.getQInstance().getDefaultTimeZoneId(), "UTC"))));
         }
         else if(record.getDisplayValue(fieldName) != null)
         {
            formattedValue = record.getDisplayValue(fieldName);
         }
         else
         {
            formattedValue = QValueFormatter.formatValue(field, value);
         }
      }

      return formattedValue;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String formatFormattedValueForDetailMessage(QFieldMetaData field, String formattedValue)
   {
      if(formattedValue == null || "null".equals(formattedValue))
      {
         formattedValue = "--";
      }
      else
      {
         if(QFieldType.STRING.equals(field.getType()) || field.getPossibleValueSourceName() != null)
         {
            formattedValue = '"' + formattedValue + '"';
         }
      }

      return (formattedValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<Serializable, QRecord> buildOldRecordMap(QTableMetaData table, List<QRecord> oldRecordList)
   {
      Map<Serializable, QRecord> rs = new HashMap<>();
      for(QRecord record : CollectionUtils.nonNullList(oldRecordList))
      {
         rs.put(record.getValue(table.getPrimaryKeyField()), record);
      }
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private DMLType getDMLType(AbstractTableActionInput tableActionInput)
   {
      if(tableActionInput instanceof InsertInput)
      {
         return DMLType.INSERT;
      }
      else if(tableActionInput instanceof UpdateInput)
      {
         return DMLType.UPDATE;
      }
      else if(tableActionInput instanceof DeleteInput)
      {
         return DMLType.DELETE;
      }
      else
      {
         return DMLType.OTHER;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Map<String, Serializable> getRecordSecurityKeyValues(QTableMetaData table, QRecord record)
   {
      Map<String, Serializable> securityKeyValues = new HashMap<>();
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(table.getRecordSecurityLocks()))
      {
         securityKeyValues.put(recordSecurityLock.getSecurityKeyType(), record == null ? null : record.getValue(recordSecurityLock.getFieldName()));
      }
      return securityKeyValues;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static AuditLevel getAuditLevel(AbstractTableActionInput tableActionInput)
   {
      QTableMetaData table = tableActionInput.getTable();
      if(table.getAuditRules() == null)
      {
         return (AuditLevel.NONE);
      }

      return (table.getAuditRules().getAuditLevel());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private enum DMLType
   {
      INSERT("Inserted", true),
      UPDATE("Edited", true),
      DELETE("Deleted", false),
      OTHER("Processed", false);

      private final String  pastTenseVerb;
      private final boolean supportsFields;



      /*******************************************************************************
       **
       *******************************************************************************/
      DMLType(String pastTenseVerb, boolean supportsFields)
      {
         this.pastTenseVerb = pastTenseVerb;
         this.supportsFields = supportsFields;
      }
   }
}
