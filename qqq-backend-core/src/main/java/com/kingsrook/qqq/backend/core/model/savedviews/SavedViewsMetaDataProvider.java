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

package com.kingsrook.qqq.backend.core.model.savedviews;


import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.sharing.ShareScopePossibleValueMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.processes.implementations.savedviews.DeleteSavedViewProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.savedviews.QuerySavedViewProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.savedviews.StoreSavedViewProcess;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedViewsMetaDataProvider
{
   public static final String SHARED_SAVED_VIEW_JOIN_SAVED_VIEW = "sharedSavedViewJoinSavedView";


   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      instance.addTable(defineSavedViewTable(backendName, backendDetailEnricher));
      instance.addPossibleValueSource(defineSavedViewPossibleValueSource());
      instance.addProcess(QuerySavedViewProcess.getProcessMetaData());
      instance.addProcess(StoreSavedViewProcess.getProcessMetaData());
      instance.addProcess(DeleteSavedViewProcess.getProcessMetaData());

      /////////////////////////////////////
      // todo - param to enable sharing? //
      /////////////////////////////////////
      instance.addTable(defineSharedSavedViewTable(backendName, backendDetailEnricher));
      instance.addJoin(defineSharedSavedViewJoinSavedView());
      if(instance.getPossibleValueSource(ShareScopePossibleValueMetaDataProducer.NAME) == null)
      {
         instance.addPossibleValueSource(new ShareScopePossibleValueMetaDataProducer().produce(new QInstance()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData defineSavedViewTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData table = new QTableMetaData()
         .withName(SavedView.TABLE_NAME)
         .withLabel("View")
         .withIcon(new QIcon().withName("table_view"))
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("label")
         .withBackendName(backendName)
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(SavedView.class)
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "label")))
         .withSection(new QFieldSection("data", new QIcon().withName("text_snippet"), Tier.T2, List.of("userId", "tableName", "viewJson")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));

      table.getField("viewJson").withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

      table.withCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(SavedViewTableCustomizer.class));
      table.withCustomizer(TableCustomizers.PRE_DELETE_RECORD, new QCodeReference(SavedViewTableCustomizer.class));

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(table);
      }

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QPossibleValueSource defineSavedViewPossibleValueSource()
   {
      return new QPossibleValueSource()
         .withName(SavedView.TABLE_NAME)
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(SavedView.TABLE_NAME)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY)
         .withOrderByField("label");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData defineSharedSavedViewTable(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      QTableMetaData table = new QTableMetaData()
         .withName(SharedSavedView.TABLE_NAME)
         .withLabel("Shared View")
         .withIcon(new QIcon().withName("share"))
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("savedViewId")
         .withBackendName(backendName)
         .withUniqueKey(new UniqueKey("savedViewId", "userId"))
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(SharedSavedView.class)
         // todo - security key
         .withAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD))
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "savedViewId", "userId")))
         .withSection(new QFieldSection("data", new QIcon().withName("text_snippet"), Tier.T2, List.of("scope")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));

      if(backendDetailEnricher != null)
      {
         backendDetailEnricher.accept(table);
      }

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QJoinMetaData defineSharedSavedViewJoinSavedView()
   {
      return (new QJoinMetaData()
         .withName(SHARED_SAVED_VIEW_JOIN_SAVED_VIEW)
         .withLeftTable(SharedSavedView.TABLE_NAME)
         .withRightTable(SavedView.TABLE_NAME)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("savedViewId", "id")));
   }

}
