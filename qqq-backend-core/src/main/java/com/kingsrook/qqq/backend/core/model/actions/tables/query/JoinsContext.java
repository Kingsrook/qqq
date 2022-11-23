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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Helper object used throughout query (and related (count, aggregate, reporting))
 ** actions that need to track joins and aliases.
 *******************************************************************************/
public class JoinsContext
{
   private final QInstance           instance;
   private final String              mainTableName;
   private final List<QueryJoin>     queryJoins;
   private final Map<String, String> aliasToTableNameMap = new HashMap<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public JoinsContext(QInstance instance, String tableName, List<QueryJoin> queryJoins) throws QException
   {
      this.instance = instance;
      this.mainTableName = tableName;
      this.queryJoins = CollectionUtils.nonNullList(queryJoins);

      for(QueryJoin queryJoin : this.queryJoins)
      {
         QTableMetaData joinTable        = instance.getTable(queryJoin.getRightTable());
         String         tableNameOrAlias = queryJoin.getAliasOrRightTable();
         if(aliasToTableNameMap.containsKey(tableNameOrAlias))
         {
            throw (new QException("Duplicate table name or alias: " + tableNameOrAlias));
         }
         aliasToTableNameMap.put(tableNameOrAlias, joinTable.getName());
      }
   }



   /*******************************************************************************
    ** Getter for queryJoins
    **
    *******************************************************************************/
   public List<QueryJoin> getQueryJoins()
   {
      return queryJoins;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String resolveTableNameOrAliasToTableName(String nameOrAlias)
   {
      if(aliasToTableNameMap.containsKey(nameOrAlias))
      {
         return (aliasToTableNameMap.get(nameOrAlias));
      }
      return (nameOrAlias);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FieldAndTableNameOrAlias getFieldAndTableNameOrAlias(String fieldName)
   {
      if(fieldName.contains("."))
      {
         String[] parts = fieldName.split("\\.");
         if(parts.length != 2)
         {
            throw new IllegalArgumentException("Mal-formatted field name in query: " + fieldName);
         }

         String tableOrAlias  = parts[0];
         String baseFieldName = parts[1];
         String tableName     = resolveTableNameOrAliasToTableName(tableOrAlias);

         QTableMetaData table = instance.getTable(tableName);
         if(table == null)
         {
            throw new IllegalArgumentException("Could not find table [" + tableName + "] in instance for query");
         }
         return new FieldAndTableNameOrAlias(table.getField(baseFieldName), tableOrAlias);
      }

      return new FieldAndTableNameOrAlias(instance.getTable(mainTableName).getField(fieldName), mainTableName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public record FieldAndTableNameOrAlias(QFieldMetaData field, String tableNameOrAlias)
   {
   }

}
