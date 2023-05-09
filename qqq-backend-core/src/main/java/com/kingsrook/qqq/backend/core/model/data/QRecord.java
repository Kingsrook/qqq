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

package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.QErrorMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QWarningMessage;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang.SerializationUtils;


/*******************************************************************************
 ** Data Record within qqq.  e.g., a single row from a database.
 **
 ** Actual values (e.g., as stored in the backend system) are in the `values`
 ** map.  Keys in this map are fieldNames from the QTableMetaData.
 **
 ** "Display values" (e.g., labels for possible values, or formatted numbers
 ** (e.g., quantities with commas)) are in the displayValues map.
 **
 ** backendDetails are additional data about a record, that aren't strictly
 ** values, but are more like meta-data - e.g., for a file-backend, what file the
 ** record came from.
 **
 ** Errors are meant to hold information about things that went wrong when
 ** processing a record - e.g., in a list of records that may be the output of an
 ** action, like a bulk load.  Warnings play a similar role, but are just advice
 ** - they don't mean that the action was failed, just something you may need to know.
 *******************************************************************************/
public class QRecord implements Serializable
{
   private String tableName;
   private String recordLabel;

   private Map<String, Serializable> values         = new LinkedHashMap<>();
   private Map<String, String>       displayValues  = new LinkedHashMap<>();
   private Map<String, Serializable> backendDetails = new LinkedHashMap<>();

   private List<QErrorMessage>   errors   = new ArrayList<>();
   private List<QWarningMessage> warnings = new ArrayList<>();

   private Map<String, List<QRecord>> associatedRecords = new HashMap<>();

   public static final String BACKEND_DETAILS_TYPE_JSON_SOURCE_OBJECT = "jsonSourceObject";



   /*******************************************************************************
    ** Default constructor.
    *******************************************************************************/
   public QRecord()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord(QTableMetaData tableMetaData, Serializable primaryKeyValue)
   {
      setTableName(tableMetaData.getName());
      setValue(tableMetaData.getPrimaryKeyField(), primaryKeyValue);
   }



   /*******************************************************************************
    ** Copy constructor.
    **
    *******************************************************************************/
   public QRecord(QRecord record)
   {
      this.tableName = record.tableName;
      this.recordLabel = record.recordLabel;

      this.values = doDeepCopy(record.values);
      this.displayValues = doDeepCopy(record.displayValues);
      this.backendDetails = doDeepCopy(record.backendDetails);
      this.errors = doDeepCopy(record.errors);
      this.warnings = doDeepCopy(record.warnings);
      this.associatedRecords = doDeepCopy(record.associatedRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "QRecord{tableName='" + tableName + "',id='" + getValue("id") + "'}";
   }



   /*******************************************************************************
    ** todo - move to a cloning utils maybe?
    *******************************************************************************/
   @SuppressWarnings({ "unchecked" })
   private <K, V> Map<K, V> doDeepCopy(Map<K, V> map)
   {
      if(map == null)
      {
         return (null);
      }

      if(map instanceof Serializable serializableMap)
      {
         return (Map<K, V>) SerializationUtils.clone(serializableMap);
      }

      return (new LinkedHashMap<>(map));
   }



   /*******************************************************************************
    ** todo - move to a cloning utils maybe?
    *******************************************************************************/
   @SuppressWarnings({ "unchecked" })
   private <T> List<T> doDeepCopy(List<T> list)
   {
      if(list == null)
      {
         return (null);
      }

      if(list instanceof Serializable serializableList)
      {
         return (List<T>) SerializationUtils.clone(serializableList);
      }

      return (new ArrayList<>(list));
   }



   /*******************************************************************************
    ** Added when QRecords got exposed in scripts, and passing a constant String
    ** raised a class-cast exception, because it was some nashorn non-serializable
    ** type (though once inside *this* method, the value was a java.lang.String...)
    *******************************************************************************/
   public void setValue(String fieldName, Object value)
   {
      if(value == null)
      {
         setValue(fieldName, null);
      }
      else if(value instanceof Serializable s)
      {
         setValue(fieldName, s);
      }
      else
      {
         setValue(fieldName, ValueUtils.getValueAsString(value));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValue(String fieldName, Serializable value)
   {
      values.put(fieldName, value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void removeValue(String fieldName)
   {
      values.remove(fieldName);
      displayValues.remove(fieldName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValue(QFieldMetaData field, Serializable value)
   {
      values.put(field.getName(), value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord withValue(String fieldName, Serializable value)
   {
      setValue(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setDisplayValue(String fieldName, String displayValue)
   {
      displayValues.put(fieldName, displayValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord withDisplayValue(String fieldName, String displayValue)
   {
      setDisplayValue(fieldName, displayValue);
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public QRecord withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordLabel
    **
    *******************************************************************************/
   public String getRecordLabel()
   {
      return recordLabel;
   }



   /*******************************************************************************
    ** Setter for recordLabel
    **
    *******************************************************************************/
   public void setRecordLabel(String recordLabel)
   {
      this.recordLabel = recordLabel;
   }



   /*******************************************************************************
    ** Fluent setter for recordLabel
    **
    *******************************************************************************/
   public QRecord withRecordLabel(String recordLabel)
   {
      this.recordLabel = recordLabel;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Serializable getValue(String fieldName)
   {
      return (values.get(fieldName));
   }



   /*******************************************************************************
    ** Getter for displayValues
    **
    *******************************************************************************/
   public Map<String, String> getDisplayValues()
   {
      return displayValues;
   }



   /*******************************************************************************
    ** Setter for displayValues
    **
    *******************************************************************************/
   public void setDisplayValues(Map<String, String> displayValues)
   {
      this.displayValues = displayValues;
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public String getDisplayValue(String fieldName)
   {
      return (displayValues.get(fieldName));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public String getValueString(String fieldName)
   {
      return (ValueUtils.getValueAsString(values.get(fieldName)));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Integer getValueInteger(String fieldName)
   {
      return (ValueUtils.getValueAsInteger(values.get(fieldName)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public BigDecimal getValueBigDecimal(String fieldName)
   {
      return (ValueUtils.getValueAsBigDecimal(values.get(fieldName)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Boolean getValueBoolean(String fieldName)
   {
      return (ValueUtils.getValueAsBoolean(values.get(fieldName)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public LocalTime getValueLocalTime(String fieldName)
   {
      return (ValueUtils.getValueAsLocalTime(values.get(fieldName)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public LocalDate getValueLocalDate(String fieldName)
   {
      return (ValueUtils.getValueAsLocalDate(values.get(fieldName)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public byte[] getValueByteArray(String fieldName)
   {
      return (ValueUtils.getValueAsByteArray(values.get(fieldName)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Instant getValueInstant(String fieldName)
   {
      return (ValueUtils.getValueAsInstant(values.get(fieldName)));
   }



   /*******************************************************************************
    ** Getter for backendDetails
    **
    *******************************************************************************/
   public Map<String, Serializable> getBackendDetails()
   {
      return backendDetails;
   }



   /*******************************************************************************
    ** Setter for backendDetails
    **
    *******************************************************************************/
   public void setBackendDetails(Map<String, Serializable> backendDetails)
   {
      this.backendDetails = backendDetails;
   }



   /*******************************************************************************
    ** Add one backendDetail to this record
    **
    *******************************************************************************/
   public void addBackendDetail(String key, Serializable value)
   {
      this.backendDetails.put(key, value);
   }



   /*******************************************************************************
    ** Fluently Add one backendDetail to this record
    **
    *******************************************************************************/
   public QRecord withBackendDetail(String key, Serializable value)
   {
      addBackendDetail(key, value);
      return (this);
   }



   /*******************************************************************************
    ** Get one backendDetail from this record
    **
    *******************************************************************************/
   public Serializable getBackendDetail(String key)
   {
      if(!this.backendDetails.containsKey(key))
      {
         return (null);
      }

      return this.backendDetails.get(key);
   }



   /*******************************************************************************
    ** Get one backendDetail from this record as a String
    **
    *******************************************************************************/
   public String getBackendDetailString(String key)
   {
      return (String) getBackendDetail(key);
   }



   /*******************************************************************************
    ** Getter for errors
    **
    *******************************************************************************/
   public List<QErrorMessage> getErrors()
   {
      return (errors);
   }



   /*******************************************************************************
    ** Setter for errors
    **
    *******************************************************************************/
   public void setErrors(List<QErrorMessage> errors)
   {
      this.errors = errors;
   }



   /*******************************************************************************
    ** Add one error to this record
    **
    *******************************************************************************/
   public void addError(QErrorMessage error)
   {
      this.errors.add(error);
   }



   /*******************************************************************************
    ** Fluently Add one error to this record
    **
    *******************************************************************************/
   public QRecord withError(QErrorMessage error)
   {
      addError(error);
      return (this);
   }



   /*******************************************************************************
    ** Convert this record to an QRecordEntity
    *******************************************************************************/
   public <T extends QRecordEntity> T toEntity(Class<T> c) throws QException
   {
      return (QRecordEntity.fromQRecord(c, this));
   }



   /*******************************************************************************
    ** Getter for associatedRecords
    *******************************************************************************/
   public Map<String, List<QRecord>> getAssociatedRecords()
   {
      return (this.associatedRecords);
   }



   /*******************************************************************************
    ** Setter for associatedRecords
    *******************************************************************************/
   public void setAssociatedRecords(Map<String, List<QRecord>> associatedRecords)
   {
      this.associatedRecords = associatedRecords;
   }



   /*******************************************************************************
    ** Fluent setter for associatedRecords
    *******************************************************************************/
   public QRecord withAssociatedRecords(Map<String, List<QRecord>> associatedRecords)
   {
      this.associatedRecords = associatedRecords;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for associatedRecords
    *******************************************************************************/
   public QRecord withAssociatedRecords(String name, List<QRecord> associatedRecords)
   {
      if(this.associatedRecords == null)
      {
         this.associatedRecords = new HashMap<>();
      }
      this.associatedRecords.put(name, associatedRecords);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for associatedRecord
    *******************************************************************************/
   public QRecord withAssociatedRecord(String name, QRecord associatedRecord)
   {
      if(this.associatedRecords == null)
      {
         this.associatedRecords = new HashMap<>();
      }
      this.associatedRecords.putIfAbsent(name, new ArrayList<>());
      this.associatedRecords.get(name).add(associatedRecord);
      return (this);
   }



   /*******************************************************************************
    ** Getter for warnings
    *******************************************************************************/
   public List<QWarningMessage> getWarnings()
   {
      return (this.warnings);
   }



   /*******************************************************************************
    ** Setter for warnings
    *******************************************************************************/
   public void setWarnings(List<QWarningMessage> warnings)
   {
      this.warnings = warnings;
   }



   /*******************************************************************************
    ** Fluent setter for warnings
    *******************************************************************************/
   public QRecord withWarnings(List<QWarningMessage> warnings)
   {
      this.warnings = warnings;
      return (this);
   }



   /*******************************************************************************
    ** Add one warning to this record
    **
    *******************************************************************************/
   public void addWarning(QWarningMessage warning)
   {
      this.warnings.add(warning);
   }

}
