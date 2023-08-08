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
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MutableList;
import org.apache.logging.log4j.Level;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Helper object used throughout query (and related (count, aggregate, reporting))
 ** actions that need to track joins and aliases.
 *******************************************************************************/
public class JoinsContext
{
   private static final QLogger LOG = QLogger.getLogger(JoinsContext.class);

   private final QInstance       instance;
   private final String          mainTableName;
   private final List<QueryJoin> queryJoins;

   ////////////////////////////////////////////////////////////////
   // note - will have entries for all tables, not just aliases. //
   ////////////////////////////////////////////////////////////////
   private final Map<String, String> aliasToTableNameMap = new HashMap<>();
   private       Level               logLevel            = Level.OFF;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public JoinsContext(QInstance instance, String tableName, List<QueryJoin> queryJoins, QQueryFilter filter) throws QException
   {
      log("--- START ----------------------------------------------------------------------", logPair("mainTable", tableName));
      this.instance = instance;
      this.mainTableName = tableName;
      this.queryJoins = new MutableList<>(queryJoins);

      for(QueryJoin queryJoin : this.queryJoins)
      {
         log("Processing input query join", logPair("joinTable", queryJoin.getJoinTable()), logPair("alias", queryJoin.getAlias()), logPair("baseTableOrAlias", queryJoin.getBaseTableOrAlias()), logPair("joinMetaDataName", () -> queryJoin.getJoinMetaData().getName()));
         processQueryJoin(queryJoin);
      }

      ///////////////////////////////////////////////////////////////
      // ensure any joins that contribute a recordLock are present //
      ///////////////////////////////////////////////////////////////
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(instance.getTable(tableName).getRecordSecurityLocks()))
      {
         ensureRecordSecurityLockIsRepresented(instance, tableName, recordSecurityLock);
      }

      ensureFilterIsRepresented(filter);

      addJoinsFromExposedJoinPaths();

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

      log("Constructed JoinsContext", logPair("mainTableName", this.mainTableName), logPair("queryJoins", this.queryJoins.stream().map(qj -> qj.getJoinTable()).collect(Collectors.joining(","))));
      log("--- END ------------------------------------------------------------------------");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void ensureRecordSecurityLockIsRepresented(QInstance instance, String tableName, RecordSecurityLock recordSecurityLock) throws QException
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
      log("Evaluating recordSecurityLock", logPair("recordSecurityLock", recordSecurityLock.getFieldName()), logPair("joinNameChain", joinNameChain));

      QTableMetaData tmpTable = instance.getTable(mainTableName);

      for(String joinName : joinNameChain)
      {
         ///////////////////////////////////////////////////////////////////////////////////////////////////////
         // check the joins currently in the query - if any are for this table, then we don't need to add one //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////
         List<QueryJoin> matchingJoins = this.queryJoins.stream().filter(queryJoin ->
         {
            QJoinMetaData joinMetaData = null;
            if(queryJoin.getJoinMetaData() != null)
            {
               joinMetaData = queryJoin.getJoinMetaData();
            }
            else
            {
               joinMetaData = findJoinMetaData(instance, tableName, queryJoin.getJoinTable());
            }
            return (joinMetaData != null && Objects.equals(joinMetaData.getName(), joinName));
         }).toList();

         if(CollectionUtils.nullSafeHasContents(matchingJoins))
         {
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // note - if a user added a join as an outer type, we need to change it to be inner, for the security purpose. //
            // todo - is this always right?  what about nulls-allowed?                                                     //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            log("- skipping join already in the query", logPair("joinName", joinName));

            if(matchingJoins.get(0).getType().equals(QueryJoin.Type.LEFT) || matchingJoins.get(0).getType().equals(QueryJoin.Type.RIGHT))
            {
               log("- - although... it was here as an outer - so switching it to INNER", logPair("joinName", joinName));
               matchingJoins.get(0).setType(QueryJoin.Type.INNER);
            }

            continue;
         }

         QJoinMetaData join = instance.getJoin(joinName);
         if(join.getLeftTable().equals(tmpTable.getName()))
         {
            QueryJoin queryJoin = new ImplicitQueryJoinForSecurityLock().withJoinMetaData(join).withType(QueryJoin.Type.INNER);
            this.addQueryJoin(queryJoin, "forRecordSecurityLock (non-flipped)");
            tmpTable = instance.getTable(join.getRightTable());
         }
         else if(join.getRightTable().equals(tmpTable.getName()))
         {
            QueryJoin queryJoin = new ImplicitQueryJoinForSecurityLock().withJoinMetaData(join.flip()).withType(QueryJoin.Type.INNER);
            this.addQueryJoin(queryJoin, "forRecordSecurityLock (flipped)");
            tmpTable = instance.getTable(join.getLeftTable());
         }
         else
         {
            throw (new QException("Error adding security lock joins to query - table name [" + tmpTable.getName() + "] not found in join [" + joinName + "]"));
         }
      }
   }



   /*******************************************************************************
    ** Add a query join to the list of query joins, and "process it"
    **
    ** use this method to add to the list, instead of ever adding directly, as it's
    ** important do to that process step (and we've had bugs when it wasn't done).
    *******************************************************************************/
   private void addQueryJoin(QueryJoin queryJoin, String reason) throws QException
   {
      log("Adding query join to context",
         logPair("reason", reason),
         logPair("joinTable", queryJoin.getJoinTable()),
         logPair("joinMetaData.name", () -> queryJoin.getJoinMetaData().getName()),
         logPair("joinMetaData.leftTable", () -> queryJoin.getJoinMetaData().getLeftTable()),
         logPair("joinMetaData.rightTable", () -> queryJoin.getJoinMetaData().getRightTable())
      );
      this.queryJoins.add(queryJoin);
      processQueryJoin(queryJoin);
   }



   /*******************************************************************************
    ** If there are any joins in the context that don't have a join meta data, see
    ** if we can find the JoinMetaData to use for them by looking at the main table's
    ** exposed joins, and using their join paths.
    *******************************************************************************/
   private void addJoinsFromExposedJoinPaths() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // do a double-loop, to avoid concurrent modification on the queryJoins list. //
      // that is to say, we'll loop over that list, but possibly add things to it,  //
      // in which case we'll set this flag, and break the inner loop, to go again.  //
      ////////////////////////////////////////////////////////////////////////////////
      boolean addedJoin;
      do
      {
         addedJoin = false;
         for(QueryJoin queryJoin : queryJoins)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////
            // if the join has joinMetaData, then we don't need to process it... unless it needs flipped //
            ///////////////////////////////////////////////////////////////////////////////////////////////
            QJoinMetaData joinMetaData = queryJoin.getJoinMetaData();
            if(joinMetaData != null)
            {
               boolean isJoinLeftTableInQuery = false;
               String  joinMetaDataLeftTable  = joinMetaData.getLeftTable();
               if(joinMetaDataLeftTable.equals(mainTableName))
               {
                  isJoinLeftTableInQuery = true;
               }

               ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // check the other joins in this query - if any of them have this join's left-table as their baseTable, then set the flag to true //
               ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               for(QueryJoin otherJoin : queryJoins)
               {
                  if(otherJoin == queryJoin)
                  {
                     continue;
                  }

                  if(Objects.equals(otherJoin.getBaseTableOrAlias(), joinMetaDataLeftTable))
                  {
                     isJoinLeftTableInQuery = true;
                     break;
                  }
               }

               /////////////////////////////////////////////////////////////////////////////////
               // if the join's left-table isn't in the query, then we need to flip the join. //
               /////////////////////////////////////////////////////////////////////////////////
               if(!isJoinLeftTableInQuery)
               {
                  log("Flipping queryJoin because its leftTable wasn't found in the query", logPair("joinMetaDataName", joinMetaData.getName()), logPair("leftTable", joinMetaDataLeftTable));
                  queryJoin.setJoinMetaData(joinMetaData.flip());
               }
            }
            else
            {
               //////////////////////////////////////////////////////////////////////
               // try to find a direct join between the main table and this table. //
               // if one is found, then put it (the meta data) on the query join.  //
               //////////////////////////////////////////////////////////////////////
               String        baseTableName = Objects.requireNonNullElse(resolveTableNameOrAliasToTableName(queryJoin.getBaseTableOrAlias()), mainTableName);
               QJoinMetaData found         = findJoinMetaData(instance, baseTableName, queryJoin.getJoinTable());
               if(found != null)
               {
                  log("Found joinMetaData - setting it in queryJoin", logPair("joinMetaDataName", found.getName()), logPair("baseTableName", baseTableName), logPair("joinTable", queryJoin.getJoinTable()));
                  queryJoin.setJoinMetaData(found);
               }
               else
               {
                  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // else, the join must be indirect - so look for an exposedJoin that will have a joinPath that will connect us //
                  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  QTableMetaData mainTable          = instance.getTable(mainTableName);
                  boolean        addedAnyQueryJoins = false;
                  for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(mainTable.getExposedJoins()))
                  {
                     if(queryJoin.getJoinTable().equals(exposedJoin.getJoinTable()))
                     {
                        log("Found an exposed join", logPair("mainTable", mainTableName), logPair("joinTable", queryJoin.getJoinTable()), logPair("joinPath", exposedJoin.getJoinPath()));

                        /////////////////////////////////////////////////////////////////////////////////////
                        // loop backward through the join path (from the joinTable back to the main table) //
                        // adding joins to the table (if they aren't already in the query)                 //
                        /////////////////////////////////////////////////////////////////////////////////////
                        String tmpTable = queryJoin.getJoinTable();
                        for(int i = exposedJoin.getJoinPath().size() - 1; i >= 0; i--)
                        {
                           String        joinName  = exposedJoin.getJoinPath().get(i);
                           QJoinMetaData joinToAdd = instance.getJoin(joinName);

                           /////////////////////////////////////////////////////////////////////////////
                           // get the name from the opposite side of the join (flipping it if needed) //
                           /////////////////////////////////////////////////////////////////////////////
                           String nextTable;
                           if(joinToAdd.getRightTable().equals(tmpTable))
                           {
                              nextTable = joinToAdd.getLeftTable();
                           }
                           else
                           {
                              nextTable = joinToAdd.getRightTable();
                              joinToAdd = joinToAdd.flip();
                           }

                           if(doesJoinNeedAddedToQuery(joinName))
                           {
                              ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                              // if this is the last element in the joinPath, then we want to set this joinMetaData on the outer queryJoin //
                              // - else, we need to add a new queryJoin to this context                                                    //
                              ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                              if(i == exposedJoin.getJoinPath().size() - 1)
                              {
                                 if(queryJoin.getBaseTableOrAlias() == null)
                                 {
                                    queryJoin.setBaseTableOrAlias(nextTable);
                                 }
                                 queryJoin.setJoinMetaData(joinToAdd);
                              }
                              else
                              {
                                 QueryJoin queryJoinToAdd = makeQueryJoinFromJoinAndTableNames(nextTable, tmpTable, joinToAdd);
                                 queryJoinToAdd.setType(queryJoin.getType());
                                 addedAnyQueryJoins = true;
                                 this.addQueryJoin(queryJoinToAdd, "forExposedJoin");
                              }
                           }

                           tmpTable = nextTable;
                        }
                     }
                  }

                  ///////////////////////////////////////////////////////////////////////////////////////////////////
                  // break the inner loop (it would fail due to a concurrent modification), but continue the outer //
                  ///////////////////////////////////////////////////////////////////////////////////////////////////
                  if(addedAnyQueryJoins)
                  {
                     addedJoin = true;
                     break;
                  }
               }
            }
         }
      }
      while(addedJoin);

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean doesJoinNeedAddedToQuery(String joinName)
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // look at all queryJoins already in context - if any have this join's name, then we don't need this join... //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(QueryJoin queryJoin : queryJoins)
      {
         if(queryJoin.getJoinMetaData() != null && queryJoin.getJoinMetaData().getName().equals(joinName))
         {
            return (false);
         }
      }

      return (true);
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
    ** hasTable("order") => true
    ** hasTable("li") => false
    ** hasTable("line_item") => true
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
         log("Evaluating filterTable", logPair("filterTable", filterTable));
         if(!aliasToTableNameMap.containsKey(filterTable) && !Objects.equals(mainTableName, filterTable))
         {
            log("- table not in query - adding it", logPair("filterTable", filterTable));
            boolean found = false;
            for(QJoinMetaData join : CollectionUtils.nonNullMap(QContext.getQInstance().getJoins()).values())
            {
               QueryJoin queryJoin = makeQueryJoinFromJoinAndTableNames(mainTableName, filterTable, join);
               if(queryJoin != null)
               {
                  this.addQueryJoin(queryJoin, "forFilter (join found in instance)");
                  found = true;
                  break;
               }
            }

            if(!found)
            {
               QueryJoin queryJoin = new QueryJoin().withJoinTable(filterTable).withType(QueryJoin.Type.INNER);
               this.addQueryJoin(queryJoin, "forFilter (join not found in instance)");
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryJoin makeQueryJoinFromJoinAndTableNames(String tableA, String tableB, QJoinMetaData join)
   {
      QueryJoin queryJoin = null;
      if(join.getLeftTable().equals(tableA) && join.getRightTable().equals(tableB))
      {
         queryJoin = new QueryJoin().withJoinMetaData(join).withType(QueryJoin.Type.INNER);
      }
      else
      {
         join = join.flip();
         if(join.getLeftTable().equals(tableA) && join.getRightTable().equals(tableB))
         {
            queryJoin = new QueryJoin().withJoinMetaData(join).withType(QueryJoin.Type.INNER);
         }
      }
      return queryJoin;
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



   /*******************************************************************************
    **
    *******************************************************************************/
   private void log(String message, LogPair... logPairs)
   {
      LOG.log(logLevel, message, null, logPairs);
   }

}
