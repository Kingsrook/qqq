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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class DateTimeDisplayValueBehavior implements FieldDisplayBehavior<DateTimeDisplayValueBehavior>
{
   private static final QLogger LOG = QLogger.getLogger(DateTimeDisplayValueBehavior.class);

   private String zoneIdFromFieldName;
   private String defaultZoneId;

   private static DateTimeDisplayValueBehavior NOOP = new DateTimeDisplayValueBehavior();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DateTimeDisplayValueBehavior getDefault()
   {
      return NOOP;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field)
   {
      if(StringUtils.hasContent(defaultZoneId))
      {
         applyDefaultZoneId(recordList, table, field);
      }
      else if(StringUtils.hasContent(zoneIdFromFieldName))
      {
         applyZoneIdFromFieldName(recordList, table, field);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void applyDefaultZoneId(List<QRecord> recordList, QTableMetaData table, QFieldMetaData field)
   {
      for(QRecord record : CollectionUtils.nonNullList(recordList))
      {
         try
         {
            Instant       instant       = record.getValueInstant(field.getName());
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(defaultZoneId));
            record.setDisplayValue(field.getName(), QValueFormatter.formatDateTimeWithZone(zonedDateTime));
         }
         catch(Exception e)
         {
            LOG.info("Error applying defaultZoneId DateTimeDisplayValueBehavior", logPair("table", table.getName()), logPair("field", field.getName()), logPair("id", record.getValue(table.getPrimaryKeyField())));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void applyZoneIdFromFieldName(List<QRecord> recordList, QTableMetaData table, QFieldMetaData field)
   {
      for(QRecord record : CollectionUtils.nonNullList(recordList))
      {
         try
         {
            Instant       instant       = record.getValueInstant(field.getName());
            String        zoneId        = record.getValueString(zoneIdFromFieldName);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));
            record.setDisplayValue(field.getName(), QValueFormatter.formatDateTimeWithZone(zonedDateTime));
         }
         catch(Exception e)
         {
            LOG.info("Error applying zoneIdFromFieldName DateTimeDisplayValueBehavior", logPair("table", table.getName()), logPair("field", field.getName()), logPair("id", record.getValue(table.getPrimaryKeyField())));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<String> validateBehaviorConfiguration(QTableMetaData tableMetaData, QFieldMetaData fieldMetaData)
   {
      List<String> errors = new ArrayList<>();
      String errorSuffix = " field [" + fieldMetaData.getName() + "] in table [" + tableMetaData.getName() + "]";

      if(!QFieldType.DATE_TIME.equals(fieldMetaData.getType()))
      {
         errors.add("A DateTimeDisplayValueBehavior was a applied to a non-DATE_TIME" + errorSuffix);
      }

      if(StringUtils.hasContent(zoneIdFromFieldName))
      {
         if(!tableMetaData.getFields().containsKey(zoneIdFromFieldName))
         {
            errors.add("Unrecognized field name [" + zoneIdFromFieldName + "] for [zoneIdFromFieldName] in DateTimeDisplayValueBehavior on" + errorSuffix);
         }
         else
         {
            QFieldMetaData zoneIdField = tableMetaData.getFields().get(zoneIdFromFieldName);
            if(!QFieldType.STRING.equals(zoneIdField.getType()))
            {
               errors.add("A non-STRING type [" + zoneIdField.getType() + "] was specified as the zoneIdFromFieldName field [" + zoneIdFromFieldName + "] in DateTimeDisplayValueBehavior on" + errorSuffix);
            }
         }
      }

      return (errors);
   }



   /*******************************************************************************
    ** Getter for zoneIdFromFieldName
    *******************************************************************************/
   public String getZoneIdFromFieldName()
   {
      return (this.zoneIdFromFieldName);
   }



   /*******************************************************************************
    ** Setter for zoneIdFromFieldName
    *******************************************************************************/
   public void setZoneIdFromFieldName(String zoneIdFromFieldName)
   {
      this.zoneIdFromFieldName = zoneIdFromFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for zoneIdFromFieldName
    *******************************************************************************/
   public DateTimeDisplayValueBehavior withZoneIdFromFieldName(String zoneIdFromFieldName)
   {
      this.zoneIdFromFieldName = zoneIdFromFieldName;
      return (this);
   }


   /*******************************************************************************
    ** Getter for defaultZoneId
    *******************************************************************************/
   public String getDefaultZoneId()
   {
      return (this.defaultZoneId);
   }



   /*******************************************************************************
    ** Setter for defaultZoneId
    *******************************************************************************/
   public void setDefaultZoneId(String defaultZoneId)
   {
      this.defaultZoneId = defaultZoneId;
   }



   /*******************************************************************************
    ** Fluent setter for defaultZoneId
    *******************************************************************************/
   public DateTimeDisplayValueBehavior withDefaultZoneId(String defaultZoneId)
   {
      this.defaultZoneId = defaultZoneId;
      return (this);
   }


}
