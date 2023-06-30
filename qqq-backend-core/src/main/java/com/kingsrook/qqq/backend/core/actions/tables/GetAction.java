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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPostQueryCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.interfaces.GetInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.GetActionCacheHelper;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Action to run a get against a table.
 **
 *******************************************************************************/
public class GetAction
{
   private Optional<AbstractPostQueryCustomizer> postGetRecordCustomizer;

   private GetInput                 getInput;
   private QPossibleValueTranslator qPossibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetOutput execute(GetInput getInput) throws QException
   {
      ActionHelper.validateSession(getInput);

      QTableMetaData table = getInput.getTable();
      if(table == null)
      {
         throw (new QException("Requested to Get a record from an unrecognized table: " + getInput.getTableName()));
      }

      postGetRecordCustomizer = QCodeLoader.getTableCustomizer(AbstractPostQueryCustomizer.class, table, TableCustomizers.POST_QUERY_RECORD.getRole());
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
         new GetActionCacheHelper().handleCaching(getInput, getOutput);
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
    ** Run a GetAction by using the QueryAction instead (e.g., with a filter made
    ** from the pkey/ukey, and returning the single record if found).
    *******************************************************************************/
   public GetOutput executeViaQuery(GetInput getInput) throws QException
   {
      return (new DefaultGetInterface().execute(getInput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class DefaultGetInterface implements GetInterface
   {
      @Override
      public GetOutput execute(GetInput getInput) throws QException
      {
         QueryInput queryInput = convertGetInputToQueryInput(getInput);

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
    **
    *******************************************************************************/
   public static QueryInput convertGetInputToQueryInput(GetInput getInput) throws QException
   {
      QueryInput queryInput = new QueryInput();
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
      queryInput.setTransaction(getInput.getTransaction());
      queryInput.setIncludeAssociations(getInput.getIncludeAssociations());
      queryInput.setAssociationNamesToInclude(getInput.getAssociationNamesToInclude());
      queryInput.setShouldTranslatePossibleValues(getInput.getShouldTranslatePossibleValues());
      queryInput.setShouldGenerateDisplayValues(getInput.getShouldGenerateDisplayValues());
      queryInput.setShouldFetchHeavyFields(getInput.getShouldFetchHeavyFields());
      queryInput.setShouldMaskPasswords(getInput.getShouldMaskPasswords());
      queryInput.setShouldOmitHiddenFields(getInput.getShouldOmitHiddenFields());
      return queryInput;
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
         returnRecord = postGetRecordCustomizer.get().apply(List.of(record)).get(0);
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
         QValueFormatter.setDisplayValuesInRecords(getInput.getTable(), List.of(returnRecord));
      }

      if(getInput.getShouldOmitHiddenFields() || getInput.getShouldMaskPasswords())
      {
         Map<String, QFieldMetaData> fields = QContext.getQInstance().getTable(getInput.getTableName()).getFields();
         for(String fieldName : fields.keySet())
         {
            QFieldMetaData field = fields.get(fieldName);
            if(getInput.getShouldOmitHiddenFields() && field.getIsHidden())
            {
               returnRecord.removeValue(fieldName);
            }
            else if(getInput.getShouldMaskPasswords() && field.getType() != null && field.getType().needsMasked() && !field.hasAdornmentType(AdornmentType.REVEAL))
            {
               //////////////////////////////////////////////////////////////////////
               // empty out the value completely first (which will remove from     //
               // display fields as well) then update display value if flag is set //
               //////////////////////////////////////////////////////////////////////
               returnRecord.removeValue(fieldName);
               returnRecord.setValue(fieldName, "************");
               if(getInput.getShouldGenerateDisplayValues())
               {
                  returnRecord.setDisplayValue(fieldName, record.getValueString(fieldName));
               }
            }
         }
      }

      //////////////////////////////////////////////////////////////////////////////
      // note - shouldFetchHeavyFields should be handled by the underlying action //
      //////////////////////////////////////////////////////////////////////////////

      return (returnRecord);
   }
}
