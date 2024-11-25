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


import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfileField;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertReceiveFileMappingStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(BulkInsertReceiveFileMappingStep.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         BulkInsertStepUtils.handleSavedBulkLoadProfileIdValue(runBackendStepInput, runBackendStepOutput);

         ///////////////////////////////////////////////////////////////////
         // read process values - construct a bulkLoadProfile out of them //
         ///////////////////////////////////////////////////////////////////
         BulkLoadProfile bulkLoadProfile = BulkInsertStepUtils.getBulkLoadProfile(runBackendStepInput);

         /////////////////////////////////////////////////////////////////////////
         // put the list of bulk load profile into the process state - it's the //
         // thing that the frontend will be looking at as the saved profile     //
         /////////////////////////////////////////////////////////////////////////
         runBackendStepOutput.addValue("bulkLoadProfile", bulkLoadProfile);

         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         // now build the mapping object that the backend wants - based on the bulkLoadProfile from the frontend //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         BulkInsertMapping bulkInsertMapping = new BulkInsertMapping();
         bulkInsertMapping.setTableName(runBackendStepInput.getTableName());
         bulkInsertMapping.setHasHeaderRow(bulkLoadProfile.getHasHeaderRow());
         bulkInsertMapping.setLayout(BulkInsertMapping.Layout.valueOf(bulkLoadProfile.getLayout()));

         //////////////////////////////////////////////////////////////////////////////////////////////
         // handle field to name or index mappings (depending on if there's a header row being used) //
         //////////////////////////////////////////////////////////////////////////////////////////////
         if(BooleanUtils.isTrue(bulkInsertMapping.getHasHeaderRow()))
         {
            StorageInput storageInput = BulkInsertStepUtils.getStorageInputForTheFile(runBackendStepInput);
            try
               (
                  InputStream inputStream = new StorageAction().getInputStream(storageInput);
                  FileToRowsInterface fileToRowsInterface = FileToRowsInterface.forFile(storageInput.getReference(), inputStream);
               )
            {
               Map<String, String> fieldNameToHeaderNameMap = new HashMap<>();
               bulkInsertMapping.setFieldNameToHeaderNameMap(fieldNameToHeaderNameMap);

               BulkLoadFileRow headerRow = fileToRowsInterface.next();
               for(BulkLoadProfileField bulkLoadProfileField : bulkLoadProfile.getFieldList())
               {
                  if(bulkLoadProfileField.getHeaderName() != null)
                  {
                     String headerName = bulkLoadProfileField.getHeaderName();
                     fieldNameToHeaderNameMap.put(bulkLoadProfileField.getFieldName(), headerName);
                  }
                  else if(bulkLoadProfileField.getColumnIndex() != null)
                  {
                     String headerName = ValueUtils.getValueAsString(headerRow.getValueElseNull(bulkLoadProfileField.getColumnIndex()));
                     fieldNameToHeaderNameMap.put(bulkLoadProfileField.getFieldName(), headerName);
                  }
               }
            }
         }
         else
         {
            Map<String, Integer> fieldNameToIndexMap = new HashMap<>();
            bulkInsertMapping.setFieldNameToIndexMap(fieldNameToIndexMap);
            for(BulkLoadProfileField bulkLoadProfileField : bulkLoadProfile.getFieldList())
            {
               if(bulkLoadProfileField.getColumnIndex() != null)
               {
                  fieldNameToIndexMap.put(bulkLoadProfileField.getFieldName(), bulkLoadProfileField.getColumnIndex());
               }
            }
         }

         /////////////////////////////////////
         // do fields w/ default values now //
         /////////////////////////////////////
         HashMap<String, Serializable> fieldNameToDefaultValueMap = new HashMap<>();
         bulkInsertMapping.setFieldNameToDefaultValueMap(fieldNameToDefaultValueMap);
         for(BulkLoadProfileField bulkLoadProfileField : bulkLoadProfile.getFieldList())
         {
            if(bulkLoadProfileField.getDefaultValue() != null)
            {
               fieldNameToDefaultValueMap.put(bulkLoadProfileField.getFieldName(), bulkLoadProfileField.getDefaultValue());
            }
         }

         /////////////////////////////////////////////////////////////////////////////////////////////
         // frontend at this point will have sent just told us which field names need value mapping //
         // store those - and let them drive the value-mapping screens that we'll go through next   //
         // todo - uh, what if those come from profile, dummy!?
         /////////////////////////////////////////////////////////////////////////////////////////////
         ArrayList<String> fieldNamesToDoValueMapping = new ArrayList<>();
         for(BulkLoadProfileField bulkLoadProfileField : bulkLoadProfile.getFieldList())
         {
            if(BooleanUtils.isTrue(bulkLoadProfileField.getDoValueMapping()))
            {
               fieldNamesToDoValueMapping.add(bulkLoadProfileField.getFieldName());

               if(CollectionUtils.nullSafeHasContents(bulkLoadProfileField.getValueMappings()))
               {
                  bulkInsertMapping.getFieldNameToValueMapping().put(bulkLoadProfileField.getFieldName(), bulkLoadProfileField.getValueMappings());
               }
            }
         }
         runBackendStepOutput.addValue("fieldNamesToDoValueMapping", new ArrayList<>(fieldNamesToDoValueMapping));

         ///////////////////////////////////////////////////////////////////////////////////////
         // figure out what associations are being mapped, by looking at the full field names //
         ///////////////////////////////////////////////////////////////////////////////////////
         Set<String> associationNameSet = new HashSet<>();
         for(BulkLoadProfileField bulkLoadProfileField : bulkLoadProfile.getFieldList())
         {
            if(bulkLoadProfileField.getFieldName().contains("."))
            {
               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               // handle parent.child.grandchild.fieldName,index.index.index if we do sub-indexes for grandchildren... //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               String fieldNameBeforeIndex = bulkLoadProfileField.getFieldName().split(",")[0];
               associationNameSet.add(fieldNameBeforeIndex.substring(0, fieldNameBeforeIndex.lastIndexOf('.')));
            }
         }
         bulkInsertMapping.setMappedAssociations(new ArrayList<>(associationNameSet));

         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // at this point we're done populating the bulkInsertMapping object.  put it in the process state. //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         runBackendStepOutput.addValue("bulkInsertMapping", bulkInsertMapping);

         if(CollectionUtils.nullSafeHasContents(fieldNamesToDoValueMapping))
         {
            //////////////////////////////////////////////////////////////////////////////////
            // just go to the prepareValueMapping backend step - it'll figure out the rest. //
            // it's also where the value-mapping loop of steps points.                      //
            // and, this will actually be the default (e.g., the step after this one).      //
            //////////////////////////////////////////////////////////////////////////////////
         }
         else
         {
            //////////////////////////////////////////////////////////////////////////////////
            // else - if no values to map - continue with the standard streamed-ETL preview //
            //////////////////////////////////////////////////////////////////////////////////
            BulkInsertStepUtils.setNextStepStreamedETLPreview(runBackendStepOutput);
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error in bulk insert receive mapping", e);
         throw new QException("Unhandled error in bulk insert receive mapping step", e);
      }
   }

}
