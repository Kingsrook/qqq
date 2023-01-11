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
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Helper object used throughout query (and related (count, aggregate, reporting))
 ** actions that need to track joins and aliases.
 *******************************************************************************/
public class JoinsContext
{
   private final QInstance       instance;
   private final String          mainTableName;
   private final List<QueryJoin> queryJoins;

   ////////////////////////////////////////////////////////////////
   // note - will have entries for all tables, not just aliases. //
   ////////////////////////////////////////////////////////////////
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
         QTableMetaData joinTable        = instance.getTable(queryJoin.getJoinTable());
         String         tableNameOrAlias = queryJoin.getJoinTableOrItsAlias();
         if(aliasToTableNameMap.containsKey(tableNameOrAlias))
         {
            throw (new QException("Duplicate table name or alias: " + tableNameOrAlias));
         }
         aliasToTableNameMap.put(tableNameOrAlias, joinTable.getName());
      }

      ///////////////////////////////////////////////////////////////
      // ensure any joins that contribute a recordLock are present //
      ///////////////////////////////////////////////////////////////
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(instance.getTable(tableName).getRecordSecurityLocks()))
      {
         for(String joinName : CollectionUtils.nonNullList(recordSecurityLock.getJoinChain()))
         {
            if(this.queryJoins.stream().anyMatch(qj -> qj.getJoinMetaData().getName().equals(joinName)))
            {
               ///////////////////////////////////////////////////////
               // we're good - we're already joining on this table! //
               ///////////////////////////////////////////////////////
            }
            else
            {
               this.queryJoins.add(new QueryJoin().withJoinMetaData(instance.getJoin(joinName)).withType(QueryJoin.Type.INNER)); // todo aliases?  probably.
            }
         }
      }

      /* todo!!
      for(QueryJoin queryJoin : queryJoins)
      {
         QTableMetaData joinTable = instance.getTable(queryJoin.getJoinTable());
         for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(joinTable.getRecordSecurityLocks()))
         {
            // addCriteriaForRecordSecurityLock(instance, session, joinTable, securityCriteria, recordSecurityLock, joinsContext, queryJoin.getJoinTableOrItsAlias());
         }
      }
       */
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
    ** For a given name (whether that's a table name or an alias in the query),
    ** get the actual table name (e.g., that could be passed to qInstance.getTable())
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
    ** For a given fieldName, which we expect may start with a tableNameOrAlias + '.',
    ** find the QFieldMetaData and the tableNameOrAlias that it corresponds to.
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
    ** Check if the given table name exists in the query - but that name may NOT
    ** be an alias - it must be an actual table name.
    **
    ** e.g., Given:
    **   FROM `order` INNER JOIN line_item li
    ** hasAliasOrTable("order") => true
    ** hasAliasOrTable("li") => false
    ** hasAliasOrTable("line_item") => true
    *******************************************************************************/
   public boolean hasTable(String table)
   {
      return (mainTableName.equals(table) || aliasToTableNameMap.containsValue(table));
   }



   /*******************************************************************************
    ** Check if the given tableOrAlias exists in the query - but note, if a table
    ** is in the query, but with an alias, then it would not be found by this method.
    **
    ** e.g., Given:
    **   FROM `order` INNER JOIN line_item li
    ** hasAliasOrTable("order") => false
    ** hasAliasOrTable("li") => true
    ** hasAliasOrTable("line_item") => false
    *******************************************************************************/
   public boolean hasAliasOrTable(String tableOrAlias)
   {
      return (mainTableName.equals(tableOrAlias) || aliasToTableNameMap.containsKey(tableOrAlias));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public record FieldAndTableNameOrAlias(QFieldMetaData field, String tableNameOrAlias)
   {
   }

}
