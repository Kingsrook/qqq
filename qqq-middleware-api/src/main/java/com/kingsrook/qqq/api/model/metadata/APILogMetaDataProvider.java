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

package com.kingsrook.qqq.api.model.metadata;


import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.api.model.APILog;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 **
 *******************************************************************************/
public class APILogMetaDataProvider
{
   public static final String TABLE_NAME_API_LOG      = "apiLog";
   public static final String TABLE_NAME_API_LOG_USER = "apiLogUser";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void defineAll(QInstance qInstance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      defineApiLogUserPvs(qInstance);
      defineAPILogTable(qInstance, backendName, backendDetailEnricher);
      defineAPILogUserTable(qInstance, backendName, backendDetailEnricher);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void defineApiLogUserPvs(QInstance instance)
   {
      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(TABLE_NAME_API_LOG_USER)
         .withTableName(TABLE_NAME_API_LOG_USER));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void defineAPILogUserTable(QInstance qInstance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName(TABLE_NAME_API_LOG_USER)
         .withLabel("API Log User")
         .withIcon(new QIcon().withName("person"))
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("name")
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("name"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING).withIsRequired(true))
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "name")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")))
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE);

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(tableMetaData);
      }

      qInstance.addTable(tableMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void defineAPILogTable(QInstance qInstance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName(TABLE_NAME_API_LOG)
         .withLabel("API Log")
         .withIcon(new QIcon().withName("data_object"))
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(APILog.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "apiLogUserId")))
         .withSection(new QFieldSection("request", new QIcon().withName("arrow_upward"), Tier.T2, List.of("method", "version", "path", "queryString", "requestBody")))
         .withSection(new QFieldSection("response", new QIcon().withName("arrow_downward"), Tier.T2, List.of("statusCode", "responseBody")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("timestamp")))
         .withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE);

      tableMetaData.getField("requestBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));
      tableMetaData.getField("responseBody").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

      tableMetaData.getField("method").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValue(AdornmentType.ChipValues.colorValue("GET", AdornmentType.ChipValues.COLOR_INFO))
         .withValue(AdornmentType.ChipValues.colorValue("POST", AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue("DELETE", AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue("PATCH", AdornmentType.ChipValues.COLOR_WARNING)));

      tableMetaData.getField("statusCode").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValue(AdornmentType.ChipValues.colorValue(200, AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue(201, AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue(204, AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValue(AdornmentType.ChipValues.colorValue(207, AdornmentType.ChipValues.COLOR_INFO))
         .withValue(AdornmentType.ChipValues.colorValue(400, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(401, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(403, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(404, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(429, AdornmentType.ChipValues.COLOR_ERROR))
         .withValue(AdornmentType.ChipValues.colorValue(500, AdornmentType.ChipValues.COLOR_ERROR)));

      ///////////////////////////////////////////
      // these are the lengths of a MySQL TEXT //
      ///////////////////////////////////////////
      tableMetaData.getField("requestBody").withMaxLength(65_535).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);
      tableMetaData.getField("responseBody").withMaxLength(65_535).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);

      //////////////////////////////////////////////////////////////////////////////////////////////
      // internet doesn't agree on max-length for a URL, but let's go with ... 4K on query string //
      //////////////////////////////////////////////////////////////////////////////////////////////
      tableMetaData.getField("queryString").withMaxLength(4096).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);

      ////////////////////////////////////////
      // and we expect short paths, 100 max //
      ////////////////////////////////////////
      tableMetaData.getField("path").withMaxLength(100).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(tableMetaData);
      }

      qInstance.addTable(tableMetaData);
   }
}
