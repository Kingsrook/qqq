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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles.SavedBulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfileField;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertStepUtils
{

   /***************************************************************************
    **
    ***************************************************************************/
   public static StorageInput getStorageInputForTheFile(RunBackendStepInput input) throws QException
   {
      @SuppressWarnings("unchecked")
      ArrayList<StorageInput> storageInputs = (ArrayList<StorageInput>) input.getValue("theFile");
      if(storageInputs == null)
      {
         throw (new QException("StorageInputs for theFile were not found in process state"));
      }

      if(storageInputs.isEmpty())
      {
         throw (new QException("StorageInputs for theFile was an empty list"));
      }

      StorageInput storageInput = storageInputs.get(0);
      return (storageInput);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void setNextStepStreamedETLPreview(RunBackendStepOutput runBackendStepOutput)
   {
      runBackendStepOutput.setOverrideLastStepName("receiveValueMapping");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void setNextStepPrepareValueMapping(RunBackendStepOutput runBackendStepOutput)
   {
      runBackendStepOutput.setOverrideLastStepName("receiveFileMapping");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static BulkLoadProfile getBulkLoadProfile(RunBackendStepInput runBackendStepInput)
   {
      String version = runBackendStepInput.getValueString("version");
      if("v1".equals(version))
      {
         String  layout       = runBackendStepInput.getValueString("layout");
         Boolean hasHeaderRow = runBackendStepInput.getValueBoolean("hasHeaderRow");

         ArrayList<BulkLoadProfileField> fieldList = new ArrayList<>();

         JSONArray array = new JSONArray(runBackendStepInput.getValueString("fieldListJSON"));
         for(int i = 0; i < array.length(); i++)
         {
            JSONObject           jsonObject           = array.getJSONObject(i);
            BulkLoadProfileField bulkLoadProfileField = new BulkLoadProfileField();
            fieldList.add(bulkLoadProfileField);
            bulkLoadProfileField.setFieldName(jsonObject.optString("fieldName"));
            bulkLoadProfileField.setHeaderName(jsonObject.has("headerName") ? jsonObject.getString("headerName") : null);
            bulkLoadProfileField.setColumnIndex(jsonObject.has("columnIndex") ? jsonObject.getInt("columnIndex") : null);
            bulkLoadProfileField.setDefaultValue((Serializable) jsonObject.opt("defaultValue"));
            bulkLoadProfileField.setDoValueMapping(jsonObject.optBoolean("doValueMapping"));

            if(BooleanUtils.isTrue(bulkLoadProfileField.getDoValueMapping()) && jsonObject.has("valueMappings"))
            {
               bulkLoadProfileField.setValueMappings(new HashMap<>());
               JSONObject valueMappingsJsonObject = jsonObject.getJSONObject("valueMappings");
               for(String fileValue : valueMappingsJsonObject.keySet())
               {
                  bulkLoadProfileField.getValueMappings().put(fileValue, ValueUtils.getValueAsString(valueMappingsJsonObject.get(fileValue)));
               }
            }
         }

         BulkLoadProfile bulkLoadProfile = new BulkLoadProfile()
            .withVersion(version)
            .withFieldList(fieldList)
            .withHasHeaderRow(hasHeaderRow)
            .withLayout(layout);

         return (bulkLoadProfile);
      }
      else
      {
         throw (new IllegalArgumentException("Unexpected version for bulk load profile: " + version));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void handleSavedBulkLoadProfileIdValue(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      Integer savedBulkLoadProfileId = runBackendStepInput.getValueInteger("savedBulkLoadProfileId");
      if(savedBulkLoadProfileId != null)
      {
         QRecord savedBulkLoadProfileRecord = GetAction.execute(SavedBulkLoadProfile.TABLE_NAME, savedBulkLoadProfileId);
         runBackendStepOutput.addValue("savedBulkLoadProfileRecord", savedBulkLoadProfileRecord);
      }
   }
}
