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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.memory;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.DateTimeGroupBy;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ValidateRecordSecurityLockHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByAggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByGroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.utils.BackendQueryFilterUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Storage provider for the MemoryBackendModule
 *******************************************************************************/
public class MemoryRecordStore
{
   private static final QLogger LOG = QLogger.getLogger(MemoryRecordStore.class);

   private static MemoryRecordStore instance;

   private Map<String, Map<Serializable, QRecord>> data;
   private Map<String, Integer>                    nextSerials;

   private static boolean collectStatistics = false;

   public static final String STAT_QUERIES_RAN = "queriesRan";
   public static final String STAT_INSERTS_RAN = "insertsRan";

   private static final Map<String, Integer> statistics = Collections.synchronizedMap(new HashMap<>());

   public static final ListingHash<Class<? extends AbstractActionInput>, AbstractActionInput> actionInputs = new ListingHash<>();



   /*******************************************************************************
    ** private singleton constructor
    *******************************************************************************/
   private MemoryRecordStore()
   {
      data = new HashMap<>();
      nextSerials = new HashMap<>();
   }



   /*******************************************************************************
    ** forget all data AND statistics
    *******************************************************************************/
   public static void fullReset()
   {
      getInstance().reset();
      resetStatistics();
      setCollectStatistics(false);
   }



   /*******************************************************************************
    ** Forget all data in the memory store...
    *******************************************************************************/
   public void reset()
   {
      data.clear();
      nextSerials.clear();
   }



   /*******************************************************************************
    ** singleton accessor
    *******************************************************************************/
   public static MemoryRecordStore getInstance()
   {
      if(instance == null)
      {
         instance = new MemoryRecordStore();
      }
      return (instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<Serializable, QRecord> getTableData(QTableMetaData table)
   {
      if(!data.containsKey(table.getName()))
      {
         data.put(table.getName(), new HashMap<>());
      }
      return (data.get(table.getName()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> query(QueryInput input) throws QException
   {
      incrementStatistic(input);

      Collection<QRecord> tableData = getTableData(input.getTable()).values();
      List<QRecord>       records   = new ArrayList<>();

      QQueryFilter filter       = clonedOrNewFilter(input.getFilter());
      JoinsContext joinsContext = new JoinsContext(QContext.getQInstance(), input.getTableName(), input.getQueryJoins(), filter);
      if(CollectionUtils.nullSafeHasContents(input.getQueryJoins()))
      {
         tableData = buildJoinCrossProduct(input);
      }

      for(QRecord qRecord : tableData)
      {
         if(qRecord.getTableName() == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////
            // internally, doesRecordMatch likes to know table names on records, so, set if missing. //
            ///////////////////////////////////////////////////////////////////////////////////////////
            qRecord.setTableName(input.getTableName());
         }

         boolean recordMatches = BackendQueryFilterUtils.doesRecordMatch(input.getFilter(), joinsContext, qRecord);

         if(recordMatches)
         {
            qRecord.setErrors(new ArrayList<>());
            ValidateRecordSecurityLockHelper.validateSecurityFields(input.getTable(), List.of(qRecord), ValidateRecordSecurityLockHelper.Action.SELECT);
            if(CollectionUtils.nullSafeHasContents(qRecord.getErrors()))
            {
               //////////////////////////////////////////////////////////////////////////////////////////////////////
               // security error!  no record for you.  but remove the error, so future generations won't see it... //
               //////////////////////////////////////////////////////////////////////////////////////////////////////
               qRecord.setErrors(new ArrayList<>());
               LOG.trace("Error selecting record (presumably security?): " + qRecord.getErrors());
            }
            else
            {
               //////////////////////////////////////////////////////////////////////////////////
               // make sure we're not giving back records that are all full of associations... //
               //////////////////////////////////////////////////////////////////////////////////
               QRecord recordToReturn = new QRecord(qRecord);
               recordToReturn.setAssociatedRecords(new HashMap<>());
               records.add(recordToReturn);
            }
         }
      }

      BackendQueryFilterUtils.sortRecordList(input.getFilter(), records);
      records = BackendQueryFilterUtils.applySkipAndLimit(input.getFilter(), records);

      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Collection<QRecord> buildJoinCrossProduct(QueryInput input) throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      List<QRecord>  crossProduct = new ArrayList<>();
      QTableMetaData leftTable    = input.getTable();
      for(QRecord record : getTableData(leftTable).values())
      {
         QRecord productRecord = new QRecord();
         addRecordToProduct(productRecord, record, null);
         crossProduct.add(productRecord);
      }

      for(QueryJoin queryJoin : input.getQueryJoins())
      {
         QTableMetaData      nextTable        = qInstance.getTable(queryJoin.getJoinTable());
         Collection<QRecord> nextTableRecords = getTableData(nextTable).values();
         QJoinMetaData       joinMetaData     = Objects.requireNonNull(queryJoin.getJoinMetaData(), () -> "Could not find a join between tables [" + leftTable + "][" + queryJoin.getJoinTable() + "]");

         List<QRecord> nextLevelProduct = new ArrayList<>();
         for(QRecord productRecord : crossProduct)
         {
            boolean matchFound = false;
            for(QRecord nextTableRecord : nextTableRecords)
            {
               if(joinMatches(productRecord, nextTableRecord, queryJoin, joinMetaData))
               {
                  QRecord joinRecord = new QRecord(productRecord);
                  addRecordToProduct(joinRecord, nextTableRecord, queryJoin.getJoinTableOrItsAlias());
                  nextLevelProduct.add(joinRecord);
                  matchFound = true;
               }
            }

            if(!matchFound)
            {
               // todo - Left & Right joins
            }
         }

         crossProduct = nextLevelProduct;
      }

      return (crossProduct);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean joinMatches(QRecord productRecord, QRecord nextTableRecord, QueryJoin queryJoin, QJoinMetaData joinMetaData)
   {
      for(JoinOn joinOn : joinMetaData.getJoinOns())
      {
         Serializable leftValue = productRecord.getValues().containsKey(queryJoin.getBaseTableOrAlias() + "." + joinOn.getLeftField())
            ? productRecord.getValue(queryJoin.getBaseTableOrAlias() + "." + joinOn.getLeftField())
            : productRecord.getValue(joinOn.getLeftField());
         Serializable rightValue = nextTableRecord.getValue(joinOn.getRightField());
         if(!Objects.equals(leftValue, rightValue))
         {
            return (false);
         }
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordToProduct(QRecord productRecord, QRecord record, String tableNameOrAlias)
   {
      for(Map.Entry<String, Serializable> entry : record.getValues().entrySet())
      {
         productRecord.withValue(tableNameOrAlias == null ? entry.getKey() : tableNameOrAlias + "." + entry.getKey(), entry.getValue());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer count(CountInput input) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(input.getTableName());
      if(input.getFilter() != null)
      {
         queryInput.setFilter(input.getFilter().clone().withSkip(null).withLimit(null));
      }
      List<QRecord> queryResult = query(queryInput);

      return (queryResult.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> insert(InsertInput input, boolean returnInsertedRecords)
   {
      incrementStatistic(input);

      if(input.getRecords() == null)
      {
         return (new ArrayList<>());
      }

      QTableMetaData             table     = input.getTable();
      Map<Serializable, QRecord> tableData = getTableData(table);

      ////////////////////////////////////////
      // grab the next unique serial to use //
      ////////////////////////////////////////
      Integer nextSerial = nextSerials.get(table.getName());
      if(nextSerial == null)
      {
         nextSerial = 1;
      }

      while(tableData.containsKey(nextSerial))
      {
         nextSerial++;
      }

      List<QRecord>  outputRecords   = new ArrayList<>();
      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());
      for(QRecord record : input.getRecords())
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // make a copy of the record, to be inserted, and returned. this can avoid some cases where the in-memory store acts      //
         // differently from other backends, because of having the same record variable in the backend store and in the user-code. //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         QRecord recordToInsert = new QRecord(record);
         if(CollectionUtils.nullSafeHasContents(recordToInsert.getErrors()))
         {
            outputRecords.add(recordToInsert);
            continue;
         }

         /////////////////////////////////////////////////
         // set the next serial in the record if needed //
         /////////////////////////////////////////////////
         if(recordToInsert.getValue(primaryKeyField.getName()) == null && (primaryKeyField.getType().equals(QFieldType.INTEGER) || primaryKeyField.getType().equals(QFieldType.LONG)))
         {
            recordToInsert.setValue(primaryKeyField.getName(), nextSerial++);
         }

         ///////////////////////////////////////////////////////////////////////////////////////////////////
         // make sure that if the user supplied a serial, greater than the one we had, that we skip ahead //
         ///////////////////////////////////////////////////////////////////////////////////////////////////
         if(primaryKeyField.getType().equals(QFieldType.INTEGER) && recordToInsert.getValueInteger(primaryKeyField.getName()) > nextSerial)
         {
            nextSerial = recordToInsert.getValueInteger(primaryKeyField.getName()) + 1;
         }
         else if(primaryKeyField.getType().equals(QFieldType.LONG) && recordToInsert.getValueLong(primaryKeyField.getName()) > nextSerial)
         {
            //////////////////////////////////////
            // todo - mmm, could overflow here? //
            //////////////////////////////////////
            nextSerial = recordToInsert.getValueInteger(primaryKeyField.getName()) + 1;
         }

         tableData.put(recordToInsert.getValue(primaryKeyField.getName()), recordToInsert);
         if(returnInsertedRecords)
         {
            outputRecords.add(recordToInsert);
         }
      }

      nextSerials.put(table.getName(), nextSerial);

      return (outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> update(UpdateInput input, boolean returnUpdatedRecords)
   {
      if(input.getRecords() == null)
      {
         return (new ArrayList<>());
      }

      QTableMetaData             table     = input.getTable();
      Map<Serializable, QRecord> tableData = getTableData(table);

      List<QRecord>  outputRecords   = new ArrayList<>();
      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());
      for(QRecord record : input.getRecords())
      {
         Serializable primaryKeyValue = ValueUtils.getValueAsFieldType(primaryKeyField.getType(), record.getValue(primaryKeyField.getName()));

         if(CollectionUtils.nullSafeHasContents(record.getErrors()))
         {
            outputRecords.add(record);
            continue;
         }

         if(tableData.containsKey(primaryKeyValue))
         {
            QRecord recordToUpdate = tableData.get(primaryKeyValue);
            for(Map.Entry<String, Serializable> valueEntry : record.getValues().entrySet())
            {
               recordToUpdate.setValue(valueEntry.getKey(), valueEntry.getValue());
            }

            if(returnUpdatedRecords)
            {
               outputRecords.add(record);
            }
         }
      }

      return (outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int delete(DeleteInput input)
   {
      if(input.getPrimaryKeys() == null)
      {
         return (0);
      }

      QTableMetaData             table           = input.getTable();
      QFieldMetaData             primaryKeyField = table.getField(table.getPrimaryKeyField());
      Map<Serializable, QRecord> tableData       = getTableData(table);
      int                        rowsDeleted     = 0;
      for(Serializable primaryKeyValue : input.getPrimaryKeys())
      {
         primaryKeyValue = ValueUtils.getValueAsFieldType(primaryKeyField.getType(), primaryKeyValue);
         if(tableData.containsKey(primaryKeyValue))
         {
            tableData.remove(primaryKeyValue);
            rowsDeleted++;
         }
      }

      return (rowsDeleted);
   }



   /*******************************************************************************
    ** Setter for collectStatistics
    **
    *******************************************************************************/
   public static void setCollectStatistics(boolean collectStatistics)
   {
      MemoryRecordStore.collectStatistics = collectStatistics;
   }



   /*******************************************************************************
    ** Increment a statistic
    **
    *******************************************************************************/
   public static void incrementStatistic(AbstractActionInput input)
   {
      if(collectStatistics)
      {
         actionInputs.add(input.getClass(), input);
         if(input instanceof QueryInput)
         {
            incrementStatistic(STAT_QUERIES_RAN);
         }
         else if(input instanceof InsertInput)
         {
            incrementStatistic(STAT_INSERTS_RAN);
         }
      }
   }



   /*******************************************************************************
    ** Increment a statistic
    **
    *******************************************************************************/
   public static void incrementStatistic(String statName)
   {
      if(collectStatistics)
      {
         statistics.putIfAbsent(statName, 0);
         statistics.put(statName, statistics.get(statName) + 1);
      }
   }



   /*******************************************************************************
    ** clear the map of statistics
    **
    *******************************************************************************/
   public static void resetStatistics()
   {
      statistics.clear();
      actionInputs.clear();
   }



   /*******************************************************************************
    ** Getter for statistics
    **
    *******************************************************************************/
   public static Map<String, Integer> getStatistics()
   {
      return statistics;
   }



   /*******************************************************************************
    ** Getter for the actionInputs that were recorded - only while collectStatistics
    ** was true.
    *******************************************************************************/
   public static ListingHash<Class<? extends AbstractActionInput>, AbstractActionInput> getActionInputs()
   {
      return (actionInputs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AggregateOutput aggregate(AggregateInput aggregateInput) throws QException
   {
      //////////////////////
      // first do a query //
      //////////////////////
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(aggregateInput.getTableName());
      queryInput.setFilter(aggregateInput.getFilter());
      queryInput.setQueryJoins(aggregateInput.getQueryJoins());
      List<QRecord> queryResult = query(queryInput);

      List<AggregateResult> results    = new ArrayList<>();
      List<GroupBy>         groupBys   = CollectionUtils.nonNullList(aggregateInput.getGroupBys());
      List<Aggregate>       aggregates = CollectionUtils.nonNullList(aggregateInput.getAggregates());

      /////////////////////
      // do the group-by //
      /////////////////////
      ListingHash<List<Serializable>, QRecord> bins = new ListingHash<>();
      for(QRecord record : queryResult)
      {
         List<Serializable> groupByValues = new ArrayList<>(groupBys.size());
         for(GroupBy groupBy : groupBys)
         {
            Serializable groupByValue = record.getValue(groupBy.getFieldName());
            if(StringUtils.hasContent(groupBy.getFormatString()))
            {
               groupByValue = applyFormatString(groupByValue, groupBy);
            }
            else if(groupBy.getType() != null)
            {
               groupByValue = ValueUtils.getValueAsFieldType(groupBy.getType(), groupByValue);
            }
            groupByValues.add(groupByValue);
         }

         bins.add(groupByValues, record);
      }

      ////////////////////////
      // do the aggregating //
      ////////////////////////
      for(Map.Entry<List<Serializable>, List<QRecord>> entry : bins.entrySet())
      {
         List<Serializable> groupByValueList = entry.getKey();
         List<QRecord>      records          = entry.getValue();

         AggregateResult aggregateResult = new AggregateResult();
         results.add(aggregateResult);

         ////////////////////////////////////////////
         // set the group-by values in this result //
         ////////////////////////////////////////////
         Map<GroupBy, Serializable> groupByValues = new HashMap<>();
         aggregateResult.setGroupByValues(groupByValues);
         for(int i = 0; i < groupBys.size(); i++)
         {
            GroupBy      groupBy = groupBys.get(i);
            Serializable value   = groupByValueList.get(i);
            groupByValues.put(groupBy, value);
         }

         ////////////////////////////
         // compute the aggregates //
         ////////////////////////////
         Map<Aggregate, Serializable> aggregateValues = new HashMap<>();
         aggregateResult.setAggregateValues(aggregateValues);

         for(Aggregate aggregate : aggregates)
         {
            Serializable aggregateValue = computeAggregate(records, aggregate, aggregateInput.getTable());

            aggregateValues.put(aggregate, aggregateValue);
         }
      }

      /////////////////////
      // sort the result //
      /////////////////////
      if(aggregateInput.getFilter() != null && CollectionUtils.nullSafeHasContents(aggregateInput.getFilter().getOrderBys()))
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // lambda to compare 2 serializables, as we'll assume (& cast) them to Comparables //
         /////////////////////////////////////////////////////////////////////////////////////
         Comparator<Serializable> serializableComparator = (Serializable a, Serializable b) ->
         {
            if(a == null && b == null)
            {
               return (0);
            }
            else if(a == null)
            {
               return (1);
            }
            else if(b == null)
            {
               return (-1);
            }

            @SuppressWarnings("unchecked")
            Comparable<Serializable> comparableSerializableA = (Comparable<Serializable>) a;

            return comparableSerializableA.compareTo(b);
         };

         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // reverse of the lambda above (we had some errors calling .reversed() on the comparator we were building, so this seemed simpler & worked) //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         Comparator<Serializable> reverseSerializableComparator = (Serializable a, Serializable b) -> -serializableComparator.compare(a, b);

         ////////////////////////////////////////////////
         // build a comparator out of all the orderBys //
         ////////////////////////////////////////////////
         Comparator<AggregateResult> comparator = null;
         for(QFilterOrderBy orderBy : aggregateInput.getFilter().getOrderBys())
         {
            Function<AggregateResult, Serializable> keyExtractor = aggregateResult ->
            {
               if(orderBy instanceof QFilterOrderByGroupBy orderByGroupBy)
               {
                  return aggregateResult.getGroupByValue(orderByGroupBy.getGroupBy());
               }
               else if(orderBy instanceof QFilterOrderByAggregate orderByAggregate)
               {
                  return aggregateResult.getAggregateValue(orderByAggregate.getAggregate());
               }
               else
               {
                  throw (new IllegalStateException("Unexpected orderBy [" + orderBy + "] in aggregate"));
               }
            };

            if(comparator == null)
            {
               comparator = Comparator.comparing(keyExtractor, orderBy.getIsAscending() ? serializableComparator : reverseSerializableComparator);
            }
            else
            {
               comparator = comparator.thenComparing(keyExtractor, orderBy.getIsAscending() ? serializableComparator : reverseSerializableComparator);
            }
         }

         ///////////////////////////////////////
         // sort the list with the comparator //
         ///////////////////////////////////////
         results.sort(comparator);
      }

      AggregateOutput aggregateOutput = new AggregateOutput();
      aggregateOutput.setResults(results);
      return (aggregateOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable applyFormatString(Serializable value, GroupBy groupBy) throws QException
   {
      if(value == null)
      {
         return (null);
      }

      String formatString = groupBy.getFormatString();

      try
      {
         if(formatString.startsWith("DATE_FORMAT"))
         {
            /////////////////////////////////////////////////////////////////////////////
            // one known-use case we have here looks like this:                        //
            // DATE_FORMAT(CONVERT_TZ(%s, 'UTC', 'UTC'), '%%Y-%%m-%%dT%%H')            //
            // ... for now, let's just try to support the formatting bit at the end... //
            // todo - support the CONVERT_TZ bit too!                                  //
            /////////////////////////////////////////////////////////////////////////////
            String            sqlDateTimeFormat = formatString.replaceFirst(".*'%%", "%%").replaceFirst("'.*", "");
            DateTimeFormatter dateTimeFormatter = DateTimeGroupBy.sqlDateFormatToSelectedDateTimeFormatter(sqlDateTimeFormat);
            if(dateTimeFormatter == null)
            {
               throw (new QException("Unsupported sql dateTime format string [" + sqlDateTimeFormat + "] for MemoryRecordStore"));
            }

            String        valueAsString  = ValueUtils.getValueAsString(value);
            Instant       valueAsInstant = ValueUtils.getValueAsInstant(valueAsString);
            ZonedDateTime zonedDateTime  = valueAsInstant.atZone(ZoneId.systemDefault());
            return (dateTimeFormatter.format(zonedDateTime));
         }
         else
         {
            throw (new QException("Unsupported group-by format string [" + formatString + "] for MemoryRecordStore"));
         }
      }
      catch(QException qe)
      {
         throw (qe);
      }
      catch(Exception e)
      {
         throw (new QException("Error applying format string [" + formatString + "] to group by value [" + value + "]", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings({ "rawtypes", "unchecked" })
   private static Serializable computeAggregate(List<QRecord> records, Aggregate aggregate, QTableMetaData table)
   {
      String            fieldName = aggregate.getFieldName();
      AggregateOperator operator  = aggregate.getOperator();
      QFieldType        fieldType;
      if(aggregate.getFieldType() == null)
      {
         // todo - joins probably?
         QFieldMetaData field = table.getField(fieldName);
         if((field.getType().equals(QFieldType.INTEGER) || field.getType().equals(QFieldType.LONG)) && (operator.equals(AggregateOperator.AVG)))
         {
            fieldType = QFieldType.DECIMAL;
         }
         else if(operator.equals(AggregateOperator.COUNT) || operator.equals(AggregateOperator.COUNT_DISTINCT))
         {
            fieldType = QFieldType.INTEGER;
         }
         else
         {
            fieldType = field.getType();
         }
      }
      else
      {
         fieldType = aggregate.getFieldType();
      }

      Serializable aggregateValue = switch(operator)
      {
         case COUNT -> records.stream()
            .filter(r -> r.getValue(fieldName) != null)
            .count();

         case COUNT_DISTINCT -> records.stream()
            .filter(r -> r.getValue(fieldName) != null)
            .map(r -> r.getValue(fieldName))
            .collect(Collectors.toSet())
            .size();

         case SUM -> switch(fieldType)
         {
            case INTEGER -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToInt(r -> r.getValueInteger(fieldName))
               .sum();
            case LONG -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToLong(r -> r.getValueLong(fieldName))
               .sum();
            case DECIMAL -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .map(r -> r.getValueBigDecimal(fieldName))
               .reduce(BigDecimal.ZERO, BigDecimal::add);
            default -> throw (new IllegalArgumentException("Cannot perform " + operator + " aggregate on " + fieldType + " field."));
         };

         case MIN -> switch(fieldType)
         {
            case INTEGER -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToInt(r -> r.getValueInteger(fieldName))
               .min()
               .stream().boxed().findFirst().orElse(null);
            case LONG -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToLong(r -> r.getValueLong(fieldName))
               .min()
               .stream().boxed().findFirst().orElse(null);
            case DECIMAL, STRING, DATE, DATE_TIME ->
            {
               Optional<Serializable> serializable = records.stream()
                  .filter(r -> r.getValue(fieldName) != null)
                  .map(r -> ((Comparable) ValueUtils.getValueAsFieldType(fieldType, r.getValue(fieldName))))
                  .min(Comparator.naturalOrder())
                  .map(c -> (Serializable) c);
               yield serializable.orElse(null);
            }
            default -> throw (new IllegalArgumentException("Cannot perform " + operator + " aggregate on " + fieldType + " field."));
         };

         case MAX -> switch(fieldType)
         {
            case INTEGER -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToLong(r -> r.getValueInteger(fieldName))
               .max()
               .stream().boxed().findFirst().orElse(null);
            case LONG -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToLong(r -> r.getValueLong(fieldName))
               .max()
               .stream().boxed().findFirst().orElse(null);
            case DECIMAL, STRING, DATE, DATE_TIME ->
            {
               Optional<Serializable> serializable = records.stream()
                  .filter(r -> r.getValue(fieldName) != null)
                  .map(r -> ((Comparable) ValueUtils.getValueAsFieldType(fieldType, r.getValue(fieldName))))
                  .max(Comparator.naturalOrder())
                  .map(c -> (Serializable) c);
               yield serializable.orElse(null);
            }
            default -> throw (new IllegalArgumentException("Cannot perform " + operator + " aggregate on " + fieldType + " field."));
         };

         case AVG -> switch(fieldType)
         {
            case INTEGER -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToInt(r -> r.getValueInteger(fieldName))
               .average()
               .stream().boxed().findFirst().orElse(null);
            case LONG -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToLong(r -> r.getValueLong(fieldName))
               .average()
               .stream().boxed().findFirst().orElse(null);
            case DECIMAL -> records.stream()
               .filter(r -> r.getValue(fieldName) != null)
               .mapToDouble(r -> r.getValueBigDecimal(fieldName).doubleValue())
               .average()
               .stream().boxed().map(d -> new BigDecimal(d)).findFirst().orElse(null);
            default -> throw (new IllegalArgumentException("Cannot perform " + operator + " aggregate on " + fieldType + " field."));
         };
      };

      return ValueUtils.getValueAsFieldType(fieldType, aggregateValue);
   }



   /*******************************************************************************
    ** Either clone the input filter (so we can change it safely), or return a new blank filter.
    *******************************************************************************/
   protected QQueryFilter clonedOrNewFilter(QQueryFilter filter)
   {
      if(filter == null)
      {
         return (new QQueryFilter());
      }
      else
      {
         return (filter.clone());
      }
   }
}
