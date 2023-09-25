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

package com.kingsrook.qqq.backend.core.model.processlogs;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.ChildRecordListRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import static com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType.ChipValues.iconAndColorValues;


/*******************************************************************************
 ** Meta Data provider for ProcessLog tables
 *******************************************************************************/
public class ProcessLogsMetaDataProvider
{
   public static final String PROCESS_LOG_JOIN_PROCESS_LOG_VALUE      = "processLogJoinProcessLogValue";
   public static final String PROCESS_LOG_JOIN_PROCESS_LOG_RECORD_INT = "processLogJoinProcessLogRecordInt";
   public static final String PROCESS_LOG_JOIN_PROCESS_LOG_SUMMARY    = "processLogJoinProcessLogSummary";



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      defineStandardTables(instance, backendName, backendDetailEnricher);
      defineStandardPossibleValueSources(instance);
      defineStandardJoins(instance);
      defineStandardWidgets(instance);
      // instance.addProcess(defineRunRecordScriptProcess());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineStandardWidgets(QInstance instance)
   {
      instance.addWidget(ChildRecordListRenderer.widgetMetaDataBuilder(instance.getJoin(PROCESS_LOG_JOIN_PROCESS_LOG_VALUE))
         .withLabel("Process Values")
         .getWidgetMetaData());

      instance.addWidget(ChildRecordListRenderer.widgetMetaDataBuilder(instance.getJoin(PROCESS_LOG_JOIN_PROCESS_LOG_RECORD_INT))
         .withLabel("Process Records")
         .getWidgetMetaData());

      instance.addWidget(ChildRecordListRenderer.widgetMetaDataBuilder(instance.getJoin(PROCESS_LOG_JOIN_PROCESS_LOG_SUMMARY))
         .withLabel("Process Summary")
         .getWidgetMetaData());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineStandardJoins(QInstance instance)
   {
      instance.addJoin(new QJoinMetaData()
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(ProcessLog.TABLE_NAME)
         .withRightTable(ProcessLogValue.TABLE_NAME)
         .withJoinOn(new JoinOn("id", "processLogId"))
         .withOrderBy(new QFilterOrderBy("name"))
         .withName(PROCESS_LOG_JOIN_PROCESS_LOG_VALUE));

      instance.addJoin(new QJoinMetaData()
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(ProcessLog.TABLE_NAME)
         .withRightTable(ProcessLogRecordInt.TABLE_NAME)
         .withJoinOn(new JoinOn("id", "processLogId"))
         .withOrderBy(new QFilterOrderBy("recordId"))
         .withName(PROCESS_LOG_JOIN_PROCESS_LOG_RECORD_INT));

      instance.addJoin(new QJoinMetaData()
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(ProcessLog.TABLE_NAME)
         .withRightTable(ProcessLogSummary.TABLE_NAME)
         .withJoinOn(new JoinOn("id", "processLogId"))
         .withOrderBy(new QFilterOrderBy("id"))
         .withName(PROCESS_LOG_JOIN_PROCESS_LOG_SUMMARY));

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineStandardPossibleValueSources(QInstance instance)
   {
      instance.addPossibleValueSource(QPossibleValueSource.newForTable(ProcessLog.TABLE_NAME, "id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardTables(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      for(QTableMetaData tableMetaData : defineStandardTables(backendName, backendDetailEnricher))
      {
         instance.addTable(tableMetaData);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QTableMetaData> defineStandardTables(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      List<QTableMetaData> rs = new ArrayList<>();
      rs.add(enrich(backendDetailEnricher, defineProcessLogTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineProcessLogValueTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineProcessLogRecordIntTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineProcessLogSummaryTable(backendName)));
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
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(fieldsFromEntity);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineProcessLogTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, ProcessLog.TABLE_NAME, ProcessLog.class)
         .withRecordLabelFormat("%s - %s")
         .withRecordLabelFields("qqqProcessId", "id")
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "qqqProcessId", "qqqUserId")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("startTime", "endTime")))
         .withSection(new QFieldSection("summary", new QIcon().withName("functions"), Tier.T2).withWidgetName(PROCESS_LOG_JOIN_PROCESS_LOG_SUMMARY))
         .withSection(new QFieldSection("values", new QIcon().withName("border_color"), Tier.T2).withWidgetName(PROCESS_LOG_JOIN_PROCESS_LOG_VALUE))
         .withSection(new QFieldSection("records", new QIcon().withName("list"), Tier.T2).withWidgetName(PROCESS_LOG_JOIN_PROCESS_LOG_RECORD_INT))

         .withAssociation(new Association().withName("processLogValues").withJoinName(PROCESS_LOG_JOIN_PROCESS_LOG_VALUE).withAssociatedTableName(ProcessLogValue.TABLE_NAME))
         .withAssociation(new Association().withName("processLogRecordInts").withJoinName(PROCESS_LOG_JOIN_PROCESS_LOG_VALUE).withAssociatedTableName(ProcessLogRecordInt.TABLE_NAME))
         .withAssociation(new Association().withName("processLogSummaries").withJoinName(PROCESS_LOG_JOIN_PROCESS_LOG_SUMMARY).withAssociatedTableName(ProcessLogSummary.TABLE_NAME))

         .withExposedJoin(new ExposedJoin().withJoinTable(ProcessLogSummary.TABLE_NAME).withLabel("Process Log Summary").withJoinPath(List.of(PROCESS_LOG_JOIN_PROCESS_LOG_SUMMARY)))
         .withExposedJoin(new ExposedJoin().withJoinTable(ProcessLogValue.TABLE_NAME).withLabel("Process Log Value").withJoinPath(List.of(PROCESS_LOG_JOIN_PROCESS_LOG_VALUE)))
         .withExposedJoin(new ExposedJoin().withJoinTable(ProcessLogRecordInt.TABLE_NAME).withLabel("Process Log Record").withJoinPath(List.of(PROCESS_LOG_JOIN_PROCESS_LOG_RECORD_INT)))

         .withDisabledCapabilities(Set.of(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE));

      // todo
      /*
      tableMetaData.getField("filterId").withPossibleValueSourceFilter(new QQueryFilter(
         new QFilterCriteria("tableName", QCriteriaOperator.EQUALS, "${input.tableName}")
      ));
      */

      return tableMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineProcessLogValueTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, ProcessLogValue.TABLE_NAME, ProcessLogValue.class)
         .withRecordLabelFormat("%s=%s")
         .withRecordLabelFields("name", "value");

      return tableMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineProcessLogRecordIntTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, ProcessLogRecordInt.TABLE_NAME, ProcessLogRecordInt.class)
         .withRecordLabelFormat("Record %s in process log %s")
         .withRecordLabelFields("recordId", "processLogId");

      tableMetaData.getField("status").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValues(iconAndColorValues(Status.OK.name(), "check", AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValues(iconAndColorValues(Status.INFO.name(), "inbox", AdornmentType.ChipValues.COLOR_INFO))
         .withValues(iconAndColorValues(Status.WARNING.name(), "warning", AdornmentType.ChipValues.COLOR_WARNING))
         .withValues(iconAndColorValues(Status.ERROR.name(), "error", AdornmentType.ChipValues.COLOR_ERROR)));

      return tableMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineProcessLogSummaryTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, ProcessLogSummary.TABLE_NAME, ProcessLogSummary.class)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("id");

      tableMetaData.getField("status").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValues(iconAndColorValues(Status.OK.name(), "check", AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValues(iconAndColorValues(Status.INFO.name(), "inbox", AdornmentType.ChipValues.COLOR_INFO))
         .withValues(iconAndColorValues(Status.WARNING.name(), "warning", AdornmentType.ChipValues.COLOR_WARNING))
         .withValues(iconAndColorValues(Status.ERROR.name(), "error", AdornmentType.ChipValues.COLOR_ERROR)));

      return tableMetaData;
   }

}
