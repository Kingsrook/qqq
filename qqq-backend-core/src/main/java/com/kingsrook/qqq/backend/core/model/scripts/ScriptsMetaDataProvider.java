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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class ScriptsMetaDataProvider
{

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
   public List<QTableMetaData> defineStandardScriptsTables(String backendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
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
      return (defineStandardTable(backendName, Script.TABLE_NAME, Script.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptTypeTable(String backendName) throws QException
   {
      return (defineStandardTable(backendName, ScriptType.TABLE_NAME, ScriptType.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptRevisionTable(String backendName) throws QException
   {
      return (defineStandardTable(backendName, ScriptRevision.TABLE_NAME, ScriptRevision.class)
         .withRecordLabelFields(List.of("id")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineScriptLogTable(String backendName) throws QException
   {
      return (defineStandardTable(backendName, ScriptLog.TABLE_NAME, ScriptLog.class)
         .withRecordLabelFields(List.of("id")));
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
