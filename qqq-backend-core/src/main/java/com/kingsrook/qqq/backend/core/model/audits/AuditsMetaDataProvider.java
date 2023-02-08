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

package com.kingsrook.qqq.backend.core.model.audits;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 **
 *******************************************************************************/
public class AuditsMetaDataProvider
{
   public static final String TABLE_NAME_AUDIT_TABLE  = "auditTable";
   public static final String TABLE_NAME_AUDIT_USER   = "auditUser";
   public static final String TABLE_NAME_AUDIT        = "audit";
   public static final String TABLE_NAME_AUDIT_DETAIL = "auditDetail";



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      defineStandardAuditTables(instance, backendName, backendDetailEnricher);
      defineStandardAuditPossibleValueSources(instance);
      defineStandardAuditJoins(instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineStandardAuditJoins(QInstance instance)
   {
      instance.addJoin(new QJoinMetaData()
         .withLeftTable(TABLE_NAME_AUDIT)
         .withRightTable(TABLE_NAME_AUDIT_TABLE)
         .withInferredName()
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("auditTableId", "id")));

      instance.addJoin(new QJoinMetaData()
         .withLeftTable(TABLE_NAME_AUDIT)
         .withRightTable(TABLE_NAME_AUDIT_USER)
         .withInferredName()
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("auditUserId", "id")));

      instance.addJoin(new QJoinMetaData()
         .withLeftTable(TABLE_NAME_AUDIT)
         .withRightTable(TABLE_NAME_AUDIT_DETAIL)
         .withInferredName()
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "auditId")));

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardAuditTables(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      for(QTableMetaData tableMetaData : defineStandardAuditTables(backendName, backendDetailEnricher))
      {
         instance.addTable(tableMetaData);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardAuditPossibleValueSources(QInstance instance)
   {
      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(TABLE_NAME_AUDIT_TABLE)
         .withTableName(TABLE_NAME_AUDIT_TABLE)
      );

      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(TABLE_NAME_AUDIT_USER)
         .withTableName(TABLE_NAME_AUDIT_USER)
      );

      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName(TABLE_NAME_AUDIT)
         .withTableName(TABLE_NAME_AUDIT)
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QTableMetaData> defineStandardAuditTables(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      List<QTableMetaData> rs = new ArrayList<>();
      rs.add(enrich(backendDetailEnricher, defineAuditUserTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineAuditTableTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineAuditTable(backendName)));
      rs.add(enrich(backendDetailEnricher, defineAuditDetailTable(backendName)));
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
   private QTableMetaData defineAuditTableTable(String backendName)
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_AUDIT_TABLE)
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("label")
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("name"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("label", QFieldType.STRING))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineAuditUserTable(String backendName)
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_AUDIT_USER)
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("name")
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("name"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineAuditTable(String backendName)
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_AUDIT)
         .withBackendName(backendName)
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("auditTableId", "recordId")
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("auditTableId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_AUDIT_TABLE))
         .withField(new QFieldMetaData("auditUserId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_AUDIT_USER))
         .withField(new QFieldMetaData("recordId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("message", QFieldType.STRING).withMaxLength(250).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS))
         .withField(new QFieldMetaData("timestamp", QFieldType.DATE_TIME));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineAuditDetailTable(String backendName)
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_AUDIT_DETAIL)
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("id")
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("auditId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_AUDIT))
         .withField(new QFieldMetaData("message", QFieldType.STRING).withMaxLength(250).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS));
   }

}
