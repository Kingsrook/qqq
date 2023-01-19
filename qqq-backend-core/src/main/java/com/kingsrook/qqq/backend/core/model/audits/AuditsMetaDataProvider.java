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
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 **
 *******************************************************************************/
public class AuditsMetaDataProvider
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      defineStandardAuditTables(instance, backendName, backendDetailEnricher);
      defineStandardAuditPossibleValueSources(instance);
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
         .withName("auditTable")
         .withTableName("auditTable")
      );

      instance.addPossibleValueSource(new QPossibleValueSource()
         .withName("auditUser")
         .withTableName("auditUser")
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
         .withName("auditTable")
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
         .withName("auditUser")
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
         .withName("audit")
         .withBackendName(backendName)
         .withRecordLabelFormat("%s")
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("auditTableId", QFieldType.INTEGER).withPossibleValueSourceName("auditTable"))
         .withField(new QFieldMetaData("auditUserId", QFieldType.INTEGER).withPossibleValueSourceName("auditUser"))
         .withField(new QFieldMetaData("recordId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("message", QFieldType.STRING).withMaxLength(250).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS))
         .withField(new QFieldMetaData("timestamp", QFieldType.DATE_TIME));
   }

}
