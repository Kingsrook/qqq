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

package com.kingsrook.qqq.backend.core.model.savedreports;


import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.RenderSavedReportMetaDataProducer;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedReportsMetaDataProvider
{
   public static final String REPORT_STORAGE_TABLE_NAME = "reportStorage";



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String recordTablesBackendName, String reportStorageBackendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      instance.addTable(defineSavedReportTable(recordTablesBackendName, backendDetailEnricher));
      instance.addPossibleValueSource(QPossibleValueSource.newForTable(SavedReport.TABLE_NAME));
      instance.addPossibleValueSource(QPossibleValueSource.newForEnum(ReportFormatPossibleValueEnum.NAME, ReportFormatPossibleValueEnum.values()));

      instance.addTable(defineReportStorageTable(reportStorageBackendName, backendDetailEnricher));

      QProcessMetaData renderSavedReportProcess = new RenderSavedReportMetaDataProducer().produce(instance);
      instance.addProcess(renderSavedReportProcess);
      renderSavedReportProcess.getInputFields().stream()
         .filter(f -> RenderSavedReportMetaDataProducer.FIELD_NAME_STORAGE_TABLE_NAME.equals(f.getName()))
         .findFirst()
         .ifPresent(f -> f.setDefaultValue(REPORT_STORAGE_TABLE_NAME));

      // todo - when we build the UI instance.addWidget(defineReportSetupWidget());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineReportStorageTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher)
   {
      QTableMetaData table = new QTableMetaData()
         .withName(REPORT_STORAGE_TABLE_NAME)
         .withBackendName(backendName)
         .withPrimaryKeyField("reference")
         .withField(new QFieldMetaData("reference", QFieldType.STRING))
         .withField(new QFieldMetaData("contents", QFieldType.BLOB));

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(table);
      }

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   /* todo - when we build the UI
   private QWidgetMetaDataInterface defineReportSetupWidget()
   {
      return new QWidgetMetaData()
         .withName("reportSetupWidget")
         .withLabel("Report Setup")
         .withIsCard(true)
         .withType(WidgetType.REPORT_SETUP.getType())
         .withCodeReference(new QCodeReference(DefaultWidgetRenderer.class));
   }
   */



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData defineSavedReportTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData table = new QTableMetaData()
         .withName(SavedReport.TABLE_NAME)
         .withLabel("Saved Report")
         .withIcon(new QIcon().withName("article"))
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("label")
         .withBackendName(backendName)
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(SavedReport.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "label")))
         .withSection(new QFieldSection("settings", new QIcon().withName("settings"), Tier.T2, List.of("tableName")))
         .withSection(new QFieldSection("reportSetup", new QIcon().withName("table_chart"), Tier.T2).withWidgetName("reportSetupWidget"))
         .withSection(new QFieldSection("data", new QIcon().withName("text_snippet"), Tier.T2, List.of("queryFilterJson", "columnsJson", "pivotTableJson")))
         .withSection(new QFieldSection("hidden", new QIcon().withName("text_snippet"), Tier.T2, List.of("inputFieldsJson", "userId")).withIsHidden(true))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));

      for(String jsonFieldName : List.of("queryFilterJson", "columnsJson", "inputFieldsJson", "pivotTableJson"))
      {
         table.getField(jsonFieldName).withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));
      }

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(table);
      }

      return (table);
   }

}
