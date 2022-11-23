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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.delete.BulkDeleteTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit.BulkEditTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkInsertExtractStep;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkInsertTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaDeleteStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaUpdateStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** As part of helping a QInstance be created and/or validated, apply some default
 ** transformations to it, such as populating missing labels based on names.
 **
 *******************************************************************************/
public class QInstanceEnricher
{
   private static final Logger LOG = LogManager.getLogger(QInstanceEnricher.class);

   private final QInstance qInstance;

   //////////////////////////////////////////////////////////
   // todo - come up w/ a way for app devs to set configs! //
   //////////////////////////////////////////////////////////
   private boolean configRemoveIdFromNameWhenCreatingPossibleValueFieldLabels = true;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QInstanceEnricher(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void enrich()
   {
      if(qInstance.getTables() != null)
      {
         qInstance.getTables().values().forEach(this::enrichTable);
         defineTableBulkProcesses(qInstance);
      }

      if(qInstance.getProcesses() != null)
      {
         qInstance.getProcesses().values().forEach(this::enrichProcess);
      }

      if(qInstance.getBackends() != null)
      {
         qInstance.getBackends().values().forEach(this::enrichBackend);
      }

      if(qInstance.getApps() != null)
      {
         qInstance.getApps().values().forEach(this::enrichApp);
      }

      if(qInstance.getReports() != null)
      {
         qInstance.getReports().values().forEach(this::enrichReport);
      }

      if(qInstance.getPossibleValueSources() != null)
      {
         qInstance.getPossibleValueSources().values().forEach(this::enrichPossibleValueSource);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichBackend(QBackendMetaData qBackendMetaData)
   {
      qBackendMetaData.enrich();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichTable(QTableMetaData table)
   {
      if(!StringUtils.hasContent(table.getLabel()))
      {
         table.setLabel(nameToLabel(table.getName()));
      }

      if(table.getFields() != null)
      {
         table.getFields().values().forEach(this::enrichField);
      }

      if(CollectionUtils.nullSafeIsEmpty(table.getSections()))
      {
         generateTableFieldSections(table);
      }
      else
      {
         table.getSections().forEach(this::enrichFieldSection);
      }

      if(CollectionUtils.nullSafeHasContents(table.getRecordLabelFields()) && !StringUtils.hasContent(table.getRecordLabelFormat()))
      {
         table.setRecordLabelFormat(String.join(" ", Collections.nCopies(table.getRecordLabelFields().size(), "%s")));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichProcess(QProcessMetaData process)
   {
      if(!StringUtils.hasContent(process.getLabel()))
      {
         process.setLabel(nameToLabel(process.getName()));
      }

      if(process.getStepList() != null)
      {
         process.getStepList().forEach(this::enrichStep);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichStep(QStepMetaData step)
   {
      if(!StringUtils.hasContent(step.getLabel()))
      {
         step.setLabel(nameToLabel(step.getName()));
      }

      step.getInputFields().forEach(this::enrichField);
      step.getOutputFields().forEach(this::enrichField);

      if(step instanceof QFrontendStepMetaData frontendStepMetaData)
      {
         if(frontendStepMetaData.getFormFields() != null)
         {
            frontendStepMetaData.getFormFields().forEach(this::enrichField);
         }
         if(frontendStepMetaData.getViewFields() != null)
         {
            frontendStepMetaData.getViewFields().forEach(this::enrichField);
         }
         if(frontendStepMetaData.getRecordListFields() != null)
         {
            frontendStepMetaData.getRecordListFields().forEach(this::enrichField);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void enrichField(QFieldMetaData field)
   {
      if(!StringUtils.hasContent(field.getLabel()))
      {
         if(configRemoveIdFromNameWhenCreatingPossibleValueFieldLabels && StringUtils.hasContent(field.getPossibleValueSourceName()) && field.getName() != null && field.getName().endsWith("Id"))
         {
            field.setLabel(nameToLabel(field.getName().substring(0, field.getName().length() - 2)));
         }
         else
         {
            field.setLabel(nameToLabel(field.getName()));
         }
      }

      //////////////////////////////////////////////////////////////////////////
      // if this field has a possibleValueSource                              //
      // and that PVS exists in the instance                                  //
      // and it's a table-type PVS and the table name is set                  //
      // and it's a valid table in the instance, and the table is in some app //
      // and the field doesn't have a LINK adornment                          //
      // then add a link-to-record-from-table adornment to the field.         //
      //////////////////////////////////////////////////////////////////////////
      if(StringUtils.hasContent(field.getPossibleValueSourceName()))
      {
         QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(field.getPossibleValueSourceName());
         if(possibleValueSource != null)
         {
            String tableName = possibleValueSource.getTableName();
            if(QPossibleValueSourceType.TABLE.equals(possibleValueSource.getType()) && StringUtils.hasContent(tableName))
            {
               if(qInstance.getTable(tableName) != null && doesAnyAppHaveTable(tableName))
               {
                  if(field.getAdornments() == null || field.getAdornments().stream().noneMatch(a -> AdornmentType.LINK.equals(a.getType())))
                  {
                     field.withFieldAdornment(new FieldAdornment().withType(AdornmentType.LINK)
                        .withValue(AdornmentType.LinkValues.TO_RECORD_FROM_TABLE, tableName));
                  }
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean doesAnyAppHaveTable(String tableName)
   {
      if(qInstance.getApps() != null)
      {
         for(QAppMetaData app : qInstance.getApps().values())
         {
            if(app.getChildren() != null)
            {
               for(QAppChildMetaData child : app.getChildren())
               {
                  if(child instanceof QTableMetaData && tableName.equals(child.getName()))
                  {
                     return (true);
                  }
               }
            }
         }
      }

      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichApp(QAppMetaData app)
   {
      if(!StringUtils.hasContent(app.getLabel()))
      {
         app.setLabel(nameToLabel(app.getName()));
      }

      if(CollectionUtils.nullSafeIsEmpty(app.getSections()))
      {
         generateAppSections(app);
      }

      for(QAppSection section : CollectionUtils.nonNullList(app.getSections()))
      {
         enrichAppSection(section);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichAppSection(QAppSection section)
   {
      if(!StringUtils.hasContent(section.getLabel()))
      {
         section.setLabel(nameToLabel(section.getName()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichFieldSection(QFieldSection section)
   {
      if(!StringUtils.hasContent(section.getLabel()))
      {
         section.setLabel(nameToLabel(section.getName()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichReport(QReportMetaData report)
   {
      if(!StringUtils.hasContent(report.getLabel()))
      {
         report.setLabel(nameToLabel(report.getName()));
      }

      if(report.getInputFields() != null)
      {
         report.getInputFields().forEach(this::enrichField);
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // if there's only 1 data source in the report, and it doesn't have a name, give it a default name //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      String singleDataSourceName = null;
      if(report.getDataSources() != null)
      {
         if(report.getDataSources().size() == 1)
         {
            QReportDataSource dataSource = report.getDataSources().get(0);
            if(!StringUtils.hasContent(dataSource.getName()))
            {
               dataSource.setName("DEFAULT");
            }
            singleDataSourceName = dataSource.getName();
         }
      }

      if(report.getViews() != null)
      {
         //////////////////////////////////////////////////////////////////////////////////////////////
         // if there's only 1 view in the report, and it doesn't have a name, give it a default name //
         //////////////////////////////////////////////////////////////////////////////////////////////
         if(report.getViews().size() == 1)
         {
            QReportView view = report.getViews().get(0);
            if(!StringUtils.hasContent(view.getName()))
            {
               view.setName("DEFAULT");
            }
         }

         /////////////////////////////////////////////////////////////////////////////
         // for any views in the report, if they don't specify a data source name,  //
         // but there's only 1 data source, then use that single data source's name //
         /////////////////////////////////////////////////////////////////////////////
         for(QReportView view : report.getViews())
         {
            if(!StringUtils.hasContent(view.getDataSourceName()) && singleDataSourceName != null)
            {
               view.setDataSourceName(singleDataSourceName);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String nameToLabel(String name)
   {
      if(!StringUtils.hasContent(name))
      {
         return (name);
      }

      if(name.length() == 1)
      {
         return (name.substring(0, 1).toUpperCase(Locale.ROOT));
      }

      String suffix = name.substring(1)

         //////////////////////////////////////////////////////////////////////
         // Put a space before capital letters or numbers embedded in a name //
         // e.g., omethingElse -> omething Else; umber1 -> umber 1           //
         //////////////////////////////////////////////////////////////////////
         .replaceAll("([A-Z0-9]+)", " $1")

         ////////////////////////////////////////////////////////////////
         // put a space between numbers and words that come after them //
         // e.g., umber1dad -> number 1 dad                            //
         ////////////////////////////////////////////////////////////////
         .replaceAll("([0-9])([A-Za-z])", "$1 $2");

      return (name.substring(0, 1).toUpperCase(Locale.ROOT) + suffix);
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
      Map<String, Serializable> values = new HashMap<>();
      values.put(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE, table.getName());

      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
            BulkInsertExtractStep.class,
            BulkInsertTransformStep.class,
            LoadViaInsertStep.class,
            values
         )
         .withName(processName)
         .withLabel(table.getLabel() + " Bulk Insert")
         .withTableName(table.getName())
         .withIsHidden(true);

      List<QFieldMetaData> editableFields = new ArrayList<>();
      for(QFieldSection section : CollectionUtils.nonNullList(table.getSections()))
      {
         for(String fieldName : CollectionUtils.nonNullList(section.getFieldNames()))
         {
            try
            {
               QFieldMetaData field = table.getField(fieldName);
               if(field.getIsEditable())
               {
                  editableFields.add(field);
               }
            }
            catch(Exception e)
            {
               // shrug?
            }
         }
      }

      String fieldsForHelpText = editableFields.stream()
         .map(QFieldMetaData::getLabel)
         .collect(Collectors.joining(", "));

      QFrontendStepMetaData uploadScreen = new QFrontendStepMetaData()
         .withName("upload")
         .withLabel("Upload File")
         .withFormField(new QFieldMetaData("theFile", QFieldType.BLOB).withIsRequired(true))
         .withComponent(new QFrontendComponentMetaData()
            .withType(QComponentType.HELP_TEXT)
            .withValue("previewText", "file upload instructions")
            .withValue("text", "Upload a CSV file with the following columns:\n" + fieldsForHelpText))
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM));

      process.addStep(0, uploadScreen);
      process.getFrontendStep("review").setRecordListFields(editableFields);
      qInstance.addProcess(process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineTableBulkEdit(QInstance qInstance, QTableMetaData table, String processName)
   {
      Map<String, Serializable> values = new HashMap<>();
      values.put(StreamedETLWithFrontendProcess.FIELD_SOURCE_TABLE, table.getName());
      values.put(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE, table.getName());
      values.put(StreamedETLWithFrontendProcess.FIELD_PREVIEW_MESSAGE, StreamedETLWithFrontendProcess.DEFAULT_PREVIEW_MESSAGE_FOR_UPDATE);

      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
            ExtractViaQueryStep.class,
            BulkEditTransformStep.class,
            LoadViaUpdateStep.class,
            values
         )
         .withName(processName)
         .withLabel(table.getLabel() + " Bulk Edit")
         .withTableName(table.getName())
         .withIsHidden(true);

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
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.BULK_EDIT_FORM));

      process.addStep(0, editScreen);
      process.getFrontendStep("review").setRecordListFields(editableFields);
      qInstance.addProcess(process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineTableBulkDelete(QInstance qInstance, QTableMetaData table, String processName)
   {
      Map<String, Serializable> values = new HashMap<>();
      values.put(StreamedETLWithFrontendProcess.FIELD_SOURCE_TABLE, table.getName());
      values.put(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE, table.getName());
      values.put(StreamedETLWithFrontendProcess.FIELD_PREVIEW_MESSAGE, StreamedETLWithFrontendProcess.DEFAULT_PREVIEW_MESSAGE_FOR_DELETE);

      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
            ExtractViaQueryStep.class,
            BulkDeleteTransformStep.class,
            LoadViaDeleteStep.class,
            values
         )
         .withName(processName)
         .withLabel(table.getLabel() + " Bulk Delete")
         .withTableName(table.getName())
         .withIsHidden(true);

      List<QFieldMetaData> tableFields = table.getFields().values().stream().toList();
      process.getFrontendStep("review").setRecordListFields(tableFields);

      qInstance.addProcess(process);
   }



   /*******************************************************************************
    ** for all fields in a table, set their backendName, using the default "inference" logic
    ** see {@link #inferBackendName(String)}
    *******************************************************************************/
   public static void setInferredFieldBackendNames(QTableMetaData tableMetaData)
   {
      if(tableMetaData == null)
      {
         LOG.warn("Requested to infer field backend names with a null table as input.  Returning with noop.");
         return;
      }

      if(CollectionUtils.nullSafeIsEmpty(tableMetaData.getFields()))
      {
         LOG.warn("Requested to infer field backend names on a table [" + tableMetaData.getName() + "] with no fields.  Returning with noop.");
         return;
      }

      for(QFieldMetaData field : tableMetaData.getFields().values())
      {
         String fieldName        = field.getName();
         String fieldBackendName = field.getBackendName();
         if(!StringUtils.hasContent(fieldBackendName))
         {
            String backendName = inferBackendName(fieldName);
            field.setBackendName(backendName);
         }
      }
   }



   /*******************************************************************************
    ** Do a default mapping from a camelCase field name to an underscore_style
    ** name for a backend.
    **
    ** Examples:
    ** <ul>
    **   <li>wordAnotherWordMoreWords -> word_another_word_more_words</li>
    **   <li>lUlUlUl -> l_ul_ul_ul</li>
    **   <li>StartsUpper -> starts_upper</li>
    **   <li>TLAFirst -> tla_first</li>
    **   <li>wordThenTLAInMiddle -> word_then_tla_in_middle</li>
    **   <li>endWithTLA -> end_with_tla</li>
    **   <li>TLAAndAnotherTLA -> tla_and_another_tla</li>
    ** </ul>
    *******************************************************************************/
   public static String inferBackendName(String fieldName)
   {
      ////////////////////////////////////////////////////////////////////////////////////////
      // build a list of words in the name, then join them with _ and lower-case the result //
      ////////////////////////////////////////////////////////////////////////////////////////
      List<String>  words       = new ArrayList<>();
      StringBuilder currentWord = new StringBuilder();
      for(int i = 0; i < fieldName.length(); i++)
      {
         Character thisChar = fieldName.charAt(i);
         Character nextChar = i < (fieldName.length() - 1) ? fieldName.charAt(i + 1) : null;

         /////////////////////////////////////////////////////////////////////////////////////
         // if we're at the end of the whole string, then we're at the end of the last word //
         /////////////////////////////////////////////////////////////////////////////////////
         if(nextChar == null)
         {
            currentWord.append(thisChar);
            words.add(currentWord.toString());
         }

         ///////////////////////////////////////////////////////////
         // transitioning from a lower to an upper starts a word. //
         ///////////////////////////////////////////////////////////
         else if(Character.isLowerCase(thisChar) && Character.isUpperCase(nextChar))
         {
            currentWord.append(thisChar);
            words.add(currentWord.toString());
            currentWord = new StringBuilder();
         }

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // transitioning from an upper to a lower - it starts a word, as long as there were already letters in the current word                                    //
         // e.g., on wordThenTLAInMiddle, when thisChar=I and nextChar=n.  currentWord will be "TLA".  So finish that word, and start a new one with the 'I'        //
         // but the normal single-upper condition, e.g., firstName, when thisChar=N and nextChar=a, current word will be empty string, so just append the 'a' to it //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         else if(Character.isUpperCase(thisChar) && Character.isLowerCase(nextChar) && currentWord.length() > 0)
         {
            words.add(currentWord.toString());
            currentWord = new StringBuilder();
            currentWord.append(thisChar);
         }

         /////////////////////////////////////////////////////////////
         // by default, just add this character to the current word //
         /////////////////////////////////////////////////////////////
         else
         {
            currentWord.append(thisChar);
         }
      }

      return (String.join("_", words).toLowerCase(Locale.ROOT));
   }



   /*******************************************************************************
    ** If a app didn't have any sections, generate "sensible defaults"
    *******************************************************************************/
   private void generateAppSections(QAppMetaData app)
   {
      if(CollectionUtils.nullSafeIsEmpty(app.getChildren()))
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // assume this app is valid if it has no children, but surely it doesn't need any sections then. //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         return;
      }

      //////////////////////////////////////////////////////////////////////////////
      // create an identity section for the id and any fields in the record label //
      //////////////////////////////////////////////////////////////////////////////
      QAppSection defaultSection = new QAppSection(app.getName(), app.getLabel(), new QIcon("badge"), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

      boolean foundNonAppChild = false;
      if(CollectionUtils.nullSafeHasContents(app.getChildren()))
      {
         for(QAppChildMetaData child : app.getChildren())
         {
            //////////////////////////////////////////////////////////////////////////////////////////
            // only tables, processes, and reports are allowed to be in sections at this time, apps //
            // might be children but not in sections so keep track if we find any non-app           //
            //////////////////////////////////////////////////////////////////////////////////////////
            if(child.getClass().equals(QTableMetaData.class))
            {
               defaultSection.getTables().add(child.getName());
               foundNonAppChild = true;
            }
            else if(child.getClass().equals(QProcessMetaData.class))
            {
               defaultSection.getProcesses().add(child.getName());
               foundNonAppChild = true;
            }
            else if(child.getClass().equals(QReportMetaData.class))
            {
               defaultSection.getReports().add(child.getName());
               foundNonAppChild = true;
            }
         }
      }

      if(foundNonAppChild)
      {
         app.addSection(defaultSection);
      }
   }



   /*******************************************************************************
    ** If a table didn't have any sections, generate "sensible defaults"
    *******************************************************************************/
   private void generateTableFieldSections(QTableMetaData table)
   {
      if(CollectionUtils.nullSafeIsEmpty(table.getFields()))
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // assume this table is invalid if it has no fields, but surely it doesn't need any sections then. //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         return;
      }

      //////////////////////////////////////////////////////////////////////////////
      // create an identity section for the id and any fields in the record label //
      //////////////////////////////////////////////////////////////////////////////
      QFieldSection identitySection = new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1, new ArrayList<>());

      Set<String> usedFieldNames = new HashSet<>();

      if(StringUtils.hasContent(table.getPrimaryKeyField()))
      {
         identitySection.getFieldNames().add(table.getPrimaryKeyField());
         usedFieldNames.add(table.getPrimaryKeyField());
      }

      if(CollectionUtils.nullSafeHasContents(table.getRecordLabelFields()))
      {
         for(String fieldName : table.getRecordLabelFields())
         {
            if(!usedFieldNames.contains(fieldName) && table.getFields().containsKey(fieldName))
            {
               identitySection.getFieldNames().add(fieldName);
               usedFieldNames.add(fieldName);
            }
         }
      }

      if(!identitySection.getFieldNames().isEmpty())
      {
         table.addSection(identitySection);
      }

      ///////////////////////////////////////////////////////////////////////////////
      // if there are more fields, then add them in a default/Other Fields section //
      ///////////////////////////////////////////////////////////////////////////////
      QFieldSection otherSection = new QFieldSection("otherFields", "Other Fields", new QIcon("dataset"), Tier.T2, new ArrayList<>());
      if(CollectionUtils.nullSafeHasContents(table.getFields()))
      {
         for(String fieldName : table.getFields().keySet())
         {
            if(!usedFieldNames.contains(fieldName))
            {
               otherSection.getFieldNames().add(fieldName);
               usedFieldNames.add(fieldName);
            }
         }
      }

      if(!otherSection.getFieldNames().isEmpty())
      {
         table.addSection(otherSection);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void enrichPossibleValueSource(QPossibleValueSource possibleValueSource)
   {
      if(QPossibleValueSourceType.TABLE.equals(possibleValueSource.getType()))
      {
         if(CollectionUtils.nullSafeIsEmpty(possibleValueSource.getSearchFields()))
         {
            QTableMetaData table = qInstance.getTable(possibleValueSource.getTableName());
            if(table != null)
            {
               if(table.getPrimaryKeyField() != null)
               {
                  possibleValueSource.withSearchField(table.getPrimaryKeyField());
               }

               for(String recordLabelField : CollectionUtils.nonNullList(table.getRecordLabelFields()))
               {
                  possibleValueSource.withSearchField(recordLabelField);
               }
            }
         }

         if(CollectionUtils.nullSafeIsEmpty(possibleValueSource.getOrderByFields()))
         {
            QTableMetaData table = qInstance.getTable(possibleValueSource.getTableName());
            if(table != null)
            {
               for(String recordLabelField : CollectionUtils.nonNullList(table.getRecordLabelFields()))
               {
                  possibleValueSource.withOrderByField(recordLabelField);
               }

               if(table.getPrimaryKeyField() != null)
               {
                  possibleValueSource.withOrderByField(table.getPrimaryKeyField());
               }
            }
         }

      }
   }

}
