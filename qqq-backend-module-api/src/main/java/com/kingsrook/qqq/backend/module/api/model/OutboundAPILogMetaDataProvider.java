/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.api.model;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.module.api.processes.MigrateOutboundAPILogExtractStep;
import com.kingsrook.qqq.backend.module.api.processes.MigrateOutboundAPILogLoadStep;
import com.kingsrook.qqq.backend.module.api.processes.MigrateOutboundAPILogTransformStep;


/*******************************************************************************
 **
 *******************************************************************************/
public class OutboundAPILogMetaDataProvider
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static void defineAll(QInstance qInstance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      definePossibleValueSources().forEach(pvs ->
      {
         if(qInstance.getPossibleValueSource(pvs.getName()) == null)
         {
            qInstance.addPossibleValueSource(pvs);
         }
      });

      qInstance.addTable(defineOutboundAPILogTable(backendName, backendDetailEnricher));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void defineNewVersion(QInstance qInstance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      definePossibleValueSources().forEach(pvs ->
      {
         if(qInstance.getPossibleValueSource(pvs.getName()) == null)
         {
            qInstance.addPossibleValueSource(pvs);
         }
      });

      qInstance.addTable(defineOutboundAPILogHeaderTable(backendName, backendDetailEnricher));
      qInstance.addPossibleValueSource(defineOutboundAPILogHeaderPossibleValueSource());
      qInstance.addTable(defineOutboundAPILogRequestTable(backendName, backendDetailEnricher));
      qInstance.addTable(defineOutboundAPILogResponseTable(backendName, backendDetailEnricher));
      defineJoins().forEach(join -> qInstance.add(join));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void defineMigrationProcesses(QInstance qInstance, String sourceTableName)
   {
      qInstance.addProcess(StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName("migrateOutboundApiLogToHeaderChildProcess")
         .withLabel("Migrate Outbound API Log Test to Header/Child")
         .withIcon(new QIcon().withName("drive_file_move"))
         .withTableName(sourceTableName)
         .withSourceTable(sourceTableName)
         .withDestinationTable(OutboundAPILogHeader.TABLE_NAME)
         .withExtractStepClass(MigrateOutboundAPILogExtractStep.class)
         .withTransformStepClass(MigrateOutboundAPILogTransformStep.class)
         .withLoadStepClass(MigrateOutboundAPILogLoadStep.class)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("url", QFieldType.INTEGER)
         ))
         .getProcessMetaData());

      qInstance.addProcess(StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName("migrateOutboundApiLogToMongoDBProcess")
         .withLabel("Migrate Outbound API Log Test to MongoDB")
         .withIcon(new QIcon().withName("drive_file_move"))
         .withTableName(sourceTableName)
         .withSourceTable(sourceTableName)
         .withDestinationTable(OutboundAPILog.TABLE_NAME + "MongoDB")
         .withExtractStepClass(MigrateOutboundAPILogExtractStep.class)
         .withTransformStepClass(MigrateOutboundAPILogTransformStep.class)
         .withLoadStepClass(MigrateOutboundAPILogLoadStep.class)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("url", QFieldType.INTEGER)
         ))
         .getProcessMetaData());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QPossibleValueSource> definePossibleValueSources()
   {
      List<QPossibleValueSource> rs = new ArrayList<>();

      rs.add(new QPossibleValueSource()
         .withName("outboundApiMethod")
         .withType(QPossibleValueSourceType.ENUM)
         .withEnumValues(List.of(
            new QPossibleValue<>("GET"),
            new QPossibleValue<>("POST"),
            new QPossibleValue<>("PUT"),
            new QPossibleValue<>("PATCH"),
            new QPossibleValue<>("DELETE")
         )));

      rs.add(new QPossibleValueSource()
         .withName("outboundApiStatusCode")
         .withType(QPossibleValueSourceType.ENUM)
         .withEnumValues(List.of(
            new QPossibleValue<>(200, "200 (OK)"),
            new QPossibleValue<>(201, "201 (Created)"),
            new QPossibleValue<>(204, "204 (No Content)"),
            new QPossibleValue<>(207, "207 (Multi-Status)"),
            new QPossibleValue<>(400, "400 (Bad Request)"),
            new QPossibleValue<>(401, "401 (Not Authorized)"),
            new QPossibleValue<>(403, "403 (Forbidden)"),
            new QPossibleValue<>(404, "404 (Not Found)"),
            new QPossibleValue<>(422, "422 (Unprocessable Entity)"),
            new QPossibleValue<>(429, "429 (Too Many Requests)"),
            new QPossibleValue<>(500, "500 (Internal Server Error)"),
            new QPossibleValue<>(502, "502 (Bad Gateway)"),
            new QPossibleValue<>(503, "503 (Service Unavailable)"),
            new QPossibleValue<>(504, "500 (Gateway Timeout)")
         )));

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineOutboundAPILogTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName(OutboundAPILog.TABLE_NAME)
         .withLabel("Outbound API Log")
         .withIcon(new QIcon().withName("data_object"))
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("id")
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(OutboundAPILog.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("request", new QIcon().withName("arrow_upward"), Tier.T2, List.of("method", "url", "requestBody")))
         .withSection(new QFieldSection("response", new QIcon().withName("arrow_downward"), Tier.T2, List.of("statusCode", "responseBody")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("timestamp")))
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE);

      tableMetaData.getField("requestBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));
      tableMetaData.getField("responseBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

      addChipAdornmentToMethodField(tableMetaData);
      addChipAdornmentToStatusCodeField(tableMetaData);

      ///////////////////////////////////////////
      // these are the lengths of a MySQL TEXT //
      ///////////////////////////////////////////
      tableMetaData.getField("requestBody").withMaxLength(16_777_215).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);
      tableMetaData.getField("responseBody").withMaxLength(16_777_215).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);

      /////////////////////////
      // limit url to 250... //
      /////////////////////////
      tableMetaData.getField("url").withMaxLength(4096).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);
      tableMetaData.getField("url").withFieldAdornment(AdornmentType.Size.XLARGE.toAdornment());

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(tableMetaData);
      }

      return (tableMetaData);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void addChipAdornmentToStatusCodeField(QTableMetaData tableMetaData)
   {
      tableMetaData.getField("statusCode").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValue(AdornmentType.ChipValues.colorValue(200, AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue(201, AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue(204, AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue(207, AdornmentType.ChipValues.COLOR_INFO))
         .withValue(AdornmentType.ChipValues.colorValue(400, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(401, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(403, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(404, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(422, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(429, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(500, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(502, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(503, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(504, AdornmentType.ChipValues.COLOR_ERROR))
      );
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void addChipAdornmentToMethodField(QTableMetaData tableMetaData)
   {
      tableMetaData.getField("method").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValue(AdornmentType.ChipValues.colorValue("GET", AdornmentType.ChipValues.COLOR_INFO))
         .withValue(AdornmentType.ChipValues.colorValue("POST", AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue("DELETE", AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue("PATCH", AdornmentType.ChipValues.COLOR_WARNING))
         .withValue(AdornmentType.ChipValues.colorValue("PUT", AdornmentType.ChipValues.COLOR_WARNING)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData defineOutboundAPILogHeaderTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName(OutboundAPILogHeader.TABLE_NAME)
         .withLabel("Outbound API Log Header/Child")
         .withIcon(new QIcon().withName("data_object"))
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("id")
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(OutboundAPILogHeader.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("request", new QIcon().withName("arrow_upward"), Tier.T2, List.of("method", "url", OutboundAPILogRequest.TABLE_NAME + ".requestBody")))
         .withSection(new QFieldSection("response", new QIcon().withName("arrow_downward"), Tier.T2, List.of("statusCode", OutboundAPILogResponse.TABLE_NAME + ".responseBody")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("timestamp")))
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE);

      // tableMetaData.getField(OutboundAPILogRequest.TABLE_NAME + ".requestBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));
      // tableMetaData.getField(OutboundAPILogResponse.TABLE_NAME + ".responseBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

      addChipAdornmentToMethodField(tableMetaData);
      addChipAdornmentToStatusCodeField(tableMetaData);

      tableMetaData.withAssociation(new Association()
         .withName(OutboundAPILogRequest.TABLE_NAME)
         .withAssociatedTableName(OutboundAPILogRequest.TABLE_NAME)
         .withJoinName(QJoinMetaData.makeInferredJoinName(OutboundAPILogHeader.TABLE_NAME, OutboundAPILogRequest.TABLE_NAME)));

      tableMetaData.withAssociation(new Association()
         .withName(OutboundAPILogResponse.TABLE_NAME)
         .withAssociatedTableName(OutboundAPILogResponse.TABLE_NAME)
         .withJoinName(QJoinMetaData.makeInferredJoinName(OutboundAPILogHeader.TABLE_NAME, OutboundAPILogResponse.TABLE_NAME)));

      tableMetaData.withExposedJoin(new ExposedJoin()
         .withJoinTable(OutboundAPILogRequest.TABLE_NAME)
         .withJoinPath(List.of(QJoinMetaData.makeInferredJoinName(OutboundAPILogHeader.TABLE_NAME, OutboundAPILogRequest.TABLE_NAME))));

      tableMetaData.withExposedJoin(new ExposedJoin()
         .withJoinTable(OutboundAPILogResponse.TABLE_NAME)
         .withJoinPath(List.of(QJoinMetaData.makeInferredJoinName(OutboundAPILogHeader.TABLE_NAME, OutboundAPILogResponse.TABLE_NAME))));

      tableMetaData.getField("url").withMaxLength(4096).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);
      tableMetaData.getField("url").withFieldAdornment(AdornmentType.Size.XLARGE.toAdornment());

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(tableMetaData);
      }

      return (tableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData defineOutboundAPILogRequestTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName(OutboundAPILogRequest.TABLE_NAME)
         .withLabel("Outbound API Log Request")
         .withIcon(new QIcon().withName("arrow_upward"))
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("id")
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(OutboundAPILogRequest.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "outboundApiLogHeaderId")))
         .withSection(new QFieldSection("request", new QIcon().withName("arrow_upward"), Tier.T2, List.of("requestBody")))
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE);

      tableMetaData.getField("requestBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

      //////////////////////////////////////////////
      // this is the length of a MySQL MEDIUMTEXT //
      //////////////////////////////////////////////
      tableMetaData.getField("requestBody").withMaxLength(16_777_215).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(tableMetaData);
      }

      return (tableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData defineOutboundAPILogResponseTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName(OutboundAPILogResponse.TABLE_NAME)
         .withLabel("Outbound API Log Response")
         .withIcon(new QIcon().withName("arrow_upward"))
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("id")
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(OutboundAPILogResponse.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "outboundApiLogHeaderId")))
         .withSection(new QFieldSection("response", new QIcon().withName("arrow_upward"), Tier.T2, List.of("responseBody")))
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE);

      tableMetaData.getField("responseBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

      //////////////////////////////////////////////
      // this is the length of a MySQL MEDIUMTEXT //
      //////////////////////////////////////////////
      tableMetaData.getField("responseBody").withMaxLength(16_777_215).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(tableMetaData);
      }

      return (tableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QJoinMetaData> defineJoins()
   {
      List<QJoinMetaData> rs = new ArrayList<>();

      rs.add(new QJoinMetaData()
         .withLeftTable(OutboundAPILogHeader.TABLE_NAME)
         .withRightTable(OutboundAPILogRequest.TABLE_NAME)
         .withInferredName()
         .withType(JoinType.ONE_TO_ONE)
         .withJoinOn(new JoinOn("id", "outboundApiLogHeaderId")));

      rs.add(new QJoinMetaData()
         .withLeftTable(OutboundAPILogHeader.TABLE_NAME)
         .withRightTable(OutboundAPILogResponse.TABLE_NAME)
         .withInferredName()
         .withType(JoinType.ONE_TO_ONE)
         .withJoinOn(new JoinOn("id", "outboundApiLogHeaderId")));

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static QPossibleValueSource defineOutboundAPILogHeaderPossibleValueSource()
   {
      return QPossibleValueSource.newForTable(OutboundAPILogHeader.TABLE_NAME);
   }

}
