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

package com.kingsrook.qqq.backend.core.instances;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.scripts.TestScriptActionInterface;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.tables.AssociatedScript;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTracking;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTrackingType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.QTableAutomationDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.cache.CacheOf;
import com.kingsrook.qqq.backend.core.model.metadata.tables.cache.CacheUseCase;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Class that knows how to take a look at the data in a QInstance, and report
 ** if it is all valid - e.g., non-null things are set; references line-up (e.g.,
 ** a table's backend must be a defined backend).
 **
 ** Prior to doing validation, the the QInstanceEnricher is ran over the QInstance,
 ** e.g., to fill in things that can be defaulted or assumed.  TODO let the instance
 ** customize or opt-out of Enrichment.
 **
 *******************************************************************************/
public class QInstanceValidator
{
   private static final Logger LOG = LogManager.getLogger(QInstanceValidator.class);

   private boolean printWarnings = false;

   private List<String> errors = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void validate(QInstance qInstance) throws QInstanceValidationException
   {
      if(qInstance.getHasBeenValidated())
      {
         //////////////////////////////////////////
         // don't re-validate if previously done //
         //////////////////////////////////////////
         return;
      }

      try
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // before validation, enrich the object (e.g., to fill in values that the user doesn't have to //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // TODO - possible point of customization (use a different enricher, or none, or pass it options).
         new QInstanceEnricher(qInstance).enrich();
      }
      catch(Exception e)
      {
         LOG.error("Error enriching instance prior to validation", e);
         throw (new QInstanceValidationException("Error enriching qInstance prior to validation.", e));
      }

      //////////////////////////////////////////////////////////////////////////
      // do the validation checks - a good qInstance has all conditions TRUE! //
      //////////////////////////////////////////////////////////////////////////
      try
      {
         validateBackends(qInstance);
         validateAutomationProviders(qInstance);
         validateTables(qInstance);
         validateProcesses(qInstance);
         validateReports(qInstance);
         validateApps(qInstance);
         validatePossibleValueSources(qInstance);
         validateQueuesAndProviders(qInstance);
         validateJoins(qInstance);

         validateUniqueTopLevelNames(qInstance);
      }
      catch(Exception e)
      {
         throw (new QInstanceValidationException("Error performing qInstance validation.", e));
      }

      if(!errors.isEmpty())
      {
         throw (new QInstanceValidationException(errors));
      }

      qInstance.setHasBeenValidated(new QInstanceValidationKey());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateJoins(QInstance qInstance)
   {
      qInstance.getJoins().forEach((joinName, join) ->
      {
         assertCondition(Objects.equals(joinName, join.getName()), "Inconsistent naming for join: " + joinName + "/" + join.getName() + ".");

         assertCondition(StringUtils.hasContent(join.getLeftTable()), "Missing left-table name in join: " + joinName);
         assertCondition(StringUtils.hasContent(join.getRightTable()), "Missing right-table name in join: " + joinName);
         assertCondition(join.getType() != null, "Missing type for join: " + joinName);
         assertCondition(CollectionUtils.nullSafeHasContents(join.getJoinOns()), "Missing joinOns for join: " + joinName);

         boolean leftTableExists  = assertCondition(qInstance.getTable(join.getLeftTable()) != null, "Left-table name " + join.getLeftTable() + " join " + joinName + " is not a defined table in this instance.");
         boolean rightTableExists = assertCondition(qInstance.getTable(join.getRightTable()) != null, "Right-table name " + join.getRightTable() + " join " + joinName + " is not a defined table in this instance.");

         for(JoinOn joinOn : CollectionUtils.nonNullList(join.getJoinOns()))
         {
            assertCondition(StringUtils.hasContent(joinOn.getLeftField()), "Missing left-field name in a joinOn for join: " + joinName);
            assertCondition(StringUtils.hasContent(joinOn.getRightField()), "Missing right-field name in a joinOn for join: " + joinName);

            if(leftTableExists)
            {
               assertNoException(() -> qInstance.getTable(join.getLeftTable()).getField(joinOn.getLeftField()), "Left field name in joinOn " + joinName + " is not a defined field in table " + join.getLeftTable());
            }

            if(rightTableExists)
            {
               assertNoException(() -> qInstance.getTable(join.getRightTable()).getField(joinOn.getRightField()), "Right field name in joinOn " + joinName + " is not a defined field in table " + join.getRightTable());
            }
         }
      });
   }



   /*******************************************************************************
    ** there can be some unexpected bad-times if you have a table and process, or
    ** table and app (etc) with the same name (e.g., in app tree building).  So,
    ** just go ahead and make sure those are all unique.
    *******************************************************************************/
   private void validateUniqueTopLevelNames(QInstance qInstance)
   {
      String      suffix    = " is not unique across tables, processes, and apps (but it needs to be)";
      Set<String> usedNames = new HashSet<>();
      if(qInstance.getTables() != null)
      {
         for(String tableName : qInstance.getTables().keySet())
         {
            assertCondition(!usedNames.contains(tableName), "Table name " + tableName + suffix);
            usedNames.add(tableName);
         }
      }

      if(qInstance.getProcesses() != null)
      {
         for(String processName : qInstance.getProcesses().keySet())
         {
            assertCondition(!usedNames.contains(processName), "Process name " + processName + suffix);
            usedNames.add(processName);
         }
      }

      if(qInstance.getApps() != null)
      {
         for(String appName : qInstance.getApps().keySet())
         {
            assertCondition(!usedNames.contains(appName), "App name " + appName + suffix);
            usedNames.add(appName);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateQueuesAndProviders(QInstance qInstance)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getQueueProviders()))
      {
         qInstance.getQueueProviders().forEach((name, queueProvider) ->
         {
            assertCondition(Objects.equals(name, queueProvider.getName()), "Inconsistent naming for queueProvider: " + name + "/" + queueProvider.getName() + ".");
            assertCondition(queueProvider.getType() != null, "Missing type for queueProvider: " + name);

            if(queueProvider instanceof SQSQueueProviderMetaData sqsQueueProvider)
            {
               assertCondition(StringUtils.hasContent(sqsQueueProvider.getAccessKey()), "Missing accessKey for SQSQueueProvider: " + name);
               assertCondition(StringUtils.hasContent(sqsQueueProvider.getSecretKey()), "Missing secretKey for SQSQueueProvider: " + name);
               assertCondition(StringUtils.hasContent(sqsQueueProvider.getBaseURL()), "Missing baseURL for SQSQueueProvider: " + name);
               assertCondition(StringUtils.hasContent(sqsQueueProvider.getRegion()), "Missing region for SQSQueueProvider: " + name);
            }
         });
      }

      if(CollectionUtils.nullSafeHasContents(qInstance.getQueues()))
      {
         qInstance.getQueues().forEach((name, queue) ->
         {
            assertCondition(Objects.equals(name, queue.getName()), "Inconsistent naming for queue: " + name + "/" + queue.getName() + ".");
            assertCondition(qInstance.getQueueProvider(queue.getProviderName()) != null, "Unrecognized queue providerName for queue: " + name);
            assertCondition(StringUtils.hasContent(queue.getQueueName()), "Missing queueName for queue: " + name);
            if(assertCondition(StringUtils.hasContent(queue.getProcessName()), "Missing processName for queue: " + name))
            {
               assertCondition(qInstance.getProcesses() != null && qInstance.getProcess(queue.getProcessName()) != null, "Unrecognized processName for queue: " + name);
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateBackends(QInstance qInstance)
   {
      if(assertCondition(CollectionUtils.nullSafeHasContents(qInstance.getBackends()), "At least 1 backend must be defined."))
      {
         qInstance.getBackends().forEach((backendName, backend) ->
         {
            assertCondition(Objects.equals(backendName, backend.getName()), "Inconsistent naming for backend: " + backendName + "/" + backend.getName() + ".");

            backend.performValidation(this);
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateAutomationProviders(QInstance qInstance)
   {
      if(qInstance.getAutomationProviders() != null)
      {
         qInstance.getAutomationProviders().forEach((name, automationProvider) ->
         {
            assertCondition(Objects.equals(name, automationProvider.getName()), "Inconsistent naming for automationProvider: " + name + "/" + automationProvider.getName() + ".");
            assertCondition(automationProvider.getType() != null, "Missing type for automationProvider: " + name);
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTables(QInstance qInstance)
   {
      if(assertCondition(CollectionUtils.nullSafeHasContents(qInstance.getTables()), "At least 1 table must be defined."))
      {
         qInstance.getTables().forEach((tableName, table) ->
         {
            assertCondition(Objects.equals(tableName, table.getName()), "Inconsistent naming for table: " + tableName + "/" + table.getName() + ".");
            validateAppChildHasValidParentAppName(qInstance, table);

            ////////////////////////////////////////
            // validate the backend for the table //
            ////////////////////////////////////////
            if(assertCondition(StringUtils.hasContent(table.getBackendName()), "Missing backend name for table " + tableName + "."))
            {
               if(CollectionUtils.nullSafeHasContents(qInstance.getBackends()))
               {
                  QBackendMetaData backendForTable = qInstance.getBackendForTable(tableName);
                  if(assertCondition(backendForTable != null, "Unrecognized backend " + table.getBackendName() + " for table " + tableName + "."))
                  {
                     ////////////////////////////////////////////////////////////
                     // if the backend requires primary keys, then validate it //
                     ////////////////////////////////////////////////////////////
                     if(backendForTable.requiresPrimaryKeyOnTables())
                     {
                        if(assertCondition(StringUtils.hasContent(table.getPrimaryKeyField()), "Missing primary key for table: " + tableName))
                        {
                           assertNoException(() -> table.getField(table.getPrimaryKeyField()), "Primary key for table " + tableName + " is not a recognized field on this table.");
                        }
                     }
                  }
               }
            }

            //////////////////////////////////
            // validate fields in the table //
            //////////////////////////////////
            if(assertCondition(CollectionUtils.nullSafeHasContents(table.getFields()), "At least 1 field must be defined in table " + tableName + "."))
            {
               table.getFields().forEach((fieldName, field) ->
               {
                  validateTableField(qInstance, tableName, fieldName, field);
               });
            }

            //////////////////////////////////////////
            // validate field sections in the table //
            //////////////////////////////////////////
            Set<String>   fieldNamesInSections = new HashSet<>();
            QFieldSection tier1Section         = null;
            Set<String>   usedSectionNames     = new HashSet<>();
            Set<String>   usedSectionLabels    = new HashSet<>();
            if(table.getSections() != null)
            {
               for(QFieldSection section : table.getSections())
               {
                  validateTableSection(qInstance, table, section, fieldNamesInSections);
                  if(section.getTier().equals(Tier.T1))
                  {
                     assertCondition(tier1Section == null, "Table " + tableName + " has more than 1 section listed as Tier 1");
                     tier1Section = section;
                  }

                  assertCondition(!usedSectionNames.contains(section.getName()), "Table " + tableName + " has more than 1 section named " + section.getName());
                  usedSectionNames.add(section.getName());

                  assertCondition(!usedSectionLabels.contains(section.getLabel()), "Table " + tableName + " has more than 1 section labeled " + section.getLabel());
                  usedSectionLabels.add(section.getLabel());
               }
            }

            if(CollectionUtils.nullSafeHasContents(table.getFields()))
            {
               for(String fieldName : table.getFields().keySet())
               {
                  assertCondition(fieldNamesInSections.contains(fieldName), "Table " + tableName + " field " + fieldName + " is not listed in any field sections.");
               }
            }

            ///////////////////////////////
            // validate the record label //
            ///////////////////////////////
            if(table.getRecordLabelFields() != null && table.getFields() != null)
            {
               for(String recordLabelField : table.getRecordLabelFields())
               {
                  assertCondition(table.getFields().containsKey(recordLabelField), "Table " + tableName + " record label field " + recordLabelField + " is not a field on this table.");
               }
            }

            if(table.getCustomizers() != null)
            {
               for(Map.Entry<String, QCodeReference> entry : table.getCustomizers().entrySet())
               {
                  validateTableCustomizer(tableName, entry.getKey(), entry.getValue());
               }
            }

            //////////////////////////////////////
            // validate the table's automations //
            //////////////////////////////////////
            if(table.getAutomationDetails() != null)
            {
               validateTableAutomationDetails(qInstance, table);
            }

            //////////////////////////////////////
            // validate the table's unique keys //
            //////////////////////////////////////
            if(table.getUniqueKeys() != null)
            {
               validateTableUniqueKeys(table);
            }

            /////////////////////////////////////////////
            // validate the table's associated scripts //
            /////////////////////////////////////////////
            if(table.getAssociatedScripts() != null)
            {
               validateAssociatedScripts(table);
            }

            //////////////////////
            // validate cacheOf //
            //////////////////////
            if(table.getCacheOf() != null)
            {
               validateTableCacheOf(qInstance, table);
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableField(QInstance qInstance, String tableName, String fieldName, QFieldMetaData field)
   {
      assertCondition(Objects.equals(fieldName, field.getName()),
         "Inconsistent naming in table " + tableName + " for field " + fieldName + "/" + field.getName() + ".");

      if(field.getPossibleValueSourceName() != null)
      {
         assertCondition(qInstance.getPossibleValueSource(field.getPossibleValueSourceName()) != null,
            "Unrecognized possibleValueSourceName " + field.getPossibleValueSourceName() + " in table " + tableName + " for field " + fieldName + ".");
      }

      ValueTooLongBehavior behavior = field.getBehavior(qInstance, ValueTooLongBehavior.class);
      if(behavior != null && !behavior.equals(ValueTooLongBehavior.PASS_THROUGH))
      {
         assertCondition(field.getMaxLength() != null, "Field " + fieldName + " in table " + tableName + " specifies a ValueTooLongBehavior, but not a maxLength.");
      }

      if(field.getMaxLength() != null)
      {
         assertCondition(field.getMaxLength() > 0, "Field " + fieldName + " in table " + tableName + " has an invalid maxLength (" + field.getMaxLength() + ") - must be greater than 0.");
         assertCondition(field.getType().isStringLike(), "Field " + fieldName + " in table " + tableName + " has maxLength, but is not of a supported type (" + field.getType() + ") - must be a string-like type.");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableCacheOf(QInstance qInstance, QTableMetaData table)
   {
      CacheOf cacheOf         = table.getCacheOf();
      String  prefix          = "Table " + table.getName() + " cacheOf ";
      String  sourceTableName = cacheOf.getSourceTable();
      if(assertCondition(StringUtils.hasContent(sourceTableName), prefix + "is missing a sourceTable name"))
      {
         assertCondition(qInstance.getTable(sourceTableName) != null, prefix + "is referencing an unknown sourceTable: " + sourceTableName);

         boolean hasExpirationSeconds  = cacheOf.getExpirationSeconds() != null;
         boolean hasCacheDateFieldName = StringUtils.hasContent(cacheOf.getCachedDateFieldName());
         assertCondition(hasExpirationSeconds && hasCacheDateFieldName || (!hasExpirationSeconds && !hasCacheDateFieldName), prefix + "is missing either expirationSeconds or cachedDateFieldName (must either have both, or neither.)");

         if(hasCacheDateFieldName)
         {
            assertNoException(() -> table.getField(cacheOf.getCachedDateFieldName()), prefix + "cachedDateFieldName " + cacheOf.getCachedDateFieldName() + " is an unrecognized field.");
         }

         if(assertCondition(CollectionUtils.nullSafeHasContents(cacheOf.getUseCases()), prefix + "does not have any useCases defined."))
         {
            for(CacheUseCase useCase : cacheOf.getUseCases())
            {
               assertCondition(useCase.getType() != null, prefix + "has a useCase without a type.");
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateAssociatedScripts(QTableMetaData table)
   {
      Set<String> usedFieldNames = new HashSet<>();
      for(AssociatedScript associatedScript : table.getAssociatedScripts())
      {
         if(assertCondition(StringUtils.hasContent(associatedScript.getFieldName()), "Table " + table.getName() + " has an associatedScript without a fieldName"))
         {
            assertCondition(!usedFieldNames.contains(associatedScript.getFieldName()), "Table " + table.getName() + " has more than one associatedScript specifying field: " + associatedScript.getFieldName());
            usedFieldNames.add(associatedScript.getFieldName());
            assertNoException(() -> table.getField(associatedScript.getFieldName()), "Table " + table.getName() + " has an associatedScript specifying an unrecognized field: " + associatedScript.getFieldName());
         }

         assertCondition(associatedScript.getScriptTypeId() != null, "Table " + table.getName() + " associatedScript on field " + associatedScript.getFieldName() + " is missing a scriptTypeId");
         if(associatedScript.getScriptTester() != null)
         {
            String prefix = "Table " + table.getName() + " associatedScript on field " + associatedScript.getFieldName();
            if(preAssertionsForCodeReference(associatedScript.getScriptTester(), prefix))
            {
               validateSimpleCodeReference(prefix, associatedScript.getScriptTester(), TestScriptActionInterface.class);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableUniqueKeys(QTableMetaData table)
   {
      Set<Set<String>> ukSets = new HashSet<>();
      for(UniqueKey uniqueKey : table.getUniqueKeys())
      {
         if(assertCondition(CollectionUtils.nullSafeHasContents(uniqueKey.getFieldNames()), table.getName() + " has a uniqueKey with no fields"))
         {
            Set<String> fieldNamesInThisUK = new HashSet<>();
            for(String fieldName : uniqueKey.getFieldNames())
            {
               assertNoException(() -> table.getField(fieldName), table.getName() + " has a uniqueKey with an unrecognized field name: " + fieldName);
               assertCondition(!fieldNamesInThisUK.contains(fieldName), table.getName() + " has a uniqueKey with the same field multiple times: " + fieldName);
               fieldNamesInThisUK.add(fieldName);
            }

            assertCondition(!ukSets.contains(fieldNamesInThisUK), table.getName() + " has more than one uniqueKey with the same set of fields: " + fieldNamesInThisUK);
            ukSets.add(fieldNamesInThisUK);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableAutomationDetails(QInstance qInstance, QTableMetaData table)
   {
      String tableName = table.getName();
      String prefix    = "Table " + tableName + " automationDetails ";

      QTableAutomationDetails automationDetails = table.getAutomationDetails();

      //////////////////////////////////////
      // validate the automation provider //
      //////////////////////////////////////
      String providerName = automationDetails.getProviderName();
      if(assertCondition(StringUtils.hasContent(providerName), prefix + " is missing a providerName"))
      {
         assertCondition(qInstance.getAutomationProvider(providerName) != null, " has an unrecognized providerName: " + providerName);
      }

      //////////////////////////////////
      // validate the status tracking //
      //////////////////////////////////
      AutomationStatusTracking statusTracking = automationDetails.getStatusTracking();
      if(assertCondition(statusTracking != null, prefix + "do not have statusTracking defined."))
      {
         if(assertCondition(statusTracking.getType() != null, prefix + "statusTracking is missing a type"))
         {
            if(statusTracking.getType().equals(AutomationStatusTrackingType.FIELD_IN_TABLE))
            {
               if(assertCondition(StringUtils.hasContent(statusTracking.getFieldName()), prefix + "statusTracking of type fieldInTable is missing its fieldName"))
               {
                  assertNoException(() -> table.getField(statusTracking.getFieldName()), prefix + "statusTracking field is not a defined field on this table.");
               }
            }
         }
      }

      //////////////////////////
      // validate the actions //
      //////////////////////////
      Set<String> usedNames = new HashSet<>();
      if(automationDetails.getActions() != null)
      {
         automationDetails.getActions().forEach(action ->
         {
            assertCondition(StringUtils.hasContent(action.getName()), prefix + "has an action missing a name");
            assertCondition(!usedNames.contains(action.getName()), prefix + "has more than one action named " + action.getName());
            usedNames.add(action.getName());

            String actionPrefix = prefix + "action " + action.getName() + " ";
            assertCondition(action.getTriggerEvent() != null, actionPrefix + "is missing a triggerEvent");

            /////////////////////////////////////////////////////
            // validate the code or process used by the action //
            /////////////////////////////////////////////////////
            int numberSet = 0;
            if(action.getCodeReference() != null)
            {
               numberSet++;
               if(preAssertionsForCodeReference(action.getCodeReference(), actionPrefix))
               {
                  validateSimpleCodeReference(actionPrefix + "code reference: ", action.getCodeReference(), RecordAutomationHandler.class);
               }
            }

            if(action.getProcessName() != null)
            {
               numberSet++;
               QProcessMetaData process = qInstance.getProcess(action.getProcessName());
               if(assertCondition(process != null, actionPrefix + "has an unrecognized processName: " + action.getProcessName()))
               {
                  if(process.getTableName() != null)
                  {
                     assertCondition(tableName.equals(process.getTableName()), actionPrefix + " references a process from a different table");
                  }
               }
            }

            assertCondition(numberSet != 0, actionPrefix + "is missing both a codeReference and a processName");
            assertCondition(!(numberSet > 1), actionPrefix + "has both a codeReference and a processName (which is not allowed)");

            ///////////////////////////////////////////
            // validate the filter (if there is one) //
            ///////////////////////////////////////////
            if(action.getFilter() != null && action.getFilter().getCriteria() != null)
            {
               action.getFilter().getCriteria().forEach((criteria) ->
               {
                  if(assertCondition(StringUtils.hasContent(criteria.getFieldName()), actionPrefix + "has a filter criteria without a field name"))
                  {
                     assertNoException(() -> table.getField(criteria.getFieldName()), actionPrefix + "has a filter criteria referencing an unrecognized field: " + criteria.getFieldName());
                  }

                  assertCondition(criteria.getOperator() != null, actionPrefix + "has a filter criteria without an operator");

                  // todo - validate cardinality of values...
               });
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableCustomizer(String tableName, String customizerName, QCodeReference codeReference)
   {
      String prefix = "Table " + tableName + ", customizer " + customizerName + ": ";

      if(!preAssertionsForCodeReference(codeReference, prefix))
      {
         return;
      }

      //////////////////////////////////////////////////////////////////////////////
      // make sure (at this time) that it's a java type, then do some java checks //
      //////////////////////////////////////////////////////////////////////////////
      if(assertCondition(codeReference.getCodeType().equals(QCodeType.JAVA), prefix + "Only JAVA customizers are supported at this time."))
      {
         ///////////////////////////////////////
         // make sure the class can be loaded //
         ///////////////////////////////////////
         Class<?> customizerClass = getClassForCodeReference(codeReference, prefix);
         if(customizerClass != null)
         {
            //////////////////////////////////////////////////
            // make sure the customizer can be instantiated //
            //////////////////////////////////////////////////
            Object customizerInstance = getInstanceOfCodeReference(prefix, customizerClass);

            TableCustomizers tableCustomizer = TableCustomizers.forRole(customizerName);
            if(tableCustomizer == null)
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////////
               // todo - in the future, load customizers from backend-modules (e.g., FilesystemTableCustomizers) //
               ////////////////////////////////////////////////////////////////////////////////////////////////////
               warn(prefix + "Unrecognized table customizer name (at least at backend-core level)");
            }
            else
            {
               ////////////////////////////////////////////////////////////////////////
               // make sure the customizer instance can be cast to the expected type //
               ////////////////////////////////////////////////////////////////////////
               if(customizerInstance != null && tableCustomizer.getTableCustomizer().getExpectedType() != null)
               {
                  Object castedObject = getCastedObject(prefix, tableCustomizer.getTableCustomizer().getExpectedType(), customizerInstance);

                  Consumer<Object> validationFunction = tableCustomizer.getTableCustomizer().getValidationFunction();
                  if(castedObject != null && validationFunction != null)
                  {
                     try
                     {
                        validationFunction.accept(castedObject);
                     }
                     catch(ClassCastException e)
                     {
                        errors.add(prefix + "Error validating customizer type parameters: " + e.getMessage());
                     }
                     catch(Exception e)
                     {
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // mmm, calling customizers w/ random data is expected to often throw, so, this check is iffy at best... //
                        // if we run into more trouble here, we might consider disabling the whole "validation function" check.  //
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                     }
                  }
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private <T> T getCastedObject(String prefix, Class<T> expectedType, Object customizerInstance)
   {
      T castedObject = null;
      try
      {
         castedObject = expectedType.cast(customizerInstance);
      }
      catch(ClassCastException e)
      {
         errors.add(prefix + "CodeReference is not of the expected type: " + expectedType);
      }
      return castedObject;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Object getInstanceOfCodeReference(String prefix, Class<?> clazz)
   {
      Object instance = null;
      try
      {
         instance = clazz.getConstructor().newInstance();
      }
      catch(InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e)
      {
         prefix += "Instance of " + clazz.getSimpleName() + " could not be created";
         if(Modifier.isAbstract(clazz.getModifiers()))
         {
            errors.add(prefix + " because it is abstract");
         }
         else if(Modifier.isInterface(clazz.getModifiers()))
         {
            errors.add(prefix + " because it is an interface");
         }
         else if(!Modifier.isPublic(clazz.getModifiers()))
         {
            errors.add(prefix + " because it is not public");
         }
         else
         {
            //////////////////////////////////
            // check for no-arg constructor //
            //////////////////////////////////
            boolean hasNoArgConstructor = Stream.of(clazz.getConstructors()).anyMatch(c -> c.getParameterCount() == 0);
            if(!hasNoArgConstructor)
            {
               errors.add(prefix + " because it does not have a parameterless constructor");
            }
            else
            {
               //////////////////////////////////////////
               // otherwise, just append the exception //
               //////////////////////////////////////////
               errors.add(prefix + ": " + e);
            }
         }
      }
      return instance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableSection(QInstance qInstance, QTableMetaData table, QFieldSection section, Set<String> fieldNamesInSections)
   {
      assertCondition(StringUtils.hasContent(section.getName()), "Missing a name for field section in table " + table.getName() + ".");
      assertCondition(StringUtils.hasContent(section.getLabel()), "Missing a label for field section in table " + table.getLabel() + ".");

      boolean hasFields = CollectionUtils.nullSafeHasContents(section.getFieldNames());
      boolean hasWidget = StringUtils.hasContent(section.getWidgetName());

      if(assertCondition(hasFields || hasWidget, "Table " + table.getName() + " section " + section.getName() + " does not have any fields or a widget."))
      {
         if(table.getFields() != null && hasFields)
         {
            for(String fieldName : section.getFieldNames())
            {
               assertCondition(table.getFields().containsKey(fieldName), "Table " + table.getName() + " section " + section.getName() + " specifies fieldName " + fieldName + ", which is not a field on this table.");
               assertCondition(!fieldNamesInSections.contains(fieldName), "Table " + table.getName() + " has field " + fieldName + " listed more than once in its field sections.");

               fieldNamesInSections.add(fieldName);
            }
         }
         else if(hasWidget)
         {
            assertCondition(qInstance.getWidget(section.getWidgetName()) != null, "Table " + table.getName() + " section " + section.getName() + " specifies widget " + section.getWidgetName() + ", which is not a widget in this instance.");
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateAppSection(QAppMetaData app, QAppSection section, Set<String> childNamesInSections)
   {
      assertCondition(StringUtils.hasContent(section.getName()), "Missing a name for a section in app " + app.getName() + ".");
      assertCondition(StringUtils.hasContent(section.getLabel()), "Missing a label for a section in app " + app.getLabel() + ".");
      boolean hasTables    = CollectionUtils.nullSafeHasContents(section.getTables());
      boolean hasProcesses = CollectionUtils.nullSafeHasContents(section.getProcesses());
      boolean hasReports   = CollectionUtils.nullSafeHasContents(section.getReports());
      if(assertCondition(hasTables || hasProcesses || hasReports, "App " + app.getName() + " section " + section.getName() + " does not have any children."))
      {
         if(hasTables)
         {
            for(String tableName : section.getTables())
            {
               assertCondition(app.getChildren().stream().anyMatch(c -> c.getName().equals(tableName)), "App " + app.getName() + " section " + section.getName() + " specifies table " + tableName + ", which is not a child of this app.");
               assertCondition(!childNamesInSections.contains(tableName), "App " + app.getName() + " has table " + tableName + " listed more than once in its sections.");

               childNamesInSections.add(tableName);
            }
         }
         if(hasProcesses)
         {
            for(String processName : section.getProcesses())
            {
               assertCondition(app.getChildren().stream().anyMatch(c -> c.getName().equals(processName)), "App " + app.getName() + " section " + section.getName() + " specifies process " + processName + ", which is not a child of this app.");
               assertCondition(!childNamesInSections.contains(processName), "App " + app.getName() + " has process " + processName + " listed more than once in its sections.");

               childNamesInSections.add(processName);
            }
         }
         if(hasReports)
         {
            for(String reportName : section.getReports())
            {
               assertCondition(app.getChildren().stream().anyMatch(c -> c.getName().equals(reportName)), "App " + app.getName() + " section " + section.getName() + " specifies report " + reportName + ", which is not a child of this app.");
               assertCondition(!childNamesInSections.contains(reportName), "App " + app.getName() + " has report " + reportName + " listed more than once in its sections.");

               childNamesInSections.add(reportName);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateProcesses(QInstance qInstance)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getProcesses()))
      {
         qInstance.getProcesses().forEach((processName, process) ->
         {
            assertCondition(Objects.equals(processName, process.getName()), "Inconsistent naming for process: " + processName + "/" + process.getName() + ".");

            validateAppChildHasValidParentAppName(qInstance, process);

            /////////////////////////////////////////////
            // validate the table name for the process //
            /////////////////////////////////////////////
            if(process.getTableName() != null)
            {
               assertCondition(qInstance.getTable(process.getTableName()) != null, "Unrecognized table " + process.getTableName() + " for process " + processName + ".");
            }

            ///////////////////////////////////
            // validate steps in the process //
            ///////////////////////////////////
            if(assertCondition(CollectionUtils.nullSafeHasContents(process.getStepList()), "At least 1 step must be defined in process " + processName + "."))
            {
               int index = 0;
               for(QStepMetaData step : process.getStepList())
               {
                  assertCondition(StringUtils.hasContent(step.getName()), "Missing name for a step at index " + index + " in process " + processName);
                  index++;

                  ////////////////////////////////////////////
                  // validate instantiation of step classes //
                  ////////////////////////////////////////////
                  if(step instanceof QBackendStepMetaData backendStepMetaData)
                  {
                     if(backendStepMetaData.getInputMetaData() != null && CollectionUtils.nullSafeHasContents(backendStepMetaData.getInputMetaData().getFieldList()))
                     {
                        for(QFieldMetaData fieldMetaData : backendStepMetaData.getInputMetaData().getFieldList())
                        {
                           if(fieldMetaData.getDefaultValue() != null && fieldMetaData.getDefaultValue() instanceof QCodeReference codeReference)
                           {
                              validateSimpleCodeReference("Process " + processName + " backend step code reference: ", codeReference, BackendStep.class);
                           }
                        }
                     }
                  }
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateReports(QInstance qInstance)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getReports()))
      {
         qInstance.getReports().forEach((reportName, report) ->
         {
            assertCondition(Objects.equals(reportName, report.getName()), "Inconsistent naming for report: " + reportName + "/" + report.getName() + ".");
            validateAppChildHasValidParentAppName(qInstance, report);

            ////////////////////////////////////////
            // validate dataSources in the report //
            ////////////////////////////////////////
            Set<String> usedDataSourceNames = new HashSet<>();
            if(assertCondition(CollectionUtils.nullSafeHasContents(report.getDataSources()), "At least 1 data source must be defined in report " + reportName + "."))
            {
               int index = 0;
               for(QReportDataSource dataSource : report.getDataSources())
               {
                  assertCondition(StringUtils.hasContent(dataSource.getName()), "Missing name for a dataSource at index " + index + " in report " + reportName);
                  index++;

                  assertCondition(!usedDataSourceNames.contains(dataSource.getName()), "More than one dataSource with name " + dataSource.getName() + " in report " + reportName);
                  usedDataSourceNames.add(dataSource.getName());

                  String dataSourceErrorPrefix = "Report " + reportName + " data source " + dataSource.getName() + " ";

                  if(StringUtils.hasContent(dataSource.getSourceTable()))
                  {
                     assertCondition(dataSource.getStaticDataSupplier() == null, dataSourceErrorPrefix + "has both a sourceTable and a staticDataSupplier (exactly 1 is required).");
                     if(assertCondition(qInstance.getTable(dataSource.getSourceTable()) != null, dataSourceErrorPrefix + "source table " + dataSource.getSourceTable() + " is not a table in this instance."))
                     {
                        if(dataSource.getQueryFilter() != null)
                        {
                           validateQueryFilter("In " + dataSourceErrorPrefix + "query filter - ", qInstance.getTable(dataSource.getSourceTable()), dataSource.getQueryFilter());
                        }
                     }
                  }
                  else if(dataSource.getStaticDataSupplier() != null)
                  {
                     validateSimpleCodeReference(dataSourceErrorPrefix, dataSource.getStaticDataSupplier(), Supplier.class);
                  }
                  else
                  {
                     errors.add(dataSourceErrorPrefix + "does not have a sourceTable or a staticDataSupplier (exactly 1 is required).");
                  }
               }
            }

            ////////////////////////////////////////
            // validate dataSources in the report //
            ////////////////////////////////////////
            if(assertCondition(CollectionUtils.nullSafeHasContents(report.getViews()), "At least 1 view must be defined in report " + reportName + "."))
            {
               int         index         = 0;
               Set<String> usedViewNames = new HashSet<>();
               for(QReportView view : report.getViews())
               {
                  assertCondition(StringUtils.hasContent(view.getName()), "Missing name for a view at index " + index + " in report " + reportName);
                  index++;

                  assertCondition(!usedViewNames.contains(view.getName()), "More than one view with name " + view.getName() + " in report " + reportName);
                  usedViewNames.add(view.getName());

                  String viewErrorPrefix = "Report " + reportName + " view " + view.getName() + " ";
                  assertCondition(view.getType() != null, viewErrorPrefix + " is missing its type.");
                  if(assertCondition(StringUtils.hasContent(view.getDataSourceName()), viewErrorPrefix + " is missing a dataSourceName"))
                  {
                     assertCondition(usedDataSourceNames.contains(view.getDataSourceName()), viewErrorPrefix + " has an unrecognized dataSourceName: " + view.getDataSourceName());
                  }

                  if(StringUtils.hasContent(view.getVarianceDataSourceName()))
                  {
                     assertCondition(usedDataSourceNames.contains(view.getVarianceDataSourceName()), viewErrorPrefix + " has an unrecognized varianceDataSourceName: " + view.getVarianceDataSourceName());
                  }

                  // actually, this is okay if there's a customizer, so...
                  assertCondition(CollectionUtils.nullSafeHasContents(view.getColumns()), viewErrorPrefix + " does not have any columns.");

                  // todo - all these too...
                  // view.getPivotFields();
                  // view.getViewCustomizer(); // validate code ref
                  // view.getRecordTransformStep(); // validate code ref
                  // view.getOrderByFields(); // make sure valid field names?
                  // view.getIncludePivotSubTotals(); // only for pivot type
                  // view.getTitleFormat(); view.getTitleFields(); // validate these match?
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateQueryFilter(String context, QTableMetaData table, QQueryFilter queryFilter)
   {
      for(QFilterCriteria criterion : CollectionUtils.nonNullList(queryFilter.getCriteria()))
      {
         if(assertCondition(StringUtils.hasContent(criterion.getFieldName()), context + "Missing fieldName for a criteria"))
         {
            assertNoException(() -> table.getField(criterion.getFieldName()), context + "Criteria fieldName " + criterion.getFieldName() + " is not a field in this table.");
         }
         assertCondition(criterion.getOperator() != null, context + "Missing operator for a criteria on fieldName " + criterion.getFieldName());
         assertCondition(criterion.getValues() != null, context + "Missing values for a criteria on fieldName " + criterion.getFieldName()); // todo - what about ops w/ no value (BLANK)
      }

      for(QFilterOrderBy orderBy : CollectionUtils.nonNullList(queryFilter.getOrderBys()))
      {
         if(assertCondition(StringUtils.hasContent(orderBy.getFieldName()), context + "Missing fieldName for an orderBy"))
         {
            assertNoException(() -> table.getField(orderBy.getFieldName()), context + "OrderBy fieldName " + orderBy.getFieldName() + " is not a field in this table.");
         }
      }

      for(QQueryFilter subFilter : CollectionUtils.nonNullList(queryFilter.getSubFilters()))
      {
         validateQueryFilter(context, table, subFilter);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateApps(QInstance qInstance)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getApps()))
      {
         qInstance.getApps().forEach((appName, app) ->
         {
            assertCondition(Objects.equals(appName, app.getName()), "Inconsistent naming for app: " + appName + "/" + app.getName() + ".");

            validateAppChildHasValidParentAppName(qInstance, app);

            Set<String> appsVisited = new HashSet<>();
            visitAppCheckingForCycles(app, appsVisited);

            if(app.getChildren() != null)
            {
               Set<String> childNames = new HashSet<>();
               for(QAppChildMetaData child : app.getChildren())
               {
                  assertCondition(Objects.equals(appName, child.getParentAppName()), "Child " + child.getName() + " of app " + appName + " does not have its parent app properly set.");
                  assertCondition(!childNames.contains(child.getName()), "App " + appName + " contains more than one child named " + child.getName());
                  childNames.add(child.getName());
               }
            }

            //////////////////////////////////////////
            // validate field sections in the table //
            //////////////////////////////////////////
            Set<String> childNamesInSections = new HashSet<>();
            if(app.getSections() != null)
            {
               for(QAppSection section : app.getSections())
               {
                  validateAppSection(app, section, childNamesInSections);
               }
            }

            if(CollectionUtils.nullSafeHasContents(app.getChildren()))
            {
               for(QAppChildMetaData child : app.getChildren())
               {
                  if(!child.getClass().equals(QAppMetaData.class))
                  {
                     assertCondition(childNamesInSections.contains(child.getName()), "App " + appName + " child " + child.getName() + " is not listed in any app sections.");
                  }
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validatePossibleValueSources(QInstance qInstance)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getPossibleValueSources()))
      {
         qInstance.getPossibleValueSources().forEach((pvsName, possibleValueSource) ->
         {
            assertCondition(Objects.equals(pvsName, possibleValueSource.getName()), "Inconsistent naming for possibleValueSource: " + pvsName + "/" + possibleValueSource.getName() + ".");
            if(assertCondition(possibleValueSource.getType() != null, "Missing type for possibleValueSource: " + pvsName))
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////
               // assert about fields that should and should not be set, based on possible value source type //
               // do additional type-specific validations as well                                            //
               ////////////////////////////////////////////////////////////////////////////////////////////////
               switch(possibleValueSource.getType())
               {
                  case ENUM ->
                  {
                     assertCondition(!StringUtils.hasContent(possibleValueSource.getTableName()), "enum-type possibleValueSource " + pvsName + " should not have a tableName.");
                     assertCondition(!CollectionUtils.nullSafeHasContents(possibleValueSource.getSearchFields()), "enum-type possibleValueSource " + pvsName + " should not have searchFields.");
                     assertCondition(!CollectionUtils.nullSafeHasContents(possibleValueSource.getOrderByFields()), "enum-type possibleValueSource " + pvsName + " should not have orderByFields.");
                     assertCondition(possibleValueSource.getCustomCodeReference() == null, "enum-type possibleValueSource " + pvsName + " should not have a customCodeReference.");

                     assertCondition(CollectionUtils.nullSafeHasContents(possibleValueSource.getEnumValues()), "enum-type possibleValueSource " + pvsName + " is missing enum values");
                  }
                  case TABLE ->
                  {
                     assertCondition(CollectionUtils.nullSafeIsEmpty(possibleValueSource.getEnumValues()), "table-type possibleValueSource " + pvsName + " should not have enum values.");
                     assertCondition(possibleValueSource.getCustomCodeReference() == null, "table-type possibleValueSource " + pvsName + " should not have a customCodeReference.");

                     QTableMetaData tableMetaData = null;
                     if(assertCondition(StringUtils.hasContent(possibleValueSource.getTableName()), "table-type possibleValueSource " + pvsName + " is missing a tableName."))
                     {
                        tableMetaData = qInstance.getTable(possibleValueSource.getTableName());
                        assertCondition(tableMetaData != null, "Unrecognized table " + possibleValueSource.getTableName() + " for possibleValueSource " + pvsName + ".");
                     }

                     if(assertCondition(CollectionUtils.nullSafeHasContents(possibleValueSource.getSearchFields()), "table-type possibleValueSource " + pvsName + " is missing searchFields."))
                     {
                        if(tableMetaData != null)
                        {
                           QTableMetaData finalTableMetaData = tableMetaData;
                           for(String searchField : possibleValueSource.getSearchFields())
                           {
                              assertNoException(() -> finalTableMetaData.getField(searchField), "possibleValueSource " + pvsName + " has an unrecognized searchField: " + searchField);
                           }
                        }
                     }

                     if(assertCondition(CollectionUtils.nullSafeHasContents(possibleValueSource.getOrderByFields()), "table-type possibleValueSource " + pvsName + " is missing orderByFields."))
                     {
                        if(tableMetaData != null)
                        {
                           QTableMetaData finalTableMetaData = tableMetaData;

                           for(QFilterOrderBy orderByField : possibleValueSource.getOrderByFields())
                           {
                              assertNoException(() -> finalTableMetaData.getField(orderByField.getFieldName()), "possibleValueSource " + pvsName + " has an unrecognized orderByField: " + orderByField.getFieldName());
                           }
                        }
                     }
                  }
                  case CUSTOM ->
                  {
                     assertCondition(CollectionUtils.nullSafeIsEmpty(possibleValueSource.getEnumValues()), "custom-type possibleValueSource " + pvsName + " should not have enum values.");
                     assertCondition(!StringUtils.hasContent(possibleValueSource.getTableName()), "custom-type possibleValueSource " + pvsName + " should not have a tableName.");
                     assertCondition(!CollectionUtils.nullSafeHasContents(possibleValueSource.getSearchFields()), "custom-type possibleValueSource " + pvsName + " should not have searchFields.");
                     assertCondition(!CollectionUtils.nullSafeHasContents(possibleValueSource.getOrderByFields()), "custom-type possibleValueSource " + pvsName + " should not have orderByFields.");

                     if(assertCondition(possibleValueSource.getCustomCodeReference() != null, "custom-type possibleValueSource " + pvsName + " is missing a customCodeReference."))
                     {
                        assertCondition(QCodeUsage.POSSIBLE_VALUE_PROVIDER.equals(possibleValueSource.getCustomCodeReference().getCodeUsage()), "customCodeReference for possibleValueSource " + pvsName + " is not a possibleValueProvider.");
                        validateSimpleCodeReference("PossibleValueSource " + pvsName + " custom code reference: ", possibleValueSource.getCustomCodeReference(), QCustomPossibleValueProvider.class);
                     }
                  }
                  default -> errors.add("Unexpected possibleValueSource type: " + possibleValueSource.getType());
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateSimpleCodeReference(String prefix, QCodeReference codeReference, Class<?> expectedClass)
   {
      if(!preAssertionsForCodeReference(codeReference, prefix))
      {
         return;
      }

      //////////////////////////////////////////////////////////////////////////////
      // make sure (at this time) that it's a java type, then do some java checks //
      //////////////////////////////////////////////////////////////////////////////
      if(assertCondition(codeReference.getCodeType().equals(QCodeType.JAVA), prefix + "Only JAVA code references are supported at this time."))
      {
         ///////////////////////////////////////
         // make sure the class can be loaded //
         ///////////////////////////////////////
         Class<?> clazz = getClassForCodeReference(codeReference, prefix);
         if(clazz != null)
         {
            //////////////////////////////////////////////////
            // make sure the customizer can be instantiated //
            //////////////////////////////////////////////////
            Object classInstance = getInstanceOfCodeReference(prefix, clazz);

            ////////////////////////////////////////////////////////////////////////
            // make sure the customizer instance can be cast to the expected type //
            ////////////////////////////////////////////////////////////////////////
            if(classInstance != null)
            {
               getCastedObject(prefix, expectedClass, classInstance);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Class<?> getClassForCodeReference(QCodeReference codeReference, String prefix)
   {
      Class<?> clazz = null;
      try
      {
         clazz = Class.forName(codeReference.getName());
      }
      catch(ClassNotFoundException e)
      {
         errors.add(prefix + "Class for " + codeReference.getName() + " could not be found.");
      }
      return clazz;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean preAssertionsForCodeReference(QCodeReference codeReference, String prefix)
   {
      boolean okay = true;
      if(!assertCondition(StringUtils.hasContent(codeReference.getName()), prefix + " is missing a code reference name"))
      {
         okay = false;
      }

      if(!assertCondition(codeReference.getCodeType() != null, prefix + " is missing a code type"))
      {
         okay = false;
      }

      return (okay);
   }



   /*******************************************************************************
    ** Check if an app's child list can recursively be traversed without finding a
    ** duplicate, which would indicate a cycle (e.g., an error)
    *******************************************************************************/
   private void visitAppCheckingForCycles(QAppMetaData app, Set<String> appsVisited)
   {
      if(assertCondition(!appsVisited.contains(app.getName()), "Circular app reference detected, involving " + app.getName()))
      {
         appsVisited.add(app.getName());
         if(app.getChildren() != null)
         {
            for(QAppChildMetaData child : app.getChildren())
            {
               if(child instanceof QAppMetaData childApp)
               {
                  visitAppCheckingForCycles(childApp, appsVisited);
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateAppChildHasValidParentAppName(QInstance qInstance, QAppChildMetaData appChild)
   {
      if(appChild.getParentAppName() != null)
      {
         assertCondition(qInstance.getApp(appChild.getParentAppName()) != null, "Unrecognized parent app " + appChild.getParentAppName() + " for " + appChild.getName() + ".");
      }
   }



   /*******************************************************************************
    ** For the given input condition, if it's true, then we're all good (and return true).
    ** But if it's false, add the provided message to the list of errors (and return false,
    ** e.g., in case you need to stop evaluating rules to avoid exceptions).
    *******************************************************************************/
   public boolean assertCondition(boolean condition, String message)
   {
      if(!condition)
      {
         errors.add(message);
      }

      return (condition);
   }



   /*******************************************************************************
    ** For the given lambda, if it doesn't throw an exception, then we're all good (and return true).
    ** But if it throws, add the provided message to the list of errors (and return false,
    ** e.g., in case you need to stop evaluating rules to avoid exceptions).
    *******************************************************************************/
   private boolean assertNoException(UnsafeLambda unsafeLambda, String message)
   {
      try
      {
         unsafeLambda.run();
         return (true);
      }
      catch(Exception e)
      {
         errors.add(message);
         return (false);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @FunctionalInterface
   interface UnsafeLambda
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      void run() throws Exception;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void warn(String message)
   {
      if(printWarnings)
      {
         LOG.info("Validation warning: " + message);
      }
   }



   /*******************************************************************************
    ** Getter for errors
    **
    *******************************************************************************/
   public List<String> getErrors()
   {
      return errors;
   }
}
