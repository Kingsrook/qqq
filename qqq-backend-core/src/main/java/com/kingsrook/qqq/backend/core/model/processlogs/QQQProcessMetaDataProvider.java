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
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.metadata.tables.cache.CacheOf;
import com.kingsrook.qqq.backend.core.model.metadata.tables.cache.CacheUseCase;


/*******************************************************************************
 **
 *******************************************************************************/
public class QQQProcessMetaDataProvider
{
   public static final String QQQ_PROCESS_CACHE_TABLE_NAME = QQQProcess.TABLE_NAME + "Cache";



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineAll(QInstance instance, String persistentBackendName, String cacheBackendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      defineStandardTables(instance, persistentBackendName, cacheBackendName, backendDetailEnricher);
      defineStandardPossibleValueSources(instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void defineStandardPossibleValueSources(QInstance instance)
   {
      instance.addPossibleValueSource(QPossibleValueSource.newForTable(QQQProcess.TABLE_NAME, "label"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void defineStandardTables(QInstance instance, String persistentBackendName, String cacheBackendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      for(QTableMetaData tableMetaData : defineStandardTables(persistentBackendName, cacheBackendName, backendDetailEnricher))
      {
         instance.addTable(tableMetaData);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QTableMetaData> defineStandardTables(String persistentBackendName, String cacheBackendName, Consumer<QTableMetaData> backendDetailEnricher) throws QException
   {
      List<QTableMetaData> rs = new ArrayList<>();
      rs.add(enrich(backendDetailEnricher, defineQQQProcessTable(persistentBackendName)));
      rs.add(enrich(backendDetailEnricher, defineQQQProcessCacheTable(cacheBackendName)));
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
   private QTableMetaData defineQQQProcessTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, QQQProcess.TABLE_NAME, QQQProcess.class)
         .withUniqueKey(new UniqueKey("label"))
         .withRecordLabelFields("label");

      return tableMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineQQQProcessCacheTable(String backendName) throws QException
   {
      QTableMetaData tableMetaData = defineStandardTable(backendName, QQQ_PROCESS_CACHE_TABLE_NAME, QQQProcess.class)
         .withRecordLabelFields("label")
         .withUniqueKey(new UniqueKey("name"))
         .withCacheOf(new CacheOf()
            .withSourceTable(QQQProcess.TABLE_NAME)
            .withUseCase(new CacheUseCase()
               .withType(CacheUseCase.Type.UNIQUE_KEY_TO_UNIQUE_KEY)
               .withCacheUniqueKey(new UniqueKey("name"))
               .withSourceUniqueKey(new UniqueKey("name"))
               .withDoCopySourcePrimaryKeyToCache(true)
            )
         );

      return tableMetaData;
   }

}
