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
import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
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



   /*******************************************************************************
    **
    *******************************************************************************/
   public String formatValue(QFieldMetaData field, Serializable value)
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
      if(StringUtils.hasContent(field.getDisplayFormat()))
      {
         try
         {
            return (field.getDisplayFormat().formatted(value));
         }
         catch(Exception e)
         {
            try
            {
               // todo - revisit if we actually want this - or - if you should get an error if you mis-configure your table this way (ideally during validation!)
               if(e.getMessage().equals("f != java.lang.Integer"))
               {
                  return formatValue(field, ValueUtils.getValueAsBigDecimal(value));
               }
               else if(e.getMessage().equals("d != java.math.BigDecimal"))
               {
                  return formatValue(field, ValueUtils.getValueAsInteger(value));
               }
               else
               {
                  LOG.warn("Error formatting value [" + value + "] for field [" + field.getName() + "] with format [" + field.getDisplayFormat() + "]: " + e.getMessage());
               }
            }
            catch(Exception e2)
            {
               LOG.warn("Caught secondary exception trying to convert type on field [" + field.getName() + "] for formatting", e);
            }
         }
      }

      ////////////////////////////////////////
      // by default, just get back a string //
      ////////////////////////////////////////
      return (ValueUtils.getValueAsString(value));
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

      ///////////////////////////////////////////////////////////////////////
      // get list of values, then pass them to the string formatter method //
      ///////////////////////////////////////////////////////////////////////
      try
      {
         List<Serializable> values = table.getRecordLabelFields().stream()
            .map(record::getValue)
            .map(v -> v == null ? "" : v)
            .toList();
         return (table.getRecordLabelFormat().formatted(values.toArray()));
      }
      catch(Exception e)
      {
         return (formatRecordLabelExceptionalCases(table, record));
      }
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
            String formattedValue = formatValue(field, record.getValue(field.getName()));
            record.setDisplayValue(field.getName(), formattedValue);
         }

         record.setRecordLabel(formatRecordLabel(table, record));
      }
   }

}
