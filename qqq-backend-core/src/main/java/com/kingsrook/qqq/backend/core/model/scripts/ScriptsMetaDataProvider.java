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

package com.kingsrook.qqq.backend.core.model.scripts;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.ChildRecordListRenderer;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.DefaultWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.processes.implementations.scripts.StoreScriptRevisionProcessStep;


/*******************************************************************************
 **
 *******************************************************************************/
public class ScriptsMetaDataProvider
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      defineStandardScriptsTables(instance, backendName, backendDetailEnricher);
      defineStandardScriptsPossibleValueSources(instance);
      defineStandardScriptsJoins(instance);
      defineStandardScriptsWidgets(instance);
      instance.addPossibleValueSource(TablesPossibleValueSourceMetaDataProvider.defineTablesPossibleValueSource(instance));
      instance.addProcess(defineStoreScriptRevisionProcess());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QProcessMetaData defineStoreScriptRevisionProcess()
   {
      return (new QProcessMetaData()
         .withName("storeScriptRevision")
         .withStepList(List.of(
            new QBackendStepMetaData()
               .withCode(new QCodeReference(StoreScriptRevisionProcessStep.class))
         )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardScriptsWidgets(QInstance instance)
   {
      instance.addWidget(ChildRecordListRenderer.widgetMetaDataBuilder(instance.getJoin(QJoinMetaData.makeInferredJoinName(ScriptLog.TABLE_NAME, ScriptLogLine.TABLE_NAME)))
         .withLabel("Log Lines")
         .getWidgetMetaData());

      instance.addWidget(new QWidgetMetaData()
         .withName("scriptViewer")
         .withLabel("Contents")
         .withIsCard(true)
         .withType(WidgetType.SCRIPT_VIEWER.getType())
         .withCodeReference(new QCodeReference(DefaultWidgetRenderer.class, null)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardScriptsJoins(QInstance instance)
   {
      instance.addJoin(new QJoinMetaData()
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(ScriptLog.TABLE_NAME)
         .withRightTable(ScriptLogLine.TABLE_NAME)
         .withJoinOn(new JoinOn("id", "scriptLogId"))
         .withOrderBy(new QFilterOrderBy("id"))
         .withInferredName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardScriptsTables(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      for(QTableMetaData tableMetaData : defineStandardScriptsTables(backendName, backendDetailEnricher))
      {
         instance.addTable(tableMetaData);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardScriptsPossibleValueSources(QInstance instance) throws QException
   {
      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(Script.TABLE_NAME)
         .withTableName(Script.TABLE_NAME)
      );

      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(ScriptRevision.TABLE_NAME)
         .withTableName(ScriptRevision.TABLE_NAME)
      );

      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(ScriptType.TABLE_NAME)
         .withTableName(ScriptType.TABLE_NAME)
      );

      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(ScriptLog.TABLE_NAME)
         .withTableName(ScriptLog.TABLE_NAME)
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QTableMetaData> defineStandardScriptsTables(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      List<QTableMetaData> rs = new ArrayList<>();
      rs.add(enrich(backendDetailEnricher, defineScriptTypeTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineScriptTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineScriptRevisionTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineScriptLogTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineScriptLogLineTable(backendName)));
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData enrich(Consumer<QTableMetaData> backendDetailEnricher, QTableMetaData table)
   {
      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(table);
      }
      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineStandardTable(String backendName, String name, Class<? extends QRecordEntity> fieldsFromEntity) throws QException
   {
      return new QTableMetaData()
         .withName(name)
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("name")
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(fieldsFromEntity);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptTable(String backendName) throws QException
   {
      return (defineStandardTable(backendName, Script.TABLE_NAME, Script.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "name", "scriptTypeId", "tableName", "currentScriptRevisionId")))
         .withSection(new QFieldSection("contents", new QIcon().withName("data_object"), Tier.T2).withWidgetName("scriptViewer"))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate"))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptTypeTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, ScriptType.TABLE_NAME, ScriptType.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "name")))
         .withSection(new QFieldSection("details", new QIcon().withName("dataset"), Tier.T2, List.of("helpText", "sampleCode")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));
      tableMetaData.getField("sampleCode").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("javascript")));
      tableMetaData.getField("helpText").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("text")));
      return (tableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptRevisionTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, ScriptRevision.TABLE_NAME, ScriptRevision.class)
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE)
         .withRecordLabelFormat("%s v%s")
         .withRecordLabelFields(List.of("scriptId", "sequenceNo"))
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "scriptId", "sequenceNo")))
         .withSection(new QFieldSection("code", new QIcon().withName("data_object"), Tier.T2, List.of("contents")))
         .withSection(new QFieldSection("changeManagement", new QIcon().withName("history"), Tier.T2, List.of("commitMessage", "author")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));

      tableMetaData.getField("contents").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR));
      return (tableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptLogTable(String backendName) throws QException
   {
      return (defineStandardTable(backendName, ScriptLog.TABLE_NAME, ScriptLog.class)
         .withRecordLabelFields(List.of("id"))
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("script", new QIcon().withName("data_object"), Tier.T2, List.of("scriptId", "scriptRevisionId")))
         .withSection(new QFieldSection("timing", new QIcon().withName("schedule"), Tier.T2, List.of("startTimestamp", "endTimestamp", "runTimeMillis", "createDate", "modifyDate")))
         .withSection(new QFieldSection("error", "Error", new QIcon().withName("error_outline"), Tier.T2, List.of("hadError", "error")))
         .withSection(new QFieldSection("inputOutput", "Input/Output", new QIcon().withName("chat"), Tier.T2, List.of("input", "output")))
         .withSection(new QFieldSection("lines", new QIcon().withName("horizontal_rule"), Tier.T2).withWidgetName(QJoinMetaData.makeInferredJoinName(ScriptLog.TABLE_NAME, ScriptLogLine.TABLE_NAME))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptLogLineTable(String backendName) throws QException
   {
      return (defineStandardTable(backendName, ScriptLogLine.TABLE_NAME, ScriptLogLine.class)
         .withRecordLabelFields(List.of("id")));
   }

}
