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

package com.kingsrook.qqq.backend.core.instances;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.delete.BulkDeleteStoreStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit.BulkEditReceiveValuesStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit.BulkEditStoreRecordsStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkInsertReceiveFileStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkInsertStoreRecordsStep;
import com.kingsrook.qqq.backend.core.processes.implementations.general.LoadInitialRecordsStep;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** As part of helping a QInstance be created and/or validated, apply some default
 ** transformations to it, such as populating missing labels based on names.
 **
 *******************************************************************************/
public class QInstanceEnricher
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public void enrich(QInstance qInstance)
   {
      if(qInstance.getTables() != null)
      {
         qInstance.getTables().values().forEach(this::enrich);
         defineTableBulkProcesses(qInstance);
      }

      if(qInstance.getProcesses() != null)
      {
         qInstance.getProcesses().values().forEach(this::enrich);
      }

      if(qInstance.getBackends() != null)
      {
         qInstance.getBackends().values().forEach(this::enrich);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QBackendMetaData qBackendMetaData)
   {
      qBackendMetaData.enrich();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QTableMetaData table)
   {
      if(!StringUtils.hasContent(table.getLabel()))
      {
         table.setLabel(nameToLabel(table.getName()));
      }

      if(table.getFields() != null)
      {
         table.getFields().values().forEach(this::enrich);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QProcessMetaData process)
   {
      if(!StringUtils.hasContent(process.getLabel()))
      {
         process.setLabel(nameToLabel(process.getName()));
      }

      if(process.getStepList() != null)
      {
         process.getStepList().forEach(this::enrich);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QStepMetaData step)
   {
      if(!StringUtils.hasContent(step.getLabel()))
      {
         step.setLabel(nameToLabel(step.getName()));
      }

      step.getInputFields().forEach(this::enrich);
      step.getOutputFields().forEach(this::enrich);

      if(step instanceof QFrontendStepMetaData frontendStepMetaData)
      {
         if(frontendStepMetaData.getFormFields() != null)
         {
            frontendStepMetaData.getFormFields().forEach(this::enrich);
         }
         if(frontendStepMetaData.getViewFields() != null)
         {
            frontendStepMetaData.getViewFields().forEach(this::enrich);
         }
         if(frontendStepMetaData.getRecordListFields() != null)
         {
            frontendStepMetaData.getRecordListFields().forEach(this::enrich);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrich(QFieldMetaData field)
   {
      if(!StringUtils.hasContent(field.getLabel()))
      {
         field.setLabel(nameToLabel(field.getName()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String nameToLabel(String name)
   {
      if(name == null)
      {
         return (null);
      }

      return (name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1).replaceAll("([A-Z])", " $1"));
   }



   /*******************************************************************************
    ** Add bulk insert/edit/delete processes to all tables (unless the meta data
    ** already had these processes defined (e.g., the user defined custom ones)
    *******************************************************************************/
   private void defineTableBulkProcesses(QInstance qInstance)
   {
      for(QTableMetaData table : qInstance.getTables().values())
      {
         if(table.getFields() == null)
         {
            /////////////////////////////////////////////////////////////////
            // these processes can't be defined if there aren't any fields //
            /////////////////////////////////////////////////////////////////
            continue;
         }

         // todo - add idea of 'supportsBulkX'
         String bulkInsertProcessName = table.getName() + ".bulkInsert";
         if(qInstance.getProcess(bulkInsertProcessName) == null)
         {
            defineTableBulkInsert(qInstance, table, bulkInsertProcessName);
         }

         String bulkEditProcessName = table.getName() + ".bulkEdit";
         if(qInstance.getProcess(bulkEditProcessName) == null)
         {
            defineTableBulkEdit(qInstance, table, bulkEditProcessName);
         }

         String bulkDeleteProcessName = table.getName() + ".bulkDelete";
         if(qInstance.getProcess(bulkDeleteProcessName) == null)
         {
            defineTableBulkDelete(qInstance, table, bulkDeleteProcessName);
         }

      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineTableBulkInsert(QInstance qInstance, QTableMetaData table, String processName)
   {
      List<QFieldMetaData> editableFields = table.getFields().values().stream()
         .filter(QFieldMetaData::getIsEditable)
         .toList();

      String fieldsForHelpText = editableFields.stream()
         .map(QFieldMetaData::getLabel)
         .collect(Collectors.joining(", "));

      QFrontendStepMetaData uploadScreen = new QFrontendStepMetaData()
         .withName("upload")
         .withLabel("Upload File")
         .withFormField(new QFieldMetaData("theFile", QFieldType.BLOB).withIsRequired(true))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            // .withValue("text", "Upload a CSV or XLSX file with the following columns: " + fieldsForHelpText));
            .withValue("text", "Upload a CSV file with the following columns: " + fieldsForHelpText));

      QBackendStepMetaData receiveFileStep = new QBackendStepMetaData()
         .withName("receiveFile")
         .withCode(new QCodeReference(BulkInsertReceiveFileStep.class))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .withFieldList(List.of(new QFieldMetaData("noOfFileRows", QFieldType.INTEGER))));

      QFrontendStepMetaData reviewScreen = new QFrontendStepMetaData()
         .withName("review")
         .withRecordListFields(editableFields)
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", "The records below were parsed from your file, and will be inserted if you click Submit."))
         .withViewField(new QFieldMetaData("noOfFileRows", QFieldType.INTEGER).withLabel("# of file rows"));

      QBackendStepMetaData storeStep = new QBackendStepMetaData()
         .withName("storeRecords")
         .withCode(new QCodeReference(BulkInsertStoreRecordsStep.class))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .withFieldList(List.of(new QFieldMetaData("noOfFileRows", QFieldType.INTEGER))));

      QFrontendStepMetaData resultsScreen = new QFrontendStepMetaData()
         .withName("results")
         .withRecordListFields(new ArrayList<>(table.getFields().values()))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", "The records below have been inserted."))
         .withViewField(new QFieldMetaData("noOfFileRows", QFieldType.INTEGER).withLabel("# of file rows"));

      qInstance.addProcess(
         new QProcessMetaData()
            .withName(processName)
            .withLabel(table.getLabel() + " Bulk Insert")
            .withTableName(table.getName())
            .withIsHidden(true)
            .withStepList(List.of(
               uploadScreen,
               receiveFileStep,
               reviewScreen,
               storeStep,
               resultsScreen
            )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineTableBulkEdit(QInstance qInstance, QTableMetaData table, String processName)
   {
      List<QFieldMetaData> editableFields = table.getFields().values().stream()
         .filter(QFieldMetaData::getIsEditable)
         .toList();

      QFrontendStepMetaData editScreen = new QFrontendStepMetaData()
         .withName("edit")
         .withLabel("Edit Values")
         .withFormFields(editableFields)
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", """
               Flip the switches next to the fields that you want to edit.
               The values you supply here will be updated in all of the records you are bulk editing.
               You can clear out the value in a field by flipping the switch on for that field and leaving the input field blank.
               Fields whose switches are off will not be updated."""))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.BULK_EDIT_FORM)
         );

      QBackendStepMetaData receiveValuesStep = new QBackendStepMetaData()
         .withName("receiveValues")
         .withCode(new QCodeReference(BulkEditReceiveValuesStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withRecordListMetaData(new QRecordListMetaData().withTableName(table.getName()))
            .withField(new QFieldMetaData(BulkEditReceiveValuesStep.FIELD_ENABLED_FIELDS, QFieldType.STRING))
            .withFields(editableFields));

      QFrontendStepMetaData reviewScreen = new QFrontendStepMetaData()
         .withName("review")
         .withRecordListFields(editableFields)
         .withViewField(new QFieldMetaData(BulkEditReceiveValuesStep.FIELD_VALUES_BEING_UPDATED, QFieldType.STRING))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", "The records below will be updated if you click Submit."));

      QBackendStepMetaData storeStep = new QBackendStepMetaData()
         .withName("storeRecords")
         .withCode(new QCodeReference(BulkEditStoreRecordsStep.class))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .withFieldList(List.of(new QFieldMetaData("noOfFileRows", QFieldType.INTEGER))));

      QFrontendStepMetaData resultsScreen = new QFrontendStepMetaData()
         .withName("results")
         .withRecordListFields(new ArrayList<>(table.getFields().values()))
         .withViewField(new QFieldMetaData(BulkEditReceiveValuesStep.FIELD_VALUES_BEING_UPDATED, QFieldType.STRING))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", "The records below have been updated."));

      qInstance.addProcess(
         new QProcessMetaData()
            .withName(processName)
            .withLabel(table.getLabel() + " Bulk Edit")
            .withTableName(table.getName())
            .withIsHidden(true)
            .withStepList(List.of(
               LoadInitialRecordsStep.defineMetaData(table.getName()),
               editScreen,
               receiveValuesStep,
               reviewScreen,
               storeStep,
               resultsScreen
            )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineTableBulkDelete(QInstance qInstance, QTableMetaData table, String processName)
   {
      QFrontendStepMetaData reviewScreen = new QFrontendStepMetaData()
         .withName("review")
         .withRecordListFields(new ArrayList<>(table.getFields().values()))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", "The records below will be deleted if you click Submit."));

      QBackendStepMetaData storeStep = new QBackendStepMetaData()
         .withName("delete")
         .withCode(new QCodeReference(BulkDeleteStoreStep.class));

      QFrontendStepMetaData resultsScreen = new QFrontendStepMetaData()
         .withName("results")
         .withRecordListFields(new ArrayList<>(table.getFields().values()))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("text", "The records below have been deleted."));

      qInstance.addProcess(
         new QProcessMetaData()
            .withName(processName)
            .withLabel(table.getLabel() + " Bulk Delete")
            .withTableName(table.getName())
            .withIsHidden(true)
            .withStepList(List.of(
               LoadInitialRecordsStep.defineMetaData(table.getName()),
               reviewScreen,
               storeStep,
               resultsScreen
            )));
   }

}
