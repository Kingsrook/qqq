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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
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
   public JoinsContext(QInstance instance, String tableName, List<QueryJoin> queryJoins, QQueryFilter filter) throws QException
   {
      this.instance = instance;
      this.mainTableName = tableName;
      this.queryJoins = CollectionUtils.nonNullList(queryJoins);

      for(QueryJoin queryJoin : this.queryJoins)
      {
         processQueryJoin(queryJoin);
      }

      ///////////////////////////////////////////////////////////////
      // ensure any joins that contribute a recordLock are present //
      ///////////////////////////////////////////////////////////////
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(instance.getTable(tableName).getRecordSecurityLocks()))
      {
         ///////////////////////////////////////////////////////////////////////////////////////////////////
         // ok - so - the join name chain is going to be like this:                                       //
         // for a table:  orderLineItemExtrinsic (that's 2 away from order, where the security field is): //
         // - securityFieldName = order.clientId                                                          //
         // - joinNameChain = orderJoinOrderLineItem, orderLineItemJoinOrderLineItemExtrinsic             //
         // so - to navigate from the table to the security field, we need to reverse the joinNameChain,  //
         // and step (via tmpTable variable) back to the securityField                                    //
         ///////////////////////////////////////////////////////////////////////////////////////////////////
         ArrayList<String> joinNameChain = new ArrayList<>(CollectionUtils.nonNullList(recordSecurityLock.getJoinNameChain()));
         Collections.reverse(joinNameChain);

         QTableMetaData tmpTable = instance.getTable(mainTableName);

         for(String joinName : joinNameChain)
         {
            if(this.queryJoins.stream().anyMatch(queryJoin ->
            {
               QJoinMetaData joinMetaData = Objects.requireNonNullElseGet(queryJoin.getJoinMetaData(), () -> findJoinMetaData(instance, tableName, queryJoin.getJoinTable()));
               return (joinMetaData != null && Objects.equals(joinMetaData.getName(), joinName));
            }))
            {
               continue;
            }

            QJoinMetaData join = instance.getJoin(joinName);
            if(join.getLeftTable().equals(tmpTable.getName()))
            {
               QueryJoin queryJoin = new ImplicitQueryJoinForSecurityLock().withJoinMetaData(join).withType(QueryJoin.Type.INNER);
               this.queryJoins.add(queryJoin); // todo something else with aliases?  probably.
               tmpTable = instance.getTable(join.getRightTable());
            }
            else if(join.getRightTable().equals(tmpTable.getName()))
            {
               QueryJoin queryJoin = new ImplicitQueryJoinForSecurityLock().withJoinMetaData(join.flip()).withType(QueryJoin.Type.INNER);
               this.queryJoins.add(queryJoin); // todo something else with aliases?  probably.
               tmpTable = instance.getTable(join.getLeftTable());
            }
            else
            {
               throw (new QException("Error adding security lock joins to query - table name [" + tmpTable.getName() + "] not found in join [" + joinName + "]"));
            }
         }
      }

      ensureFilterIsRepresented(filter);

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
    **
    *******************************************************************************/
   private void processQueryJoin(QueryJoin queryJoin) throws QException
   {
      QTableMetaData joinTable        = QContext.getQInstance().getTable(queryJoin.getJoinTable());
      String         tableNameOrAlias = queryJoin.getJoinTableOrItsAlias();
      if(aliasToTableNameMap.containsKey(tableNameOrAlias))
      {
         throw (new QException("Duplicate table name or alias: " + tableNameOrAlias));
      }
      aliasToTableNameMap.put(tableNameOrAlias, joinTable.getName());
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
   private void ensureFilterIsRepresented(QQueryFilter filter) throws QException
   {
      Set<String> filterTables = new HashSet<>();
      populateFilterTablesSet(filter, filterTables);

      for(String filterTable : filterTables)
      {
         if(!aliasToTableNameMap.containsKey(filterTable) && !Objects.equals(mainTableName, filterTable))
         {
            for(QJoinMetaData join : CollectionUtils.nonNullMap(QContext.getQInstance().getJoins()).values())
            {
               QueryJoin queryJoin = null;
               if(join.getLeftTable().equals(mainTableName) && join.getRightTable().equals(filterTable))
               {
                  queryJoin = new QueryJoin().withJoinMetaData(join).withType(QueryJoin.Type.INNER);
               }
               else
               {
                  join = join.flip();
                  if(join.getLeftTable().equals(mainTableName) && join.getRightTable().equals(filterTable))
                  {
                     queryJoin = new QueryJoin().withJoinMetaData(join).withType(QueryJoin.Type.INNER);
                  }
               }

               if(queryJoin != null)
               {
                  this.queryJoins.add(queryJoin); // todo something else with aliases?  probably.
                  processQueryJoin(queryJoin);
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void populateFilterTablesSet(QQueryFilter filter, Set<String> filterTables)
   {
      if(filter != null)
      {
         for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
         {
            getTableNameFromFieldNameAndAddToSet(criteria.getFieldName(), filterTables);
            getTableNameFromFieldNameAndAddToSet(criteria.getOtherFieldName(), filterTables);
         }

         for(QQueryFilter subFilter : CollectionUtils.nonNullList(filter.getSubFilters()))
         {
            populateFilterTablesSet(subFilter, filterTables);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void getTableNameFromFieldNameAndAddToSet(String fieldName, Set<String> filterTables)
   {
      if(fieldName != null && fieldName.contains("."))
      {
         String tableName = fieldName.replaceFirst("\\..*", "");
         filterTables.add(tableName);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJoinMetaData findJoinMetaData(QInstance instance, String baseTableName, String joinTableName)
   {
      List<QJoinMetaData> matches = new ArrayList<>();
      if(baseTableName != null)
      {
         ///////////////////////////////////////////////////////////////////////////
         // if query specified a left-table, look for a join between left & right //
         ///////////////////////////////////////////////////////////////////////////
         for(QJoinMetaData join : instance.getJoins().values())
         {
            if(join.getLeftTable().equals(baseTableName) && join.getRightTable().equals(joinTableName))
            {
               matches.add(join);
            }

            //////////////////////////////
            // look in both directions! //
            //////////////////////////////
            if(join.getRightTable().equals(baseTableName) && join.getLeftTable().equals(joinTableName))
            {
               matches.add(join.flip());
            }
         }
      }
      else
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // if query didn't specify a left-table, then look for any join to the right table //
         /////////////////////////////////////////////////////////////////////////////////////
         for(QJoinMetaData join : instance.getJoins().values())
         {
            if(join.getRightTable().equals(joinTableName) && this.hasTable(join.getLeftTable()))
            {
               matches.add(join);
            }

            //////////////////////////////
            // look in both directions! //
            //////////////////////////////
            if(join.getLeftTable().equals(joinTableName) && this.hasTable(join.getRightTable()))
            {
               matches.add(join.flip());
            }
         }
      }

      if(matches.size() == 1)
      {
         return (matches.get(0));
      }
      else if(matches.size() > 1)
      {
         throw (new RuntimeException("More than 1 join was found between [" + baseTableName + "] and [" + joinTableName + "].  Specify which one in your QueryJoin."));
      }

      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public record FieldAndTableNameOrAlias(QFieldMetaData field, String tableNameOrAlias)
   {
   }

}
