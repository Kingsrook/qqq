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


import java.io.Serializable;
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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.NullValueBehaviorUtil;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLockFilters;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
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

   private final QQueryFilter securityFilter;

   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // pointer either at securityFilter, or at a sub-filter within it, for when we're doing a recursive build-out of multi-locks //
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private QQueryFilter securityFilterCursor;

   ////////////////////////////////////////////////////////////////
   // note - will have entries for all tables, not just aliases. //
   ////////////////////////////////////////////////////////////////
   private final Map<String, String> aliasToTableNameMap = new HashMap<>();

   ////////////////////////////////////////////////////////////////////////////////////////
   // option (see constructor that takes it) to not expand the filter for security locks //
   ////////////////////////////////////////////////////////////////////////////////////////
   private boolean omitSecurity = false;

   /////////////////////////////////////////////////////////////////////////////
   // we will get a TON of more output if this gets turned up, so be cautious //
   /////////////////////////////////////////////////////////////////////////////
   private Level logLevel          = Level.OFF;
   private Level logLevelForFilter = Level.OFF;



   /*******************************************************************************
    ** Constructor - same as original, but assumes the QInstance from QContext.
    **
    *******************************************************************************/
   public JoinsContext(String tableName, List<QueryJoin> queryJoins, QQueryFilter filter) throws QException
   {
      this(QContext.getQInstance(), tableName, queryJoins, filter);
   }



   /*******************************************************************************
    * Constructor - original.
    *
    *******************************************************************************/
   public JoinsContext(QInstance instance, String tableName, List<QueryJoin> queryJoins, QQueryFilter filter) throws QException
   {
      this.instance = instance;
      this.mainTableName = tableName;
      this.queryJoins = new MutableList<>(queryJoins);
      this.securityFilter = new QQueryFilter();
      this.securityFilterCursor = this.securityFilter;

      init(instance, tableName, filter);
   }



   /*******************************************************************************
    * Constructor - allows you to omit security, and doesn't pre-assume any
    * query joins.
    *
    *******************************************************************************/
   public JoinsContext(String tableName, QQueryFilter filter, boolean omitSecurity) throws QException
   {
      this.instance = QContext.getQInstance();
      this.mainTableName = tableName;
      this.queryJoins = new ArrayList<>();
      this.omitSecurity = omitSecurity;
      this.securityFilter = new QQueryFilter();
      this.securityFilterCursor = this.securityFilter;

      init(instance, tableName, filter);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void init(QInstance instance, String tableName, QQueryFilter filter) throws QException
   {
      // log("--- START ----------------------------------------------------------------------", logPair("mainTable", tableName));
      dumpDebug(true, false);

      for(QueryJoin queryJoin : this.queryJoins)
      {
         processQueryJoin(queryJoin);
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // make sure that all tables specified in filter columns are being brought into the query as joins //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      ensureFilterIsRepresented(filter);
      logFilter("After ensureFilterIsRepresented:", securityFilter);

      ///////////////////////////////////////////////////////////////////////////////////////
      // ensure that any record locks on the main table, which require a join, are present //
      ///////////////////////////////////////////////////////////////////////////////////////
      if(!omitSecurity)
      {
         MultiRecordSecurityLock multiRecordSecurityLock = RecordSecurityLockFilters.filterForReadLockTree(CollectionUtils.nonNullList(instance.getTable(tableName).getRecordSecurityLocks()));
         for(RecordSecurityLock lock : multiRecordSecurityLock.getLocks())
         {
            ensureRecordSecurityLockIsRepresented(tableName, tableName, lock, null);
            logFilter("After ensureRecordSecurityLockIsRepresented[fieldName=" + lock.getFieldName() + "]:", securityFilter);
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////
      // make sure that all joins in the query have meta data specified                //
      // e.g., a user-added join may just specify the join-table                       //
      // or a join implicitly added from a filter may also not have its join meta data //
      ///////////////////////////////////////////////////////////////////////////////////
      fillInMissingJoinMetaData();
      logFilter("After fillInMissingJoinMetaData:", securityFilter);

      ///////////////////////////////////////////////////////////////
      // ensure any joins that contribute a recordLock are present //
      ///////////////////////////////////////////////////////////////
      if(!omitSecurity)
      {
         ensureAllJoinRecordSecurityLocksAreRepresented(instance);
         logFilter("After ensureAllJoinRecordSecurityLocksAreRepresented:", securityFilter);

         ////////////////////////////////////////////////////////////////////////////////////
         // if there were any security filters built, then put those into the input filter //
         ////////////////////////////////////////////////////////////////////////////////////
         addSecurityFiltersToInputFilter(filter);
      }

      log("Constructed JoinsContext", logPair("mainTableName", this.mainTableName), logPair("queryJoins", this.queryJoins.stream().map(qj -> qj.getJoinTable()).collect(Collectors.joining(","))));
      log("", logPair("securityFilter", securityFilter));
      log("", logPair("fullFilter", filter));
      dumpDebug(false, true);
      // log("--- END ------------------------------------------------------------------------");
   }



   /*******************************************************************************
    ** Update the input filter with any security filters that were built.
    *******************************************************************************/
   private void addSecurityFiltersToInputFilter(QQueryFilter filter)
   {
      ////////////////////////////////////////////////////////////////////////////////////
      // if there's no security filter criteria (including sub-filters), return w/ noop //
      ////////////////////////////////////////////////////////////////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(securityFilter.getSubFilters()))
      {
         return;
      }

      ///////////////////////////////////////////////////////////////////////
      // if the input filter is an OR we need to replace it with a new AND //
      ///////////////////////////////////////////////////////////////////////
      if(filter.getBooleanOperator().equals(QQueryFilter.BooleanOperator.OR))
      {
         List<QFilterCriteria> originalCriteria   = filter.getCriteria();
         List<QQueryFilter>    originalSubFilters = filter.getSubFilters();

         QQueryFilter replacementFilter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR);
         replacementFilter.setCriteria(originalCriteria);
         replacementFilter.setSubFilters(originalSubFilters);

         filter.setCriteria(new ArrayList<>());
         filter.setSubFilters(new ArrayList<>());
         filter.setBooleanOperator(QQueryFilter.BooleanOperator.AND);
         filter.addSubFilter(replacementFilter);
      }

      filter.addSubFilter(securityFilter);
   }



   /*******************************************************************************
    ** In case we've added any joins to the query that have security locks which
    ** weren't previously added to the query, add them now.  basically, this is
    ** calling ensureRecordSecurityLockIsRepresented for each queryJoin.
    *******************************************************************************/
   private void ensureAllJoinRecordSecurityLocksAreRepresented(QInstance instance) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // avoid concurrent modification exceptions by doing a double-loop and breaking the inner any time anything gets added //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      Set<QueryJoin> processedQueryJoins   = new HashSet<>();
      boolean        addedAnyThisIteration = true;
      while(addedAnyThisIteration)
      {
         addedAnyThisIteration = false;

         for(QueryJoin queryJoin : this.queryJoins)
         {
            boolean addedAnyForThisJoin = false;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // avoid double-processing the same query join                                                                                            //
            // or adding security filters for a join who was only added to the query so that we could add locks (an ImplicitQueryJoinForSecurityLock) //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(processedQueryJoins.contains(queryJoin) || queryJoin instanceof ImplicitQueryJoinForSecurityLock)
            {
               continue;
            }
            processedQueryJoins.add(queryJoin);

            //////////////////////////////////////////////////////////////////////////////////////////
            // process all locks on this join's join-table.  keep track if any new joins were added //
            //////////////////////////////////////////////////////////////////////////////////////////
            QTableMetaData joinTable = instance.getTable(queryJoin.getJoinTable());

            MultiRecordSecurityLock multiRecordSecurityLock = RecordSecurityLockFilters.filterForReadLockTree(CollectionUtils.nonNullList(joinTable.getRecordSecurityLocks()));
            for(RecordSecurityLock lock : multiRecordSecurityLock.getLocks())
            {
               List<QueryJoin> addedQueryJoins = ensureRecordSecurityLockIsRepresented(joinTable.getName(), queryJoin.getJoinTableOrItsAlias(), lock, queryJoin);

               //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // if any joins were added by this call, add them to the set of processed ones, so they don't get re-processed. //
               // also mark the flag that any were added for this join, to manage the double-looping                           //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               if(CollectionUtils.nullSafeHasContents(addedQueryJoins))
               {
                  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // make all new joins added in that method be of the same type (inner/left/etc) as the query join they are connected to //
                  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  for(QueryJoin addedQueryJoin : addedQueryJoins)
                  {
                     addedQueryJoin.setType(queryJoin.getType());
                  }

                  processedQueryJoins.addAll(addedQueryJoins);
                  addedAnyForThisJoin = true;
               }
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // if any new joins were added, we need to break the inner-loop, and continue the outer loop //
            // e.g., to process the next query join (but we can't just go back to the foreach queryJoin, //
            // because it would fail with concurrent modification)                                       //
            ///////////////////////////////////////////////////////////////////////////////////////////////
            if(addedAnyForThisJoin)
            {
               addedAnyThisIteration = true;
               break;
            }
         }
      }
   }



   /*******************************************************************************
    ** For a given recordSecurityLock on a given table (with a possible alias),
    ** make sure that if any joins are needed to get to the lock, that they are in the query.
    **
    ** returns the list of query joins that were added, if any were added
    *******************************************************************************/
   private List<QueryJoin> ensureRecordSecurityLockIsRepresented(String tableName, String tableNameOrAlias, RecordSecurityLock recordSecurityLock, QueryJoin sourceQueryJoin) throws QException
   {
      List<QueryJoin> addedQueryJoins = new ArrayList<>();

      ////////////////////////////////////////////////////////////////////////////
      // if this lock is a multi-lock, then recursively process its child-locks //
      ////////////////////////////////////////////////////////////////////////////
      if(recordSecurityLock instanceof MultiRecordSecurityLock multiRecordSecurityLock)
      {
         log("Processing MultiRecordSecurityLock...");

         /////////////////////////////////////////////////////////////////////////////////////////////////////////
         // make a new level in the filter-tree - storing old cursor, and updating cursor to point at new level //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////
         QQueryFilter oldSecurityFilterCursor = this.securityFilterCursor;
         QQueryFilter nextLevelSecurityFilter = new QQueryFilter();
         this.securityFilterCursor.addSubFilter(nextLevelSecurityFilter);
         this.securityFilterCursor = nextLevelSecurityFilter;

         ///////////////////////////////////////
         // set the boolean operator to match //
         ///////////////////////////////////////
         nextLevelSecurityFilter.setBooleanOperator(multiRecordSecurityLock.getOperator().toFilterOperator());

         //////////////////////
         // process children //
         //////////////////////
         for(RecordSecurityLock childLock : CollectionUtils.nonNullList(multiRecordSecurityLock.getLocks()))
         {
            log(" - Recursive call for childLock: " + childLock);
            addedQueryJoins.addAll(ensureRecordSecurityLockIsRepresented(tableName, tableNameOrAlias, childLock, sourceQueryJoin));
         }

         ////////////////////
         // restore cursor //
         ////////////////////
         this.securityFilterCursor = oldSecurityFilterCursor;

         return addedQueryJoins;
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // A join name chain is going to look like this:                                                 //
      // for a table:  orderLineItemExtrinsic (that's 2 away from order, where its security field is): //
      // - securityFieldName = order.clientId                                                          //
      // - joinNameChain = orderJoinOrderLineItem, orderLineItemJoinOrderLineItemExtrinsic             //
      // so - to navigate from the table to the security field, we need to reverse the joinNameChain,  //
      // and step (via tmpTable variable) back to the securityField                                    //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      ArrayList<String> joinNameChain = new ArrayList<>(CollectionUtils.nonNullList(recordSecurityLock.getJoinNameChain()));
      Collections.reverse(joinNameChain);
      log("Evaluating recordSecurityLock.  Join name chain is of length: " + joinNameChain.size(), logPair("tableNameOrAlias", tableNameOrAlias), logPair("recordSecurityLock", recordSecurityLock.getFieldName()), logPair("joinNameChain", joinNameChain));

      QTableMetaData tmpTable                = instance.getTable(tableName);
      String         securityFieldTableAlias = tableNameOrAlias;
      String         baseTableOrAlias        = tableNameOrAlias;

      boolean chainIsInner = true;
      if(sourceQueryJoin != null && QueryJoin.Type.isOuter(sourceQueryJoin.getType()))
      {
         chainIsInner = false;
      }

      for(String joinName : joinNameChain)
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////
         // check the joins currently in the query - if any are THIS join, then we don't need to add one //
         //////////////////////////////////////////////////////////////////////////////////////////////////
         List<QueryJoin> matchingQueryJoins = this.queryJoins.stream().filter(queryJoin ->
         {
            QJoinMetaData joinMetaData = queryJoin.getJoinMetaData();
            return (joinMetaData != null && Objects.equals(joinMetaData.getName(), joinName));
         }).toList();

         if(CollectionUtils.nullSafeHasContents(matchingQueryJoins))
         {
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // note - if a user added a join as an outer type, we need to change it to be inner, for the security purpose. //
            // todo - is this always right?  what about nulls-allowed?                                                     //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            log("- skipping join already in the query", logPair("joinName", joinName));

            QueryJoin matchedQueryJoin = matchingQueryJoins.get(0);

            if(matchedQueryJoin.getType().equals(QueryJoin.Type.LEFT) || matchedQueryJoin.getType().equals(QueryJoin.Type.RIGHT))
            {
               chainIsInner = false;
            }

            /* ?? todo ??
            if(matchedQueryJoin.getType().equals(QueryJoin.Type.LEFT) || matchedQueryJoin.getType().equals(QueryJoin.Type.RIGHT))
            {
               log("- - although... it was here as an outer - so switching it to INNER", logPair("joinName", joinName));
               matchedQueryJoin.setType(QueryJoin.Type.INNER);
            }
            */

            //////////////////////////////////////////////////////////////////////////////////////////////////////
            // as we're walking from tmpTable to the table which ultimately has the security key field,         //
            // if the queryJoin we just found is joining out to tmpTable, then we need to advance tmpTable back //
            // to the queryJoin's base table - else, tmpTable advances to the matched queryJoin's joinTable     //
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            if(tmpTable.getName().equals(matchedQueryJoin.getJoinTable()))
            {
               securityFieldTableAlias = Objects.requireNonNullElse(matchedQueryJoin.getBaseTableOrAlias(), mainTableName);
            }
            else
            {
               securityFieldTableAlias = matchedQueryJoin.getJoinTableOrItsAlias();
            }
            tmpTable = instance.getTable(aliasToTableNameMap.getOrDefault(securityFieldTableAlias, securityFieldTableAlias));

            ////////////////////////////////////////////////////////////////////////////////////////
            // set the baseTableOrAlias for the next iteration to be this join's joinTableOrAlias //
            ////////////////////////////////////////////////////////////////////////////////////////
            baseTableOrAlias = securityFieldTableAlias;

            continue;
         }

         QJoinMetaData join = instance.getJoin(joinName);
         if(join.getLeftTable().equals(tmpTable.getName()))
         {
            securityFieldTableAlias = join.getRightTable() + "_forSecurityJoin_" + join.getName();
            QueryJoin queryJoin = new ImplicitQueryJoinForSecurityLock()
               .withJoinMetaData(join)
               .withType(chainIsInner ? QueryJoin.Type.INNER : QueryJoin.Type.LEFT)
               .withBaseTableOrAlias(baseTableOrAlias)
               .withAlias(securityFieldTableAlias);

            if(securityFilterCursor.getBooleanOperator() == QQueryFilter.BooleanOperator.OR)
            {
               queryJoin.withType(QueryJoin.Type.LEFT);
               chainIsInner = false;
            }

            if(hasAllAccessKey(recordSecurityLock))
            {
               queryJoin.withType(QueryJoin.Type.LEFT);
               chainIsInner = false;
            }

            addQueryJoin(queryJoin, "forRecordSecurityLock (non-flipped)", "- ");
            addedQueryJoins.add(queryJoin);
            tmpTable = instance.getTable(join.getRightTable());
         }
         else if(join.getRightTable().equals(tmpTable.getName()))
         {
            securityFieldTableAlias = join.getLeftTable() + "_forSecurityJoin_" + join.getName();
            QueryJoin queryJoin = new ImplicitQueryJoinForSecurityLock()
               .withJoinMetaData(join.flip())
               .withType(chainIsInner ? QueryJoin.Type.INNER : QueryJoin.Type.LEFT)
               .withBaseTableOrAlias(baseTableOrAlias)
               .withAlias(securityFieldTableAlias);

            if(securityFilterCursor.getBooleanOperator() == QQueryFilter.BooleanOperator.OR)
            {
               queryJoin.withType(QueryJoin.Type.LEFT);
               chainIsInner = false;
            }

            if(hasAllAccessKey(recordSecurityLock))
            {
               queryJoin.withType(QueryJoin.Type.LEFT);
               chainIsInner = false;
            }

            addQueryJoin(queryJoin, "forRecordSecurityLock (flipped)", "- ");
            addedQueryJoins.add(queryJoin);
            tmpTable = instance.getTable(join.getLeftTable());
         }
         else
         {
            dumpDebug(false, true);
            throw (new QException("Error adding security lock joins to query - table name [" + tmpTable.getName() + "] not found in join [" + joinName + "]"));
         }

         //////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // for the next iteration of the loop, set the next join's baseTableOrAlias to be the alias we just created //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////
         baseTableOrAlias = securityFieldTableAlias;
      }

      ////////////////////////////////////////////////////////////////////////////////////
      // now that we know the joins/tables are in the query, add to the security filter //
      ////////////////////////////////////////////////////////////////////////////////////
      QueryJoin lastAddedQueryJoin = addedQueryJoins.isEmpty() ? null : addedQueryJoins.get(addedQueryJoins.size() - 1);
      if(sourceQueryJoin != null && lastAddedQueryJoin == null)
      {
         lastAddedQueryJoin = sourceQueryJoin;
      }
      addSubFilterForRecordSecurityLock(recordSecurityLock, tmpTable, securityFieldTableAlias, !chainIsInner, lastAddedQueryJoin);

      log("Finished evaluating recordSecurityLock");

      return (addedQueryJoins);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private boolean hasAllAccessKey(RecordSecurityLock recordSecurityLock)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check if the key type has an all-access key, and if so, if it's set to true for the current user/session //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QSecurityKeyType securityKeyType = instance.getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
      if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()))
      {
         QSession session = QContext.getQSession();
         if(session.hasSecurityKeyValue(securityKeyType.getAllAccessKeyName(), true, QFieldType.BOOLEAN))
         {
            return (true);
         }
      }

      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addSubFilterForRecordSecurityLock(RecordSecurityLock recordSecurityLock, QTableMetaData table, String tableNameOrAlias, boolean isOuter, QueryJoin sourceQueryJoin)
   {
      boolean haveAllAccessKey = hasAllAccessKey(recordSecurityLock);
      if(haveAllAccessKey)
      {
         if(sourceQueryJoin != null)
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // in case the queryJoin object is re-used between queries, and its security criteria need to be different (!!), reset it //
            // this can be exposed in tests - maybe not entirely expected in real-world, but seems safe enough                        //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            sourceQueryJoin.withSecurityCriteria(new ArrayList<>());
         }

         ////////////////////////////////////////////////////////////////////////////////////////
         // if we're in an AND filter, then we don't need a criteria for this lock, so return. //
         ////////////////////////////////////////////////////////////////////////////////////////
         boolean inAnAndFilter = securityFilterCursor.getBooleanOperator() == QQueryFilter.BooleanOperator.AND;
         if(inAnAndFilter)
         {
            return;
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////
      // for locks w/o a join chain, the lock fieldName will simply be a field on the table. //
      // so just prepend that with the tableNameOrAlias.                                     //
      /////////////////////////////////////////////////////////////////////////////////////////
      String fieldName = tableNameOrAlias + "." + recordSecurityLock.getFieldName();
      if(CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()))
      {
         /////////////////////////////////////////////////////////////////////////////////
         // else, expect a "table.field" in the lock fieldName - but we want to replace //
         // the table name part with a possible alias that we took in.                  //
         /////////////////////////////////////////////////////////////////////////////////
         String[] parts = recordSecurityLock.getFieldName().split("\\.");
         if(parts.length != 2)
         {
            dumpDebug(false, true);
            throw new IllegalArgumentException("Mal-formatted recordSecurityLock fieldName for lock with joinNameChain in query: " + fieldName);
         }
         fieldName = tableNameOrAlias + "." + parts[1];
      }

      ///////////////////////////////////////////////////////////////////////////////////////////
      // else - get the key values from the session and decide what kind of criterion to build //
      ///////////////////////////////////////////////////////////////////////////////////////////
      QQueryFilter          lockFilter   = new QQueryFilter();
      List<QFilterCriteria> lockCriteria = new ArrayList<>();
      lockFilter.setCriteria(lockCriteria);

      QFieldType type = QFieldType.INTEGER;
      try
      {
         JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = getFieldAndTableNameOrAlias(fieldName);
         type = fieldAndTableNameOrAlias.field().getType();
      }
      catch(Exception e)
      {
         LOG.debug("Error getting field type...  Trying Integer", e);
      }

      if(haveAllAccessKey)
      {
         ////////////////////////////////////////////////////////////////////////////////////////////
         // if we have an all access key (but we got here because we're part of an OR query), then //
         // write a criterion that will always be true - e.g., field=field                         //
         ////////////////////////////////////////////////////////////////////////////////////////////
         lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.TRUE));
      }
      else
      {
         List<Serializable> securityKeyValues = QContext.getQSession().getSecurityKeyValues(recordSecurityLock.getSecurityKeyType(), type);
         if(CollectionUtils.nullSafeIsEmpty(securityKeyValues))
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // handle user with no values -- they can only see null values, and only iff the lock's null-value behavior is ALLOW //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(RecordSecurityLock.NullValueBehavior.ALLOW.equals(NullValueBehaviorUtil.getEffectiveNullValueBehavior(recordSecurityLock)))
            {
               lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IS_BLANK));
            }
            else
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // else, if no user/session values, and null-value behavior is deny, then setup a FALSE condition, to allow no rows.         //
               // todo - maybe avoid running the whole query - as you're not allowed ANY records (based on boolean tree down to this point) //
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.FALSE));
            }
         }
         else
         {
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // else, if user/session has some values, build an IN rule -                                                //
            // noting that if the lock's null-value behavior is ALLOW, then we actually want IS_NULL_OR_IN, not just IN //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(RecordSecurityLock.NullValueBehavior.ALLOW.equals(NullValueBehaviorUtil.getEffectiveNullValueBehavior(recordSecurityLock)))
            {
               lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IS_NULL_OR_IN, securityKeyValues));
            }
            else
            {
               lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IN, securityKeyValues));
            }
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if there's a sourceQueryJoin, then set the lockCriteria on that join - so it gets written into the JOIN ... ON clause //
      // ... unless we're writing an OR filter.  then we need the condition in the WHERE clause                                //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      boolean doNotPutCriteriaInJoinOn = securityFilterCursor.getBooleanOperator() == QQueryFilter.BooleanOperator.OR;
      if(sourceQueryJoin != null && !doNotPutCriteriaInJoinOn)
      {
         sourceQueryJoin.withSecurityCriteria(lockCriteria);
      }
      else
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // we used to add an OR IS NULL for cases of an outer-join - but instead, this is now handled by putting the lockCriteria //
         // into the join (see above) - so this check is probably deprecated.                                                      //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         /*
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if this field is on the outer side of an outer join, then if we do a straight filter on it, then we're basically      //
            // nullifying the outer join... so for an outer join use-case, OR the security field criteria with a primary-key IS NULL //
            // which will make missing rows from the join be found.                                                                  //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(isOuter)
            {
               if(table == null)
               {
                  table = QContext.getQInstance().getTable(aliasToTableNameMap.get(tableNameOrAlias));
               }

               lockFilter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
               lockFilter.addCriteria(new QFilterCriteria(tableNameOrAlias + "." + table.getPrimaryKeyField(), QCriteriaOperator.IS_BLANK));
            }
         */

         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // If this filter isn't for a queryJoin, then just add it to the main list of security sub-filters //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         this.securityFilterCursor.addSubFilter(lockFilter);
      }
   }



   /*******************************************************************************
    ** Add a query join to the list of query joins, and "process it"
    **
    ** use this method to add to the list, instead of ever adding directly, as it's
    ** important do to that process step (and we've had bugs when it wasn't done).
    *******************************************************************************/
   private void addQueryJoin(QueryJoin queryJoin, String reason, String logPrefix) throws QException
   {
      log(Objects.requireNonNullElse(logPrefix, "") + "Adding query join to context",
         logPair("reason", reason),
         logPair("joinTable", queryJoin.getJoinTable()),
         logPair("joinMetaData.name", () -> queryJoin.getJoinMetaData().getName()),
         logPair("joinMetaData.leftTable", () -> queryJoin.getJoinMetaData().getLeftTable()),
         logPair("joinMetaData.rightTable", () -> queryJoin.getJoinMetaData().getRightTable())
      );
      this.queryJoins.add(queryJoin);
      processQueryJoin(queryJoin);
      dumpDebug(false, false);
   }



   /*******************************************************************************
    ** If there are any joins in the context that don't have a join meta data, see
    ** if we can find the JoinMetaData to use for them by looking at all joins in the
    ** instance, or at the main table's exposed joins, and using their join paths.
    *******************************************************************************/
   private void fillInMissingJoinMetaData() throws QException
   {
      log("Begin adding missing join meta data");

      ////////////////////////////////////////////////////////////////////////////////
      // do a double-loop, to avoid concurrent modification on the queryJoins list. //
      // that is to say, we'll loop over that list, but possibly add things to it,  //
      // in which case we'll set this flag, and break the inner loop, to go again.  //
      ////////////////////////////////////////////////////////////////////////////////
      Set<QueryJoin> processedQueryJoins = new HashSet<>();
      boolean        addedJoin;
      do
      {
         addedJoin = false;
         for(QueryJoin queryJoin : queryJoins)
         {
            if(processedQueryJoins.contains(queryJoin))
            {
               continue;
            }
            processedQueryJoins.add(queryJoin);

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // if the join has joinMetaData, then we don't need to process it... unless it needs flipped //
            ///////////////////////////////////////////////////////////////////////////////////////////////
            QJoinMetaData joinMetaData = queryJoin.getJoinMetaData();
            if(joinMetaData != null)
            {
               log("- QueryJoin already has joinMetaData", logPair("joinMetaDataName", joinMetaData.getName()));

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
                  log("- - Flipping queryJoin because its leftTable wasn't found in the query", logPair("joinMetaDataName", joinMetaData.getName()), logPair("leftTable", joinMetaDataLeftTable));
                  queryJoin.setJoinMetaData(joinMetaData.flip());
               }
            }
            else
            {
               //////////////////////////////////////////////////////////////////////
               // try to find a direct join between the main table and this table. //
               // if one is found, then put it (the meta data) on the query join.  //
               //////////////////////////////////////////////////////////////////////
               log("- QueryJoin doesn't have metaData - looking for it", logPair("joinTableOrItsAlias", queryJoin.getJoinTableOrItsAlias()));

               String        baseTableName = Objects.requireNonNullElse(resolveTableNameOrAliasToTableName(queryJoin.getBaseTableOrAlias()), mainTableName);
               QJoinMetaData found         = findJoinMetaData(baseTableName, queryJoin.getJoinTable(), true);
               if(found != null)
               {
                  log("- - Found joinMetaData - setting it in queryJoin", logPair("joinMetaDataName", found.getName()), logPair("baseTableName", baseTableName), logPair("joinTable", queryJoin.getJoinTable()));
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
                        log("- - Found an exposed join", logPair("mainTable", mainTableName), logPair("joinTable", queryJoin.getJoinTable()), logPair("joinPath", exposedJoin.getJoinPath()));

                        /////////////////////////////////////////////////////////////////////////////////////
                        // loop backward through the join path (from the joinTable back to the main table) //
                        // adding joins to the table (if they aren't already in the query)                 //
                        /////////////////////////////////////////////////////////////////////////////////////
                        String tmpTable = queryJoin.getJoinTable();
                        for(int i = exposedJoin.getJoinPath().size() - 1; i >= 0; i--)
                        {
                           String        joinName  = exposedJoin.getJoinPath().get(i);
                           QJoinMetaData joinToAdd = instance.getJoin(joinName);
                           log("- - - evaluating joinPath element", logPair("i", i), logPair("joinName", joinName));

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
                                 log("- - - - this is the last element in the join path, so setting this joinMetaData on the original queryJoin");
                              }
                              else
                              {
                                 QueryJoin queryJoinToAdd = makeQueryJoinFromJoinAndTableNames(nextTable, tmpTable, joinToAdd);
                                 queryJoinToAdd.setType(queryJoin.getType());
                                 addedAnyQueryJoins = true;
                                 log("- - - - this is not the last element in the join path, so adding a new query join:");
                                 addQueryJoin(queryJoinToAdd, "forExposedJoin", "- - - - - - ");
                                 dumpDebug(false, false);
                              }
                           }
                           else
                           {
                              log("- - - - join doesn't need added to the query");
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

      log("Done adding missing join meta data");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean doesJoinNeedAddedToQuery(String joinName)
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // look at all queryJoins already in context - if any have this join's name, and aren't implicit-security joins, then we don't need this join... //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(QueryJoin queryJoin : queryJoins)
      {
         if(queryJoin.getJoinMetaData() != null && queryJoin.getJoinMetaData().getName().equals(joinName) && !(queryJoin instanceof ImplicitQueryJoinForSecurityLock))
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
      QTableMetaData joinTable = QContext.getQInstance().getTable(queryJoin.getJoinTable());
      if(joinTable == null)
      {
         throw (new QException("Unrecognized name for join table: " + queryJoin.getJoinTable()));
      }

      String tableNameOrAlias = queryJoin.getJoinTableOrItsAlias();
      if(aliasToTableNameMap.containsKey(tableNameOrAlias))
      {
         dumpDebug(false, true);
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
            dumpDebug(false, true);
            throw new IllegalArgumentException("Mal-formatted field name in query: " + fieldName);
         }

         String tableOrAlias  = parts[0];
         String baseFieldName = parts[1];
         String tableName     = resolveTableNameOrAliasToTableName(tableOrAlias);

         QTableMetaData table = instance.getTable(tableName);
         if(table == null)
         {
            dumpDebug(false, true);
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
         log("Evaluating filter", logPair("filterTable", filterTable));
         if(!aliasToTableNameMap.containsKey(filterTable) && !Objects.equals(mainTableName, filterTable))
         {
            log("- table not in query - adding a join for it", logPair("filterTable", filterTable));
            boolean found = false;
            for(QJoinMetaData join : CollectionUtils.nonNullMap(QContext.getQInstance().getJoins()).values())
            {
               QueryJoin queryJoin = makeQueryJoinFromJoinAndTableNames(mainTableName, filterTable, join);
               if(queryJoin != null)
               {
                  addQueryJoin(queryJoin, "forFilter (join found in instance)", "- - ");
                  found = true;
                  break;
               }
            }

            if(!found)
            {
               QueryJoin queryJoin = new QueryJoin().withJoinTable(filterTable).withType(QueryJoin.Type.INNER);
               addQueryJoin(queryJoin, "forFilter (join not found in instance)", "- - ");
            }
         }
         else
         {
            log("- table is already in query - not adding any joins", logPair("filterTable", filterTable));
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

         for(QFilterOrderBy orderBy : CollectionUtils.nonNullList(filter.getOrderBys()))
         {
            getTableNameFromFieldNameAndAddToSet(orderBy.getFieldName(), filterTables);
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
   public QJoinMetaData findJoinMetaData(String baseTableName, String joinTableName, boolean useExposedJoins)
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
         ////////////////////////////////////////////////////////////////////////////////
         // if we found more than one join, but we're allowed to useExposedJoins, then //
         // see if we can tell which match to used based on the table's exposed joins  //
         ////////////////////////////////////////////////////////////////////////////////
         if(useExposedJoins)
         {
            QTableMetaData mainTable = QContext.getQInstance().getTable(mainTableName);
            for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(mainTable.getExposedJoins()))
            {
               if(exposedJoin.getJoinTable().equals(joinTableName))
               {
                  // todo ... is it wrong to always use 0??
                  return instance.getJoin(exposedJoin.getJoinPath().get(0));
               }
            }
         }

         ///////////////////////////////////////////////
         // if we couldn't figure it out, then throw. //
         ///////////////////////////////////////////////
         dumpDebug(false, true);
         throw (new RuntimeException("More than 1 join was found between [" + baseTableName + "] and [" + joinTableName + "] "
            + (useExposedJoins ? "(and exposed joins didn't clarify which one to use). " : "") + "Specify which one in your QueryJoin."));
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



   /*******************************************************************************
    **
    *******************************************************************************/
   private void logFilter(String message, QQueryFilter filter)
   {
      if(logLevelForFilter.equals(Level.OFF))
      {
         return;
      }
      System.out.println(message + "\n" + filter);
   }



   /*******************************************************************************
    ** Print (to stdout, for easier reading) the object in a big table format for
    ** debugging.  Happens any time logLevel is > OFF.  Not meant for loggly.
    *******************************************************************************/
   private void dumpDebug(boolean isStart, boolean isEnd)
   {
      if(logLevel.equals(Level.OFF))
      {
         return;
      }

      int sm       = 8;
      int md       = 30;
      int lg       = 50;
      int overhead = 14;
      int full     = sm + 3 * md + lg + overhead;

      if(isStart)
      {
         System.out.println("\n" + StringUtils.safeTruncate("--- Start [main table: " + this.mainTableName + "] " + "-".repeat(full), full));
      }

      StringBuilder rs           = new StringBuilder();
      String        formatString = "| %-" + md + "s | %-" + md + "s %-" + md + "s | %-" + lg + "s | %-" + sm + "s |\n";
      rs.append(String.format(formatString, "Base Table", "Join Table", "(Alias)", "Join Meta Data", "Type"));
      String dashesLg = "-".repeat(lg);
      String dashesMd = "-".repeat(md);
      String dashesSm = "-".repeat(sm);
      rs.append(String.format(formatString, dashesMd, dashesMd, dashesMd, dashesLg, dashesSm));
      if(CollectionUtils.nullSafeHasContents(queryJoins))
      {
         for(QueryJoin queryJoin : queryJoins)
         {
            rs.append(String.format(
               formatString,
               StringUtils.hasContent(queryJoin.getBaseTableOrAlias()) ? StringUtils.safeTruncate(queryJoin.getBaseTableOrAlias(), md) : "--",
               StringUtils.safeTruncate(queryJoin.getJoinTable(), md),
               (StringUtils.hasContent(queryJoin.getAlias()) ? "(" + StringUtils.safeTruncate(queryJoin.getAlias(), md - 2) + ")" : ""),
               queryJoin.getJoinMetaData() == null ? "--" : StringUtils.safeTruncate(queryJoin.getJoinMetaData().getName(), lg),
               queryJoin.getType()));
         }
      }
      else
      {
         rs.append(String.format(formatString, "-empty-", "", "", "", ""));
      }

      System.out.print(rs);

      System.out.println(securityFilter);

      if(isEnd)
      {
         System.out.println(StringUtils.safeTruncate("--- End " + "-".repeat(full), full) + "\n");
      }
      else
      {
         System.out.println(StringUtils.safeTruncate("-".repeat(full), full));
      }
   }
}
