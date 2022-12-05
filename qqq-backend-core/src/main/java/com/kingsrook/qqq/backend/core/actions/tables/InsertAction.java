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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPostInsertCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.UniqueKeyHelper;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Action to insert one or more records.
 **
 *******************************************************************************/
public class InsertAction extends AbstractQActionFunction<InsertInput, InsertOutput>
{
   private static final Logger LOG = LogManager.getLogger(InsertAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      ActionHelper.validateSession(insertInput);
      QTableMetaData table = insertInput.getTable();

      Optional<AbstractPostInsertCustomizer> postInsertCustomizer = QCodeLoader.getTableCustomizer(AbstractPostInsertCustomizer.class, table, TableCustomizers.POST_INSERT_RECORD.getRole());
      setAutomationStatusField(insertInput);

      ValueBehaviorApplier.applyFieldBehaviors(insertInput.getInstance(), table, insertInput.getRecords());
      // todo - need to handle records with errors coming out of here...

      QBackendModuleInterface qModule = getBackendModuleInterface(insertInput);
      // todo pre-customization - just get to modify the request?

      setErrorsIfUniqueKeyErrors(insertInput, table);

      InsertOutput insertOutput = qModule.getInsertInterface().execute(insertInput);
      // todo post-customization - can do whatever w/ the result if you want

      if(postInsertCustomizer.isPresent())
      {
         postInsertCustomizer.get().setInsertInput(insertInput);
         insertOutput.setRecords(postInsertCustomizer.get().apply(insertOutput.getRecords()));
      }

      return insertOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setErrorsIfUniqueKeyErrors(InsertInput insertInput, QTableMetaData table) throws QException
   {
      if(CollectionUtils.nullSafeHasContents(table.getUniqueKeys()))
      {
         Map<UniqueKey, Set<List<Serializable>>> keysInThisList = new HashMap<>();
         if(insertInput.getSkipUniqueKeyCheck())
         {
            LOG.debug("Skipping unique key check in " + insertInput.getTableName() + " insert.");
            return;
         }

         ////////////////////////////////////////////
         // check for any pre-existing unique keys //
         ////////////////////////////////////////////
         Map<UniqueKey, Set<List<Serializable>>> existingKeys = new HashMap<>();
         List<UniqueKey>                         uniqueKeys   = CollectionUtils.nonNullList(table.getUniqueKeys());
         for(UniqueKey uniqueKey : uniqueKeys)
         {
            existingKeys.put(uniqueKey, UniqueKeyHelper.getExistingKeys(insertInput, insertInput.getTransaction(), table, insertInput.getRecords(), uniqueKey));
         }

         /////////////////////////////////////
         // make sure this map is populated //
         /////////////////////////////////////
         uniqueKeys.forEach(uk -> keysInThisList.computeIfAbsent(uk, x -> new HashSet<>()));

         for(QRecord record : insertInput.getRecords())
         {
            //////////////////////////////////////////////////////////
            // check if this record violates any of the unique keys //
            //////////////////////////////////////////////////////////
            boolean foundDupe = false;
            for(UniqueKey uniqueKey : uniqueKeys)
            {
               Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
               if(keyValues.isPresent() && (existingKeys.get(uniqueKey).contains(keyValues.get()) || keysInThisList.get(uniqueKey).contains(keyValues.get())))
               {
                  record.addError("Another record already exists with this " + uniqueKey.getDescription(table));
                  foundDupe = true;
                  break;
               }
            }

            ///////////////////////////////////////////////////////////////////////////////
            // if this record doesn't violate any uk's, then we can add it to the output //
            ///////////////////////////////////////////////////////////////////////////////
            if(!foundDupe)
            {
               for(UniqueKey uniqueKey : uniqueKeys)
               {
                  Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
                  keyValues.ifPresent(kv -> keysInThisList.get(uniqueKey).add(kv));
               }
            }
         }
      }
   }



   /*******************************************************************************
    ** If the table being inserted into uses an automation-status field, populate it now.
    *******************************************************************************/
   private void setAutomationStatusField(InsertInput insertInput)
   {
      RecordAutomationStatusUpdater.setAutomationStatusInRecords(insertInput.getSession(), insertInput.getTable(), insertInput.getRecords(), AutomationStatus.PENDING_INSERT_AUTOMATIONS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QBackendModuleInterface getBackendModuleInterface(InsertInput insertInput) throws QException
   {
      ActionHelper.validateSession(insertInput);
      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(insertInput.getBackend());
      return (qModule);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendTransaction openTransaction(InsertInput insertInput) throws QException
   {
      QBackendModuleInterface qModule = getBackendModuleInterface(insertInput);
      return (qModule.getInsertInterface().openTransaction(insertInput));
   }

}
