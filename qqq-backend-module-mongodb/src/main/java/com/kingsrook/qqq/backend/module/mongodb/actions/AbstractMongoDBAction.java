/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.mongodb.actions;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.AbstractFilterExpression;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.security.NullValueBehaviorUtil;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLockFilters;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.querystats.QueryStat;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBTableBackendDetails;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Base class for all mongoDB module actions.
 *******************************************************************************/
public class AbstractMongoDBAction
{
   private static final QLogger LOG = QLogger.getLogger(AbstractMongoDBAction.class);

   protected QueryStat queryStat;



   /*******************************************************************************
    ** Open a MongoDB Client / session -- re-using the one in the input transaction
    ** if it is present.
    *******************************************************************************/
   public MongoClientContainer openClient(MongoDBBackendMetaData backend, QBackendTransaction transaction)
   {
      if(transaction instanceof MongoDBTransaction mongoDBTransaction)
      {
         //////////////////////////////////////////////////////////////////////////////////////////
         // re-use the connection from the transaction (indicating false in last parameter here) //
         //////////////////////////////////////////////////////////////////////////////////////////
         return (new MongoClientContainer(mongoDBTransaction.getMongoClient(), mongoDBTransaction.getClientSession(), false));
      }

      String           suffix           = StringUtils.hasContent(backend.getUrlSuffix()) ? "?" + backend.getUrlSuffix() : "";
      ConnectionString connectionString = new ConnectionString("mongodb://" + backend.getHost() + ":" + backend.getPort() + "/" + suffix);

      MongoCredential credential = MongoCredential.createCredential(backend.getUsername(), backend.getAuthSourceDatabase(), backend.getPassword().toCharArray());

      MongoClientSettings settings = MongoClientSettings.builder()

         ////////////////////////////////////////////////
         // is this needed, what, for a cluster maybe? //
         ////////////////////////////////////////////////
         // .applyToClusterSettings(builder -> builder.hosts(seeds))

         .applyConnectionString(connectionString)
         .credential(credential)
         .build();

      MongoClient mongoClient = MongoClients.create(settings);

      ////////////////////////////////////////////////////////////////////////////
      // indicate that this connection was newly opened via the true param here //
      ////////////////////////////////////////////////////////////////////////////
      return (new MongoClientContainer(mongoClient, mongoClient.startSession(), true));
   }



   /*******************************************************************************
    ** Get the name to use for a field in the mongoDB, from the fieldMetaData.
    **
    ** That is, field.backendName if set -- else, field.name
    *******************************************************************************/
   protected String getFieldBackendName(QFieldMetaData field)
   {
      if(field.getBackendName() != null)
      {
         return (field.getBackendName());
      }
      return (field.getName());
   }



   /*******************************************************************************
    ** Get the name to use for a table in the mongoDB, from the table's backendDetails.
    **
    ** else, the table's name.
    *******************************************************************************/
   protected String getBackendTableName(QTableMetaData table)
   {
      if(table == null)
      {
         return (null);
      }

      if(table.getBackendDetails() != null)
      {
         String backendTableName = ((MongoDBTableBackendDetails) table.getBackendDetails()).getTableName();
         if(StringUtils.hasContent(backendTableName))
         {
            return (backendTableName);
         }
      }
      return table.getName();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected int getPageSize()
   {
      return (1000);
   }



   /*******************************************************************************
    ** Convert a mongodb document to a QRecord.
    *******************************************************************************/
   protected QRecord documentToRecord(QTableMetaData table, Document document)
   {
      QRecord record = new QRecord();
      record.setTableName(table.getName());

      //////////////////////////////////////////////////////////////////////////////////////////////
      // first iterate over the table's fields, looking for them (at their backend name (path,    //
      // if it has dots) inside the document note that we'll remove values from the document      //
      // as we go - then after this loop, will handle all remaining values as unstructured fields //
      //////////////////////////////////////////////////////////////////////////////////////////////
      Map<String, Serializable> values = record.getValues();
      for(QFieldMetaData field : table.getFields().values())
      {
         String fieldName = field.getName();
         String fieldBackendName = getFieldBackendName(field);

         if(fieldBackendName.contains("."))
         {
            /////////////////////////////////////////////////////////////
            // process backend-names with dots as hierarchical objects //
            /////////////////////////////////////////////////////////////
            String[] parts       = fieldBackendName.split("\\.");
            Document tmpDocument = document;
            for(int i = 0; i < parts.length - 1; i++)
            {
               if(!tmpDocument.containsKey(parts[i]))
               {
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // if we can't find the sub-document, break, and we won't have a value for this field (do we want null?) //
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                  setValue(values, fieldName, null);
                  break;
               }
               else
               {
                  if(tmpDocument.get(parts[i]) instanceof Document subDocument)
                  {
                     tmpDocument = subDocument;
                  }
                  else
                  {
                     LOG.warn("Unexpected - In table [" + table.getName() + "] found a non-document at sub-key [" + parts[i] + "] for field [" + field.getName() + "]");
                  }
               }
            }

            Object value = tmpDocument.remove(parts[parts.length - 1]);
            setValue(values, fieldName, value);
         }
         else
         {
            Object value = document.remove(fieldBackendName);
            setValue(values, fieldName, value);
         }
      }

      //////////////////////////////////////////////////////////////
      // handle remaining values in the document as un-structured //
      //////////////////////////////////////////////////////////////
      for(String subFieldName : document.keySet())
      {
         Object subValue = document.get(subFieldName);
         setValue(values, subFieldName, subValue);
      }

      return (record);
   }



   /*******************************************************************************
    ** Recursive helper method to put a value in a map - where mongodb documents
    ** are recursively expanded, and types are mapped to QQQ expectations.
    *******************************************************************************/
   private void setValue(Map<String, Serializable> values, String fieldName, Object value)
   {
      if(value instanceof ObjectId objectId)
      {
         values.put(fieldName, objectId.toString());
      }
      else if(value instanceof java.util.Date date)
      {
         values.put(fieldName, date.toInstant());
      }
      else if(value instanceof Document document)
      {
         LinkedHashMap<String, Serializable> subValues = new LinkedHashMap<>();
         values.put(fieldName, subValues);

         for(String subFieldName : document.keySet())
         {
            Object subValue = document.get(subFieldName);
            setValue(subValues, subFieldName, subValue);
         }
      }
      else if(value instanceof Serializable s)
      {
         values.put(fieldName, s);
      }
      else if(value != null)
      {
         values.put(fieldName, String.valueOf(value));
      }
      else
      {
         values.put(fieldName, null);
      }
   }



   /*******************************************************************************
    ** Convert a QRecord to a mongodb document.
    *******************************************************************************/
   protected Document recordToDocument(QTableMetaData table, QRecord record) throws QException
   {
      Document document = new Document();

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // first iterate over fields defined in the table - put them in the document for mongo first. //
      // track the names that we've processed in a set. then later we'll go over all values in the  //
      // record and send them all to mongo (skipping ones we knew about from the table definition)  //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      Set<String> processedFields = new HashSet<>();

      for(QFieldMetaData field : table.getFields().values())
      {
         Serializable value = record.getValue(field.getName());
         processedFields.add(field.getName());

         if(field.getName().equals(table.getPrimaryKeyField()) && value == null)
         {
            ////////////////////////////////////
            // let mongodb client generate id //
            ////////////////////////////////////
            continue;
         }

         String fieldBackendName = getFieldBackendName(field);
         if(fieldBackendName.contains("."))
         {
            /////////////////////////////////////////////////////////////
            // process backend-names with dots as hierarchical objects //
            /////////////////////////////////////////////////////////////
            String[] parts       = fieldBackendName.split("\\.");
            Document tmpDocument = document;
            for(int i = 0; i < parts.length - 1; i++)
            {
               if(!tmpDocument.containsKey(parts[i]))
               {
                  Document subDocument = new Document();
                  tmpDocument.put(parts[i], subDocument);
                  tmpDocument = subDocument;
               }
               else
               {
                  if(tmpDocument.get(parts[i]) instanceof Document subDocument)
                  {
                     tmpDocument = subDocument;
                  }
                  else
                  {
                     throw (new QException("Fields in table [" + table.getName() + "] specify both a sub-object and a field at the key: " + parts[i]));
                  }
               }
            }
            tmpDocument.append(parts[parts.length - 1], value);
         }
         else
         {
            document.append(fieldBackendName, value);
         }
      }

      /////////////////////////
      // do remaining values //
      /////////////////////////
      for(Map.Entry<String, Serializable> entry : record.getValues().entrySet())
      {
         if(!processedFields.contains(entry.getKey()))
         {
            document.append(entry.getKey(), entry.getValue());
         }
      }

      return (document);
   }



   /*******************************************************************************
    ** Convert QQueryFilter to Bson search query document - including security
    ** for the table if needed.
    *******************************************************************************/
   protected Bson makeSearchQueryDocument(QTableMetaData table, QQueryFilter filter) throws QException
   {
      Bson         searchQueryWithoutSecurity = makeSearchQueryDocumentWithoutSecurity(table, filter);
      QQueryFilter securityFilter             = makeSecurityQueryFilter(table);
      if(!securityFilter.hasAnyCriteria())
      {
         return (searchQueryWithoutSecurity);
      }

      Bson searchQueryForSecurity = makeSearchQueryDocumentWithoutSecurity(table, securityFilter);

      if(searchQueryWithoutSecurity.toBsonDocument().isEmpty())
      {
         return (searchQueryForSecurity);
      }
      else
      {
         return (Filters.and(searchQueryWithoutSecurity, searchQueryForSecurity));
      }
   }



   /*******************************************************************************
    ** Build a QQueryFilter to apply record-level security to the query.
    ** Note, it may be empty, if there are no lock fields, or all are all-access.
    **
    ** Originally copied from RDBMS module... should this be shared?
    ** and/or, how big of a re-write did that get in the joins-enhancements branch...
    *******************************************************************************/
   private QQueryFilter makeSecurityQueryFilter(QTableMetaData table) throws QException
   {
      QQueryFilter securityFilter = new QQueryFilter();
      securityFilter.setBooleanOperator(QQueryFilter.BooleanOperator.AND);

      for(RecordSecurityLock recordSecurityLock : RecordSecurityLockFilters.filterForReadLocks(CollectionUtils.nonNullList(table.getRecordSecurityLocks())))
      {
         addSubFilterForRecordSecurityLock(QContext.getQInstance(), QContext.getQSession(), table, securityFilter, recordSecurityLock, null, table.getName(), false);
      }

      return (securityFilter);
   }



   /*******************************************************************************
    ** Helper for makeSecuritySearchQuery.
    **
    ** Originally copied from RDBMS module... should this be shared?
    ** and/or, how big of a re-write did that get in the joins-enhancements branch...
    *******************************************************************************/
   private static void addSubFilterForRecordSecurityLock(QInstance instance, QSession session, QTableMetaData table, QQueryFilter securityFilter, RecordSecurityLock recordSecurityLock, JoinsContext joinsContext, String tableNameOrAlias, boolean isOuter) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check if the key type has an all-access key, and if so, if it's set to true for the current user/session //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QSecurityKeyType securityKeyType = instance.getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
      if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()))
      {
         if(session.hasSecurityKeyValue(securityKeyType.getAllAccessKeyName(), true, QFieldType.BOOLEAN))
         {
            ///////////////////////////////////////////////////////////////////////////////
            // if we have all-access on this key, then we don't need a criterion for it. //
            ///////////////////////////////////////////////////////////////////////////////
            return;
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////
      // some differences from RDBMS here, due to not yet having joins support in mongo... //
      ///////////////////////////////////////////////////////////////////////////////////////
      // String fieldName = tableNameOrAlias + "." + recordSecurityLock.getFieldName();
      String fieldName = recordSecurityLock.getFieldName();
      if(CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()))
      {
         throw (new QException("Security locks in mongodb with joinNameChain is not yet supported"));
         // fieldName = recordSecurityLock.getFieldName();
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
         if(joinsContext == null)
         {
            type = table.getField(fieldName).getType();
         }
         else
         {
            JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(fieldName);
            type = fieldAndTableNameOrAlias.field().getType();
         }
      }
      catch(Exception e)
      {
         LOG.debug("Error getting field type...  Trying Integer", e);
      }

      List<Serializable> securityKeyValues = session.getSecurityKeyValues(recordSecurityLock.getSecurityKeyType(), type);
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
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // else, if no user/session values, and null-value behavior is deny, then setup a FALSE condition, to allow no rows.           //
            // todo - make some explicit contradiction here - maybe even avoid running the whole query - as you're not allowed ANY records //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IN, Collections.emptyList()));
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

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if this field is on the outer side of an outer join, then if we do a straight filter on it, then we're basically      //
      // nullifying the outer join... so for an outer join use-case, OR the security field criteria with a primary-key IS NULL //
      // which will make missing rows from the join be found.                                                                  //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(isOuter)
      {
         lockFilter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
         lockFilter.addCriteria(new QFilterCriteria(tableNameOrAlias + "." + table.getPrimaryKeyField(), QCriteriaOperator.IS_BLANK));
      }

      securityFilter.addSubFilter(lockFilter);
   }



   /*******************************************************************************
    ** w/o considering security, just map a QQueryFilter to a Bson searchQuery.
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   private Bson makeSearchQueryDocumentWithoutSecurity(QTableMetaData table, QQueryFilter filter)
   {
      if(filter == null || !filter.hasAnyCriteria())
      {
         return (new Document());
      }

      List<Bson> criteriaFilters = new ArrayList<>();

      for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         List<Serializable> values           = criteria.getValues() == null ? new ArrayList<>() : new ArrayList<>(criteria.getValues());
         QFieldMetaData     field            = table.getField(criteria.getFieldName());
         String             fieldBackendName = getFieldBackendName(field);

         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // replace any expression-type values with their evaluation                                                                         //
         // also, "scrub" non-expression values, which type-converts them (e.g., strings in various supported date formats become LocalDate) //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         ListIterator<Serializable> valueListIterator = values.listIterator();
         while(valueListIterator.hasNext())
         {
            Serializable value = valueListIterator.next();
            if(value instanceof AbstractFilterExpression<?> expression)
            {
               valueListIterator.set(expression.evaluate());
            }
            /*
            todo - is this needed??
            else
            {
               Serializable scrubbedValue = scrubValue(field, value);
               valueListIterator.set(scrubbedValue);
            }
            */
         }

         /////////////////////////////////////////////////////////////////////////////////////////
         // make sure any values we're going to run against the primary key (_id) are ObjectIds //
         /////////////////////////////////////////////////////////////////////////////////////////
         if(field.getName().equals(table.getPrimaryKeyField()))
         {
            ListIterator<Serializable> iterator = values.listIterator();
            while(iterator.hasNext())
            {
               Serializable value = iterator.next();
               iterator.set(new ObjectId(String.valueOf(value)));
            }
         }

         ////////
         // :( //
         ////////
         if(StringUtils.hasContent(criteria.getOtherFieldName()))
         {
            throw (new IllegalArgumentException("A mongodb query with an 'otherFieldName' specified is not currently supported."));
         }

         criteriaFilters.add(switch(criteria.getOperator())
         {
            case EQUALS -> Filters.eq(fieldBackendName, getValue(values, 0));

            case NOT_EQUALS -> Filters.and(
               Filters.ne(fieldBackendName, getValue(values, 0)),

               ////////////////////////////////////////////////////////////////////////////////////////////
               // to match RDBMS and other QQQ backends, consider a null to not match a not-equals query //
               ////////////////////////////////////////////////////////////////////////////////////////////
               Filters.not(Filters.eq(fieldBackendName, null))
            );

            case NOT_EQUALS_OR_IS_NULL -> Filters.or(
               Filters.eq(fieldBackendName, null),
               Filters.ne(fieldBackendName, getValue(values, 0))
            );
            case IN -> filterIn(fieldBackendName, values);
            case NOT_IN -> Filters.nor(filterIn(fieldBackendName, values));
            case IS_NULL_OR_IN -> Filters.or(
               Filters.eq(fieldBackendName, null),
               filterIn(fieldBackendName, values)
            );
            case LIKE -> filterRegex(fieldBackendName, null, ValueUtils.getValueAsString(getValue(values, 0)).replaceAll("%", ".*"), null);
            case NOT_LIKE -> Filters.nor(filterRegex(fieldBackendName, null, ValueUtils.getValueAsString(getValue(values, 0)).replaceAll("%", ".*"), null));
            case STARTS_WITH -> filterRegex(fieldBackendName, null, getValue(values, 0), ".*");
            case ENDS_WITH -> filterRegex(fieldBackendName, ".*", getValue(values, 0), null);
            case CONTAINS -> filterRegex(fieldBackendName, ".*", getValue(values, 0), ".*");
            case NOT_STARTS_WITH -> Filters.nor(filterRegex(fieldBackendName, null, getValue(values, 0), ".*"));
            case NOT_ENDS_WITH -> Filters.nor(filterRegex(fieldBackendName, ".*", getValue(values, 0), null));
            case NOT_CONTAINS -> Filters.nor(filterRegex(fieldBackendName, ".*", getValue(values, 0), ".*"));
            case LESS_THAN -> Filters.lt(fieldBackendName, getValue(values, 0));
            case LESS_THAN_OR_EQUALS -> Filters.lte(fieldBackendName, getValue(values, 0));
            case GREATER_THAN -> Filters.gt(fieldBackendName, getValue(values, 0));
            case GREATER_THAN_OR_EQUALS -> Filters.gte(fieldBackendName, getValue(values, 0));
            case IS_BLANK -> filterIsBlank(fieldBackendName);
            case IS_NOT_BLANK -> Filters.nor(filterIsBlank(fieldBackendName));
            case BETWEEN -> filterBetween(fieldBackendName, values);
            case NOT_BETWEEN -> Filters.nor(filterBetween(fieldBackendName, values));
         });
      }

      /////////////////////////////////////
      // recursively process sub-filters //
      /////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(filter.getSubFilters()))
      {
         for(QQueryFilter subFilter : filter.getSubFilters())
         {
            criteriaFilters.add(makeSearchQueryDocumentWithoutSecurity(table, subFilter));
         }
      }

      Bson bson = QQueryFilter.BooleanOperator.AND.equals(filter.getBooleanOperator()) ? Filters.and(criteriaFilters) : Filters.or(criteriaFilters);
      return bson;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Serializable getValue(List<Serializable> values, int i)
   {
      if(values == null || values.size() <= i)
      {
         throw new IllegalArgumentException("Incorrect number of values given for criteria");
      }

      return (values.get(i));
   }



   /*******************************************************************************
    ** build a bson filter doing a regex (e.g., for LIKE, STARTS_WITH, etc)
    *******************************************************************************/
   private Bson filterRegex(String fieldBackendName, String prefix, Serializable mainRegex, String suffix)
   {
      if(prefix == null)
      {
         prefix = "";
      }

      if(suffix == null)
      {
         suffix = "";
      }

      String fullRegex = prefix + ValueUtils.getValueAsString(mainRegex + suffix);
      return (Filters.regex(fieldBackendName, Pattern.compile(fullRegex)));
   }



   /*******************************************************************************
    ** build a bson filter doing IN
    *******************************************************************************/
   private static Bson filterIn(String fieldBackendName, List<Serializable> values)
   {
      return Filters.in(fieldBackendName, values);
   }



   /*******************************************************************************
    ** build a bson filter doing BETWEEN
    *******************************************************************************/
   private static Bson filterBetween(String fieldBackendName, List<Serializable> values)
   {
      return Filters.and(
         Filters.gte(fieldBackendName, getValue(values, 0)),
         Filters.lte(fieldBackendName, getValue(values, 1))
      );
   }



   /*******************************************************************************
    ** build a bson filter doing BLANK (null or == "")
    *******************************************************************************/
   private static Bson filterIsBlank(String fieldBackendName)
   {
      return Filters.or(
         Filters.eq(fieldBackendName, null),
         Filters.eq(fieldBackendName, "")
      );
   }



   /*******************************************************************************
    ** Getter for queryStat
    *******************************************************************************/
   public QueryStat getQueryStat()
   {
      return (this.queryStat);
   }



   /*******************************************************************************
    ** Setter for queryStat
    *******************************************************************************/
   public void setQueryStat(QueryStat queryStat)
   {
      this.queryStat = queryStat;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void setQueryInQueryStat(Bson query)
   {
      if(queryStat != null && query != null)
      {
         queryStat.setQueryText(query.toString());

         ////////////////////////////////////////////////////////////////
         // todo - if we support joins in the future, do them here too //
         ////////////////////////////////////////////////////////////////
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void logQuery(String tableName, String actionName, List<Bson> query, Long queryStartTime)
   {

      if(System.getProperty("qqq.mongodb.logQueries", "false").equals("true"))
      {
         try
         {
            if(System.getProperty("qqq.mongodb.logQueries.output", "logger").equalsIgnoreCase("system.out"))
            {
               System.out.println("Table: " + tableName + ", Action: " + actionName + ", Query: " + query);

               if(queryStartTime != null)
               {
                  System.out.println("Query Took [" + QValueFormatter.formatValue(DisplayFormat.COMMAS, (System.currentTimeMillis() - queryStartTime)) + "] ms");
               }
            }
            else
            {
               LOG.debug("Running Query", logPair("table", tableName), logPair("action", actionName), logPair("query", query), logPair("millis", queryStartTime == null ? null : (System.currentTimeMillis() - queryStartTime)));
            }
         }
         catch(Exception e)
         {
            LOG.debug("Error logging query...", e);
         }
      }
   }

}
