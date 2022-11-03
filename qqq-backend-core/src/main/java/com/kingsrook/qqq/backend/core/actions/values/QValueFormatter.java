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

package com.kingsrook.qqq.backend.core.actions.values;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility to apply display formats to values for records and fields.
 **
 *******************************************************************************/
public class QValueFormatter
{
   private static final Logger LOG = LogManager.getLogger(QValueFormatter.class);

   private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
   private static DateTimeFormatter dateFormatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd");



   /*******************************************************************************
    ** For a field, and its value, apply the field's displayFormat.
    *******************************************************************************/
   public String formatValue(QFieldMetaData field, Serializable value)
   {
      if(QFieldType.BOOLEAN.equals(field.getType()))
      {
         Boolean b = ValueUtils.getValueAsBoolean(value);
         if(b == null)
         {
            return (null);
         }
         else if(b)
         {
            return ("Yes");
         }
         else
         {
            return ("No");
         }
      }

      return (formatValue(field.getDisplayFormat(), field.getName(), value));
   }



   /*******************************************************************************
    ** For a display format string (e.g., %d), and a value, apply the displayFormat.
    *******************************************************************************/
   public String formatValue(String displayFormat, Serializable value)
   {
      return (formatValue(displayFormat, "", value));
   }



   /*******************************************************************************
    ** For a display format string, an optional fieldName (only used for logging),
    ** and a value, apply the format.
    *******************************************************************************/
   private String formatValue(String displayFormat, String fieldName, Serializable value)
   {
      //////////////////////////////////
      // null values get null results //
      //////////////////////////////////
      if(value == null)
      {
         return (null);
      }

      ////////////////////////////////////////////////////////
      // if the field has a display format, try to apply it //
      ////////////////////////////////////////////////////////
      if(StringUtils.hasContent(displayFormat))
      {
         try
         {
            return (displayFormat.formatted(value));
         }
         catch(Exception e)
         {
            try
            {
               // todo - revisit if we actually want this - or - if you should get an error if you mis-configure your table this way (ideally during validation!)
               if(e.getMessage().equals("f != java.lang.Integer"))
               {
                  return formatValue(displayFormat, ValueUtils.getValueAsBigDecimal(value));
               }
               else if(e.getMessage().equals("f != java.lang.String"))
               {
                  return formatValue(displayFormat, ValueUtils.getValueAsBigDecimal(value));
               }
               else if(e.getMessage().equals("d != java.math.BigDecimal"))
               {
                  return formatValue(displayFormat, ValueUtils.getValueAsInteger(value));
               }
               else
               {
                  LOG.warn("Error formatting value [" + value + "] for field [" + fieldName + "] with format [" + displayFormat + "]: " + e.getMessage());
               }
            }
            catch(Exception e2)
            {
               LOG.warn("Caught secondary exception trying to convert type on field [" + fieldName + "] for formatting", e);
            }
         }
      }

      ////////////////////////////////////////
      // by default, just get back a string //
      ////////////////////////////////////////
      return (ValueUtils.getValueAsString(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String formatDate(LocalDate date)
   {
      return (dateFormatter.format(date));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String formatDateTime(LocalDateTime dateTime)
   {
      return (dateTimeFormatter.format(dateTime));
   }



   /*******************************************************************************
    ** Make a string from a table's recordLabelFormat and fields, for a given record.
    *******************************************************************************/
   public String formatRecordLabel(QTableMetaData table, QRecord record)
   {
      if(!StringUtils.hasContent(table.getRecordLabelFormat()))
      {
         return (formatRecordLabelExceptionalCases(table, record));
      }

      try
      {
         return formatStringWithFields(table.getRecordLabelFormat(), table.getRecordLabelFields(), record.getDisplayValues(), record.getValues());
      }
      catch(Exception e)
      {
         LOG.debug("Error formatting record label", e);
         return (formatRecordLabelExceptionalCases(table, record));
      }
   }



   /*******************************************************************************
    ** For a given format string, and a list of fields, look in displayValueMap and
    ** rawValueMap to get the values to apply to the format.
    *******************************************************************************/
   private String formatStringWithFields(String formatString, List<String> formatFields, Map<String, String> displayValueMap, Map<String, Serializable> rawValueMap)
   {
      List<Serializable> values = formatFields.stream()
         .map(fieldName ->
         {
            ///////////////////////////////////////////////////////////////////////////
            // if there's a display value set, then use it.  Else, use the raw value //
            ///////////////////////////////////////////////////////////////////////////
            String displayValue = displayValueMap.get(fieldName);
            if(displayValue != null)
            {
               return (displayValue);
            }
            return rawValueMap.get(fieldName);
         })
         .map(v -> v == null ? "" : v)
         .toList();
      return (formatString.formatted(values.toArray()));
   }



   /*******************************************************************************
    ** For a given format string, and a list of values, apply the format.  Note, null
    ** values in the list become "".
    *******************************************************************************/
   public String formatStringWithValues(String formatString, List<String> formatValues)
   {
      List<String> values = formatValues.stream()
         .map(v -> v == null ? "" : v)
         .toList();
      return (formatString.formatted(values.toArray()));
   }



   /*******************************************************************************
    ** Deal with non-happy-path cases for making a record label.
    *******************************************************************************/
   private String formatRecordLabelExceptionalCases(QTableMetaData table, QRecord record)
   {
      ///////////////////////////////////////////////////////////////////////////////////////
      // if there's no record label format, then just return the primary key display value //
      ///////////////////////////////////////////////////////////////////////////////////////
      String pkeyDisplayValue = record.getDisplayValue(table.getPrimaryKeyField());
      if(StringUtils.hasContent(pkeyDisplayValue))
      {
         return (pkeyDisplayValue);
      }

      String pkeyRawValue = ValueUtils.getValueAsString(record.getValue(table.getPrimaryKeyField()));
      if(StringUtils.hasContent(pkeyRawValue))
      {
         return (pkeyRawValue);
      }

      ///////////////////////////////////////////////////////////////////////////////
      // worst case scenario, return empty string, but never null from this method //
      ///////////////////////////////////////////////////////////////////////////////
      return ("");
   }



   /*******************************************************************************
    ** For a list of records, set their recordLabels and display values
    *******************************************************************************/
   public void setDisplayValuesInRecords(QTableMetaData table, List<QRecord> records)
   {
      if(records == null)
      {
         return;
      }

      for(QRecord record : records)
      {
         for(QFieldMetaData field : table.getFields().values())
         {
            if(record.getDisplayValue(field.getName()) == null)
            {
               String formattedValue = formatValue(field, record.getValue(field.getName()));
               record.setDisplayValue(field.getName(), formattedValue);
            }
         }

         record.setRecordLabel(formatRecordLabel(table, record));
      }
   }

}
