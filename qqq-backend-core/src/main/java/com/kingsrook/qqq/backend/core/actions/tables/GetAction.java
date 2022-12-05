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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.interfaces.GetInterface;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.cache.CacheUseCase;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 ** Action to run a get against a table.
 **
 *******************************************************************************/
public class GetAction
{
   private Optional<Function<QRecord, QRecord>> postGetRecordCustomizer;

   private GetInput                 getInput;
   private QValueFormatter          qValueFormatter;
   private QPossibleValueTranslator qPossibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetOutput execute(GetInput getInput) throws QException
   {
      ActionHelper.validateSession(getInput);

      QTableMetaData table = getInput.getTable();
      postGetRecordCustomizer = QCodeLoader.getTableCustomizerFunction(table, TableCustomizers.POST_QUERY_RECORD.getRole());
      this.getInput = getInput;

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(getInput.getBackend());
      // todo pre-customization - just get to modify the request?

      GetInterface getInterface = null;
      try
      {
         getInterface = qModule.getGetInterface();
      }
      catch(IllegalStateException ise)
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if a module doesn't implement Get directly - try to do a Get by a Query in the DefaultGetInterface (inner class) //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      }

      GetOutput getOutput;
      if(getInterface == null)
      {
         getInterface = new DefaultGetInterface();
      }

      getInterface.validateInput(getInput);
      getOutput = getInterface.execute(getInput);

      ////////////////////////////
      // handle cache use-cases //
      ////////////////////////////
      if(table.getCacheOf() != null)
      {
         if(getOutput.getRecord() == null)
         {
            ///////////////////////////////////////////////////////////////////////
            // if the record wasn't found, see if we should look in cache-source //
            ///////////////////////////////////////////////////////////////////////
            QRecord recordFromSource = tryToGetFromCacheSource(getInput, getOutput);
            if(recordFromSource != null)
            {
               QRecord recordToCache = mapSourceRecordToCacheRecord(table, recordFromSource);

               InsertInput insertInput = new InsertInput(getInput.getInstance());
               insertInput.setSession(getInput.getSession());
               insertInput.setTableName(getInput.getTableName());
               insertInput.setRecords(List.of(recordToCache));
               InsertOutput insertOutput = new InsertAction().execute(insertInput);
               getOutput.setRecord(insertOutput.getRecords().get(0));
            }
         }
         else
         {
            /////////////////////////////////////////////////////////////////////////////////
            // if the record was found, but it's too old, maybe re-fetch from cache source //
            /////////////////////////////////////////////////////////////////////////////////
            refreshCacheIfExpired(getInput, getOutput);
         }
      }

      ////////////////////////////////////////////////////////
      // if the record is found, perform post-actions on it //
      ////////////////////////////////////////////////////////
      if(getOutput.getRecord() != null)
      {
         getOutput.setRecord(postRecordActions(getOutput.getRecord()));
      }

      return getOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord mapSourceRecordToCacheRecord(QTableMetaData table, QRecord recordFromSource)
   {
      QRecord cacheRecord = new QRecord(recordFromSource);
      if(StringUtils.hasContent(table.getCacheOf().getCachedDateFieldName()))
      {
         cacheRecord.setValue(table.getCacheOf().getCachedDateFieldName(), Instant.now());
      }
      return (cacheRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void refreshCacheIfExpired(GetInput getInput, GetOutput getOutput) throws QException
   {
      QTableMetaData table             = getInput.getTable();
      Integer        expirationSeconds = table.getCacheOf().getExpirationSeconds();
      if(expirationSeconds != null)
      {
         QRecord cachedRecord = getOutput.getRecord();
         Instant cachedDate   = cachedRecord.getValueInstant(table.getCacheOf().getCachedDateFieldName());
         if(cachedDate == null || cachedDate.isBefore(Instant.now().minus(expirationSeconds, ChronoUnit.SECONDS)))
         {
            QRecord recordFromSource = tryToGetFromCacheSource(getInput, getOutput);
            if(recordFromSource != null)
            {
               ///////////////////////////////////////////////////////////////////
               // if the record was found in the source, update it in the cache //
               ///////////////////////////////////////////////////////////////////
               QRecord recordToCache = mapSourceRecordToCacheRecord(table, recordFromSource);
               recordToCache.setValue(table.getPrimaryKeyField(), cachedRecord.getValue(table.getPrimaryKeyField()));

               UpdateInput updateInput = new UpdateInput(getInput.getInstance());
               updateInput.setSession(getInput.getSession());
               updateInput.setTableName(getInput.getTableName());
               updateInput.setRecords(List.of(recordToCache));
               UpdateOutput updateOutput = new UpdateAction().execute(updateInput);

               getOutput.setRecord(updateOutput.getRecords().get(0));
            }
            else
            {
               /////////////////////////////////////////////////////////////////////////////
               // if the record is no longer in the source, then remove it from the cache //
               /////////////////////////////////////////////////////////////////////////////
               DeleteInput deleteInput = new DeleteInput(getInput.getInstance());
               deleteInput.setSession(getInput.getSession());
               deleteInput.setTableName(getInput.getTableName());
               deleteInput.setPrimaryKeys(List.of(getOutput.getRecord().getValue(table.getPrimaryKeyField())));
               new DeleteAction().execute(deleteInput);

               getOutput.setRecord(null);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord tryToGetFromCacheSource(GetInput getInput, GetOutput getOutput) throws QException
   {
      QRecord        recordFromSource = null;
      QTableMetaData table            = getInput.getTable();

      for(CacheUseCase cacheUseCase : CollectionUtils.nonNullList(table.getCacheOf().getUseCases()))
      {
         if(CacheUseCase.Type.UNIQUE_KEY_TO_UNIQUE_KEY.equals(cacheUseCase.getType()) && getInput.getUniqueKey() != null)
         {
            recordFromSource = getFromCachedSourceForUniqueKeyToUniqueKey(getInput, table.getCacheOf().getSourceTable());
            break;
         }
         else
         {
            // todo!!
            throw new NotImplementedException("Not-yet-implemented cache use case type: " + cacheUseCase.getType());
         }
      }

      return (recordFromSource);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord getFromCachedSourceForUniqueKeyToUniqueKey(GetInput getInput, String sourceTableName) throws QException
   {
      /////////////////////////////////////////////////////
      // do a Get on the source table, by the unique key //
      /////////////////////////////////////////////////////
      GetInput sourceGetInput = new GetInput(getInput.getInstance());
      sourceGetInput.setSession(getInput.getSession());
      sourceGetInput.setTableName(sourceTableName);
      sourceGetInput.setUniqueKey(getInput.getUniqueKey());
      GetOutput sourceGetOutput = new GetAction().execute(sourceGetInput);
      return (sourceGetOutput.getRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class DefaultGetInterface implements GetInterface
   {
      @Override
      public GetOutput execute(GetInput getInput) throws QException
      {
         QueryInput queryInput = new QueryInput(getInput.getInstance());
         queryInput.setSession(getInput.getSession());
         queryInput.setTableName(getInput.getTableName());

         //////////////////////////////////////////////////
         // build filter using either pkey or unique key //
         //////////////////////////////////////////////////
         QQueryFilter filter = new QQueryFilter();
         if(getInput.getPrimaryKey() != null)
         {
            filter.addCriteria(new QFilterCriteria(getInput.getTable().getPrimaryKeyField(), QCriteriaOperator.EQUALS, getInput.getPrimaryKey()));
         }
         else if(getInput.getUniqueKey() != null)
         {
            for(Map.Entry<String, Serializable> entry : getInput.getUniqueKey().entrySet())
            {
               if(entry.getValue() == null)
               {
                  filter.addCriteria(new QFilterCriteria(entry.getKey(), QCriteriaOperator.IS_BLANK));
               }
               else
               {
                  filter.addCriteria(new QFilterCriteria(entry.getKey(), QCriteriaOperator.EQUALS, entry.getValue()));
               }
            }
         }
         else
         {
            throw (new QException("No primaryKey or uniqueKey was passed to Get"));
         }

         queryInput.setFilter(filter);

         QueryOutput queryOutput = new QueryAction().execute(queryInput);

         GetOutput getOutput = new GetOutput();
         if(!queryOutput.getRecords().isEmpty())
         {
            getOutput.setRecord(queryOutput.getRecords().get(0));
         }
         return (getOutput);
      }
   }



   /*******************************************************************************
    ** Run the necessary actions on a record.  This may include setting display values,
    ** translating possible values, and running post-record customizations.
    *******************************************************************************/
   public QRecord postRecordActions(QRecord record)
   {
      QRecord returnRecord = record;
      if(this.postGetRecordCustomizer.isPresent())
      {
         returnRecord = postGetRecordCustomizer.get().apply(record);
      }

      if(getInput.getShouldTranslatePossibleValues())
      {
         if(qPossibleValueTranslator == null)
         {
            qPossibleValueTranslator = new QPossibleValueTranslator(getInput.getInstance(), getInput.getSession());
         }
         qPossibleValueTranslator.translatePossibleValuesInRecords(getInput.getTable(), List.of(returnRecord));
      }

      if(getInput.getShouldGenerateDisplayValues())
      {
         if(qValueFormatter == null)
         {
            qValueFormatter = new QValueFormatter();
         }
         qValueFormatter.setDisplayValuesInRecords(getInput.getTable(), List.of(returnRecord));
      }

      return (returnRecord);
   }
}
