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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.basic;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.adapters.JsonToQFieldMappingAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for performing the Extract step of a basic ETL process.
 *******************************************************************************/
public class BasicETLTransformFunction implements FunctionBody
{
   private static final Logger LOG = LogManager.getLogger(BasicETLTransformFunction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////
      // exit early with no-op if no records made it here, or if we don't have a mapping to use //
      ////////////////////////////////////////////////////////////////////////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(runFunctionRequest.getRecords()))
      {
         return;
      }

      String mappingJSON = runFunctionRequest.getValueString(BasicETLProcess.FIELD_MAPPING_JSON);
      if(!StringUtils.hasContent(mappingJSON))
      {
         return;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////
      // require that the mapping be a key-based mapping (can't use indexes into qRecord values) //
      /////////////////////////////////////////////////////////////////////////////////////////////
      AbstractQFieldMapping<?> mapping = new JsonToQFieldMappingAdapter().buildMappingFromJson(mappingJSON);
      if(!(mapping instanceof QKeyBasedFieldMapping keyBasedFieldMapping))
      {
         throw (new QException("Mapping was not a Key-based mapping type.  Was a : " + mapping.getClass().getName()));
      }

      String         tableName     = runFunctionRequest.getValueString(BasicETLProcess.FIELD_DESTINATION_TABLE);
      QTableMetaData table         = runFunctionRequest.getInstance().getTable(tableName);
      List<QRecord>  mappedRecords = applyMapping(runFunctionRequest.getRecords(), table, keyBasedFieldMapping);

      //////////////////////////////////////////////////////////////////////////////////////////////////////
      // todo - should this be conditional, e.g., driven by a field, or an opt-in customization function? //
      //////////////////////////////////////////////////////////////////////////////////////////////////////
      removeNonNumericValuesFromMappedRecords(table, mappedRecords);

      runFunctionResult.setRecords(mappedRecords);
   }



   /*******************************************************************************
    ** Note:  package-private for direct unit-testability
    *******************************************************************************/
   void removeNonNumericValuesFromMappedRecords(QTableMetaData table, List<QRecord> records)
   {
      for(QRecord record : records)
      {
         for(QFieldMetaData field : table.getFields().values())
         {
            Serializable value = record.getValue(field.getName());
            if(value != null && StringUtils.hasContent(String.valueOf(value)))
            {
               record.setValue(field.getName(), tryToParseNumber(field.getType(), value));
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable tryToParseNumber(QFieldType fieldType, Serializable value)
   {
      if(value == null)
      {
         //////////////////////////////
         // null input = null output //
         //////////////////////////////
         return (null);
      }

      ////////////////////////////////////////////////////
      // get a string version of the value to work with //
      ////////////////////////////////////////////////////
      String stringValue = String.valueOf(value);
      try
      {
         ////////////////////////////////////////////////////////////////////////////////
         // based on field type, try to parse - noting bad formatted values will throw //
         ////////////////////////////////////////////////////////////////////////////////
         if(fieldType.equals(QFieldType.INTEGER))
         {
            Integer.parseInt(stringValue);
         }
         else if(fieldType.equals(QFieldType.DECIMAL))
         {
            new BigDecimal(stringValue);
         }

         //////////////////////////////////////////////////////////////////////////////////
         // if we got through the parsing without throwing, then we can return the value //
         //////////////////////////////////////////////////////////////////////////////////
         return (value);
      }
      catch(NumberFormatException nfe)
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // upon number format exception, look for commas - if found, strip them out and try again (recursive call) //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(stringValue.contains(","))
         {
            stringValue = stringValue.replaceAll(",", "");
            return (tryToParseNumber(fieldType, stringValue));
         }

         /////////////////////////////////////////////////////////////////////////////////
         // else, if no comma, then we want a null value here, rather than a non-number //
         /////////////////////////////////////////////////////////////////////////////////
         LOG.debug("Nulling out value " + value + " that will not work in a " + fieldType + " field");
         return (null);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> applyMapping(List<QRecord> input, QTableMetaData table, QKeyBasedFieldMapping mapping)
   {
      List<QRecord> output = new ArrayList<>();
      for(QRecord inputRecord : input)
      {
         QRecord outputRecord = new QRecord();
         output.add(outputRecord);
         for(QFieldMetaData field : table.getFields().values())
         {
            String fieldSource = mapping.getFieldSource(field.getName());
            outputRecord.setValue(field.getName(), inputRecord.getValue(fieldSource));
         }
      }
      return (output);
   }

}
