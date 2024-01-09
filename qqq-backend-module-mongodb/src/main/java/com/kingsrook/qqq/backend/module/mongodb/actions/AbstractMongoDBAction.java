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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLockFilters;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
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


/*******************************************************************************
 ** Base class for all mongoDB module actions.
 *******************************************************************************/
public class AbstractMongoDBAction
{
   private static final QLogger LOG = QLogger.getLogger(AbstractMongoDBAction.class);



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

      ConnectionString connectionString = new ConnectionString("mongodb://" + backend.getHost() + ":" + backend.getPort() + "/");

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

      ///////////////////////////////////////////////////////////////////////////
      // todo - this - or iterate over the values in the document??            //
      // seems like, maybe, this is an attribute in the table-backend-details? //
      ///////////////////////////////////////////////////////////////////////////
      Map<String, Serializable> values = record.getValues();
      for(QFieldMetaData field : table.getFields().values())
      {
         String fieldBackendName = getFieldBackendName(field);
         Object value            = document.get(fieldBackendName);
         String fieldName        = field.getName();

         setValue(values, fieldName, value);
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
   protected Document recordToDocument(QTableMetaData table, QRecord record)
   {
      Document document = new Document();

      ///////////////////////////////////////////////////////////////////////////
      // todo - this - or iterate over the values in the record??              //
      // seems like, maybe, this is an attribute in the table-backend-details? //
      ///////////////////////////////////////////////////////////////////////////
      for(QFieldMetaData field : table.getFields().values())
      {
         if(field.getName().equals(table.getPrimaryKeyField()) && record.getValue(field.getName()) == null)
         {
            ////////////////////////////////////
            // let mongodb client generate id //
            ////////////////////////////////////
            continue;
         }

         String fieldBackendName = getFieldBackendName(field);
         document.append(fieldBackendName, record.getValue(field.getName()));
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
      return (Filters.and(searchQueryWithoutSecurity, searchQueryForSecurity));
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
         if(RecordSecurityLock.NullValueBehavior.ALLOW.equals(recordSecurityLock.getNullValueBehavior()))
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
         if(RecordSecurityLock.NullValueBehavior.ALLOW.equals(recordSecurityLock.getNullValueBehavior()))
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

         if(field.getName().equals(table.getPrimaryKeyField()))
         {
            ListIterator<Serializable> iterator = values.listIterator();
            while(iterator.hasNext())
            {
               Serializable value = iterator.next();
               iterator.set(new ObjectId(String.valueOf(value)));
            }
         }

         Serializable value0 = values.get(0);
         criteriaFilters.add(switch(criteria.getOperator())
         {
            case EQUALS -> Filters.eq(fieldBackendName, value0);
            case NOT_EQUALS -> Filters.ne(fieldBackendName, value0);
            case NOT_EQUALS_OR_IS_NULL -> Filters.or(
               Filters.eq(fieldBackendName, null),
               Filters.ne(fieldBackendName, value0)
            );
            case IN -> filterIn(fieldBackendName, values);
            case NOT_IN -> Filters.not(filterIn(fieldBackendName, values));
            case IS_NULL_OR_IN -> Filters.or(
               Filters.eq(fieldBackendName, null),
               filterIn(fieldBackendName, values)
            );
            case LIKE -> filterRegex(fieldBackendName, null, ValueUtils.getValueAsString(value0).replaceAll("%", ".*"), null);
            case NOT_LIKE -> Filters.not(filterRegex(fieldBackendName, null, ValueUtils.getValueAsString(value0).replaceAll("%", ".*"), null));
            case STARTS_WITH -> filterRegex(fieldBackendName, null, value0, ".*");
            case ENDS_WITH -> filterRegex(fieldBackendName, ".*", value0, null);
            case CONTAINS -> filterRegex(fieldBackendName, ".*", value0, ".*");
            case NOT_STARTS_WITH -> Filters.not(filterRegex(fieldBackendName, null, value0, ".*"));
            case NOT_ENDS_WITH -> Filters.not(filterRegex(fieldBackendName, ".*", value0, null));
            case NOT_CONTAINS -> Filters.not(filterRegex(fieldBackendName, ".*", value0, ".*"));
            case LESS_THAN -> Filters.lt(fieldBackendName, value0);
            case LESS_THAN_OR_EQUALS -> Filters.lte(fieldBackendName, value0);
            case GREATER_THAN -> Filters.gt(fieldBackendName, value0);
            case GREATER_THAN_OR_EQUALS -> Filters.gte(fieldBackendName, value0);
            case IS_BLANK -> filterIsBlank(fieldBackendName);
            case IS_NOT_BLANK -> Filters.not(filterIsBlank(fieldBackendName));
            case BETWEEN -> filterBetween(fieldBackendName, values);
            case NOT_BETWEEN -> Filters.not(filterBetween(fieldBackendName, values));
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

      String fullRegex = prefix + Pattern.quote(ValueUtils.getValueAsString(mainRegex) + suffix);
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
         Filters.gte(fieldBackendName, values.get(0)),
         Filters.lte(fieldBackendName, values.get(1))
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
}
