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


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadTableStructure;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertPrepareFileMappingStep implements BackendStep
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      buildFileDetailsForMappingStep(runBackendStepInput, runBackendStepOutput);
      buildFieldsForMappingStep(runBackendStepInput, runBackendStepOutput);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void buildFileDetailsForMappingStep(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      StorageInput storageInput = BulkInsertStepUtils.getStorageInputForTheFile(runBackendStepInput);
      File         file         = new File(storageInput.getReference());
      runBackendStepOutput.addValue("fileBaseName", file.getName());

      try
         (
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            // open a stream to read from our file, and a FileToRows object, that knows how to read from that stream //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            InputStream inputStream = new StorageAction().getInputStream(storageInput);
            FileToRowsInterface fileToRowsInterface = FileToRowsInterface.forFile(storageInput.getReference(), inputStream);
         )
      {
         /////////////////////////////////////////////////
         // read the 1st row, and assume it is a header //
         /////////////////////////////////////////////////
         BulkLoadFileRow   headerRow     = fileToRowsInterface.next();
         ArrayList<String> headerValues  = new ArrayList<>();
         ArrayList<String> headerLetters = new ArrayList<>();
         for(int i = 0; i < headerRow.size(); i++)
         {
            headerValues.add(ValueUtils.getValueAsString(headerRow.getValue(i)));
            headerLetters.add(toHeaderLetter(i));
         }
         runBackendStepOutput.addValue("headerValues", headerValues);
         runBackendStepOutput.addValue("headerLetters", headerLetters);

         ///////////////////////////////////////////////////////////////////////////////////////////
         // while there are more rows in the file - and we're under preview-rows limit, read rows //
         ///////////////////////////////////////////////////////////////////////////////////////////
         int                          previewRows      = 0;
         int                          previewRowsLimit = 5;
         ArrayList<ArrayList<String>> bodyValues       = new ArrayList<>();
         for(int i = 0; i < headerRow.size(); i++)
         {
            bodyValues.add(new ArrayList<>());
         }

         while(fileToRowsInterface.hasNext() && previewRows < previewRowsLimit)
         {
            BulkLoadFileRow bodyRow = fileToRowsInterface.next();
            previewRows++;

            for(int i = 0; i < headerRow.size(); i++)
            {
               bodyValues.get(i).add(ValueUtils.getValueAsString(bodyRow.getValueElseNull(i)));
            }
         }
         runBackendStepOutput.addValue("bodyValuesPreview", bodyValues);
      }
      catch(Exception e)
      {
         throw (new QException("Error reading bulk load file", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static String toHeaderLetter(int i)
   {
      StringBuilder rs = new StringBuilder();

      do
      {
         rs.insert(0, (char) ('A' + (i % 26)));
         i = (i / 26) - 1;
      }
      while(i >= 0);

      return (rs.toString());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void buildFieldsForMappingStep(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput)
   {
      String                 tableName      = runBackendStepInput.getValueString("tableName");
      BulkLoadTableStructure tableStructure = buildTableStructure(tableName, null, null);
      runBackendStepOutput.addValue("tableStructure", tableStructure);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static BulkLoadTableStructure buildTableStructure(String tableName, Association association, String parentAssociationPath)
   {
      QTableMetaData table = QContext.getQInstance().getTable(tableName);

      BulkLoadTableStructure tableStructure = new BulkLoadTableStructure();
      tableStructure.setTableName(tableName);
      tableStructure.setLabel(table.getLabel());

      Set<String> associationJoinFieldNamesToExclude = new HashSet<>();

      if(association == null)
      {
         tableStructure.setIsMain(true);
         tableStructure.setIsMany(false);
         tableStructure.setAssociationPath(null);
      }
      else
      {
         tableStructure.setIsMain(false);

         QJoinMetaData join = QContext.getQInstance().getJoin(association.getJoinName());
         if(join.getType().equals(JoinType.ONE_TO_MANY) || join.getType().equals(JoinType.MANY_TO_ONE))
         {
            tableStructure.setIsMany(true);
         }

         for(JoinOn joinOn : join.getJoinOns())
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////
            // don't allow the user to map the "join field" from a child up to its parent                 //
            // (e.g., you can't map lineItem.orderId -- that'll happen automatically via the association) //
            ////////////////////////////////////////////////////////////////////////////////////////////////
            if(join.getLeftTable().equals(tableName))
            {
               associationJoinFieldNamesToExclude.add(joinOn.getLeftField());
            }
            else if(join.getRightTable().equals(tableName))
            {
               associationJoinFieldNamesToExclude.add(joinOn.getRightField());
            }
         }

         if(!StringUtils.hasContent(parentAssociationPath))
         {
            tableStructure.setAssociationPath(association.getName());
         }
         else
         {
            tableStructure.setAssociationPath(parentAssociationPath + "." + association.getName());
         }
      }

      ArrayList<QFieldMetaData> fields = new ArrayList<>();
      tableStructure.setFields(fields);
      for(QFieldMetaData field : table.getFields().values())
      {
         if(field.getIsEditable() && !associationJoinFieldNamesToExclude.contains(field.getName()))
         {
            fields.add(field);
         }
      }

      fields.sort(Comparator.comparing(f -> f.getLabel()));

      for(Association childAssociation : CollectionUtils.nonNullList(table.getAssociations()))
      {
         BulkLoadTableStructure associatedStructure = buildTableStructure(childAssociation.getAssociatedTableName(), childAssociation, parentAssociationPath);
         tableStructure.addAssociation(associatedStructure);
      }

      return (tableStructure);
   }

}
