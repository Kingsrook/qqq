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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
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
         new QInstanceEnricher().enrich(qInstance);
      }
      catch(Exception e)
      {
         throw (new QInstanceValidationException("Error enriching qInstance prior to validation.", e));
      }

      //////////////////////////////////////////////////////////////////////////
      // do the validation checks - a good qInstance has all conditions TRUE! //
      //////////////////////////////////////////////////////////////////////////
      List<String> errors = new ArrayList<>();
      try
      {
         validateBackends(qInstance, errors);
         validateTables(qInstance, errors);
         validateProcesses(qInstance, errors);
         validateApps(qInstance, errors);
         validatePossibleValueSources(qInstance, errors);
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
   private void validateBackends(QInstance qInstance, List<String> errors)
   {
      if(assertCondition(errors, CollectionUtils.nullSafeHasContents(qInstance.getBackends()), "At least 1 backend must be defined."))
      {
         qInstance.getBackends().forEach((backendName, backend) ->
         {
            assertCondition(errors, Objects.equals(backendName, backend.getName()), "Inconsistent naming for backend: " + backendName + "/" + backend.getName() + ".");
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTables(QInstance qInstance, List<String> errors)
   {
      if(assertCondition(errors, CollectionUtils.nullSafeHasContents(qInstance.getTables()),
         "At least 1 table must be defined."))
      {
         qInstance.getTables().forEach((tableName, table) ->
         {
            assertCondition(errors, Objects.equals(tableName, table.getName()), "Inconsistent naming for table: " + tableName + "/" + table.getName() + ".");

            validateAppChildHasValidParentAppName(qInstance, errors, table);

            ////////////////////////////////////////
            // validate the backend for the table //
            ////////////////////////////////////////
            if(assertCondition(errors, StringUtils.hasContent(table.getBackendName()),
               "Missing backend name for table " + tableName + "."))
            {
               if(CollectionUtils.nullSafeHasContents(qInstance.getBackends()))
               {
                  assertCondition(errors, qInstance.getBackendForTable(tableName) != null, "Unrecognized backend " + table.getBackendName() + " for table " + tableName + ".");
               }
            }

            //////////////////////////////////
            // validate fields in the table //
            //////////////////////////////////
            if(assertCondition(errors, CollectionUtils.nullSafeHasContents(table.getFields()), "At least 1 field must be defined in table " + tableName + "."))
            {
               table.getFields().forEach((fieldName, field) ->
               {
                  assertCondition(errors, Objects.equals(fieldName, field.getName()),
                     "Inconsistent naming in table " + tableName + " for field " + fieldName + "/" + field.getName() + ".");

                  if(field.getPossibleValueSourceName() != null)
                  {
                     assertCondition(errors, qInstance.getPossibleValueSource(field.getPossibleValueSourceName()) != null,
                        "Unrecognized possibleValueSourceName " + field.getPossibleValueSourceName() + " in table " + tableName + " for field " + fieldName + ".");
                  }
               });
            }

            //////////////////////////////////////////
            // validate field sections in the table //
            //////////////////////////////////////////
            Set<String>   fieldNamesInSections = new HashSet<>();
            QFieldSection tier1Section         = null;
            if(table.getSections() != null)
            {
               for(QFieldSection section : table.getSections())
               {
                  validateSection(errors, table, section, fieldNamesInSections);
                  if(section.getTier().equals(Tier.T1))
                  {
                     assertCondition(errors, tier1Section == null, "Table " + tableName + " has more than 1 section listed as Tier 1");
                     tier1Section = section;
                  }
               }
            }

            if(CollectionUtils.nullSafeHasContents(table.getFields()))
            {
               for(String fieldName : table.getFields().keySet())
               {
                  assertCondition(errors, fieldNamesInSections.contains(fieldName), "Table " + tableName + " field " + fieldName + " is not listed in any field sections.");
               }
            }

            ///////////////////////////////
            // validate the record label //
            ///////////////////////////////
            if(table.getRecordLabelFields() != null)
            {
               for(String recordLabelField : table.getRecordLabelFields())
               {
                  assertCondition(errors, table.getFields().containsKey(recordLabelField), "Table " + tableName + " record label field " + recordLabelField + " is not a field on this table.");
               }
            }

            if(table.getCustomizers() != null)
            {
               for(Map.Entry<String, QCodeReference> entry : table.getCustomizers().entrySet())
               {
                  validateTableCustomizer(errors, tableName, entry.getKey(), entry.getValue());
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateTableCustomizer(List<String> errors, String tableName, String customizerName, QCodeReference codeReference)
   {
      String prefix = "Table " + tableName + ", customizer " + customizerName + ": ";

      if(!preAssertionsForCodeReference(errors, codeReference, prefix))
      {
         return;
      }

      //////////////////////////////////////////////////////////////////////////////
      // make sure (at this time) that it's a java type, then do some java checks //
      //////////////////////////////////////////////////////////////////////////////
      if(assertCondition(errors, codeReference.getCodeType().equals(QCodeType.JAVA), prefix + "Only JAVA customizers are supported at this time."))
      {
         ///////////////////////////////////////
         // make sure the class can be loaded //
         ///////////////////////////////////////
         Class<?> customizerClass = getClassForCodeReference(errors, codeReference, prefix);
         if(customizerClass != null)
         {
            //////////////////////////////////////////////////
            // make sure the customizer can be instantiated //
            //////////////////////////////////////////////////
            Object customizerInstance = getInstanceOfCodeReference(errors, prefix, customizerClass);

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
                  Object castedObject = getCastedObject(errors, prefix, tableCustomizer.getTableCustomizer().getExpectedType(), customizerInstance);

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
   private <T> T getCastedObject(List<String> errors, String prefix, Class<T> expectedType, Object customizerInstance)
   {
      T castedObject = null;
      try
      {
         castedObject = expectedType.cast(customizerInstance);
      }
      catch(ClassCastException e)
      {
         errors.add(prefix + "CodeReference could not be casted to the expected type: " + expectedType);
      }
      return castedObject;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Object getInstanceOfCodeReference(List<String> errors, String prefix, Class<?> customizerClass)
   {
      Object customizerInstance = null;
      try
      {
         customizerInstance = customizerClass.getConstructor().newInstance();
      }
      catch(InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e)
      {
         errors.add(prefix + "Instance of CodeReference could not be created: " + e);
      }
      return customizerInstance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateSection(List<String> errors, QTableMetaData table, QFieldSection section, Set<String> fieldNamesInSections)
   {
      assertCondition(errors, StringUtils.hasContent(section.getName()), "Missing a name for field section in table " + table.getName() + ".");
      assertCondition(errors, StringUtils.hasContent(section.getLabel()), "Missing a label for field section in table " + table.getLabel() + ".");
      if(assertCondition(errors, CollectionUtils.nullSafeHasContents(section.getFieldNames()), "Table " + table.getName() + " section " + section.getName() + " does not have any fields."))
      {
         if(table.getFields() != null)
         {
            for(String fieldName : section.getFieldNames())
            {
               assertCondition(errors, table.getFields().containsKey(fieldName), "Table " + table.getName() + " section " + section.getName() + " specifies fieldName " + fieldName + ", which is not a field on this table.");
               assertCondition(errors, !fieldNamesInSections.contains(fieldName), "Table " + table.getName() + " has field " + fieldName + " listed more than once in its field sections.");

               fieldNamesInSections.add(fieldName);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateProcesses(QInstance qInstance, List<String> errors)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getProcesses()))
      {
         qInstance.getProcesses().forEach((processName, process) ->
         {
            assertCondition(errors, Objects.equals(processName, process.getName()), "Inconsistent naming for process: " + processName + "/" + process.getName() + ".");

            validateAppChildHasValidParentAppName(qInstance, errors, process);

            /////////////////////////////////////////////
            // validate the table name for the process //
            /////////////////////////////////////////////
            if(process.getTableName() != null)
            {
               assertCondition(errors, qInstance.getTable(process.getTableName()) != null, "Unrecognized table " + process.getTableName() + " for process " + processName + ".");
            }

            ///////////////////////////////////
            // validate steps in the process //
            ///////////////////////////////////
            if(assertCondition(errors, CollectionUtils.nullSafeHasContents(process.getStepList()), "At least 1 step must be defined in process " + processName + "."))
            {
               int index = 0;
               for(QStepMetaData step : process.getStepList())
               {
                  assertCondition(errors, StringUtils.hasContent(step.getName()), "Missing name for a step at index " + index + " in process " + processName);
                  index++;
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateApps(QInstance qInstance, List<String> errors)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getApps()))
      {
         qInstance.getApps().forEach((appName, app) ->
         {
            assertCondition(errors, Objects.equals(appName, app.getName()), "Inconsistent naming for app: " + appName + "/" + app.getName() + ".");

            validateAppChildHasValidParentAppName(qInstance, errors, app);

            Set<String> appsVisited = new HashSet<>();
            visitAppCheckingForCycles(app, appsVisited, errors);

            if(app.getChildren() != null)
            {
               Set<String> childNames = new HashSet<>();
               for(QAppChildMetaData child : app.getChildren())
               {
                  assertCondition(errors, Objects.equals(appName, child.getParentAppName()), "Child " + child.getName() + " of app " + appName + " does not have its parent app properly set.");
                  assertCondition(errors, !childNames.contains(child.getName()), "App " + appName + " contains more than one child named " + child.getName());
                  childNames.add(child.getName());
               }
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validatePossibleValueSources(QInstance qInstance, List<String> errors)
   {
      if(CollectionUtils.nullSafeHasContents(qInstance.getPossibleValueSources()))
      {
         qInstance.getPossibleValueSources().forEach((pvsName, possibleValueSource) ->
         {
            assertCondition(errors, Objects.equals(pvsName, possibleValueSource.getName()), "Inconsistent naming for possibleValueSource: " + pvsName + "/" + possibleValueSource.getName() + ".");
            if(assertCondition(errors, possibleValueSource.getType() != null, "Missing type for possibleValueSource: " + pvsName))
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////
               // assert about fields that should and should not be set, based on possible value source type //
               // do additional type-specific validations as well                                            //
               ////////////////////////////////////////////////////////////////////////////////////////////////
               switch(possibleValueSource.getType())
               {
                  case ENUM ->
                  {
                     assertCondition(errors, !StringUtils.hasContent(possibleValueSource.getTableName()), "enum-type possibleValueSource " + pvsName + " should not have a tableName.");
                     assertCondition(errors, possibleValueSource.getCustomCodeReference() == null, "enum-type possibleValueSource " + pvsName + " should not have a customCodeReference.");

                     assertCondition(errors, CollectionUtils.nullSafeHasContents(possibleValueSource.getEnumValues()), "enum-type possibleValueSource " + pvsName + " is missing enum values");
                  }
                  case TABLE ->
                  {
                     assertCondition(errors, CollectionUtils.nullSafeIsEmpty(possibleValueSource.getEnumValues()), "table-type possibleValueSource " + pvsName + " should not have enum values.");
                     assertCondition(errors, possibleValueSource.getCustomCodeReference() == null, "table-type possibleValueSource " + pvsName + " should not have a customCodeReference.");

                     if(assertCondition(errors, StringUtils.hasContent(possibleValueSource.getTableName()), "table-type possibleValueSource " + pvsName + " is missing a tableName."))
                     {
                        assertCondition(errors, qInstance.getTable(possibleValueSource.getTableName()) != null, "Unrecognized table " + possibleValueSource.getTableName() + " for possibleValueSource " + pvsName + ".");
                     }
                  }
                  case CUSTOM ->
                  {
                     assertCondition(errors, CollectionUtils.nullSafeIsEmpty(possibleValueSource.getEnumValues()), "custom-type possibleValueSource " + pvsName + " should not have enum values.");
                     assertCondition(errors, !StringUtils.hasContent(possibleValueSource.getTableName()), "custom-type possibleValueSource " + pvsName + " should not have a tableName.");

                     if(assertCondition(errors, possibleValueSource.getCustomCodeReference() != null, "custom-type possibleValueSource " + pvsName + " is missing a customCodeReference."))
                     {
                        assertCondition(errors, QCodeUsage.POSSIBLE_VALUE_PROVIDER.equals(possibleValueSource.getCustomCodeReference().getCodeUsage()), "customCodeReference for possibleValueSource " + pvsName + " is not a possibleValueProvider.");
                        validateCustomPossibleValueSourceCode(errors, pvsName, possibleValueSource.getCustomCodeReference());
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
   private void validateCustomPossibleValueSourceCode(List<String> errors, String pvsName, QCodeReference codeReference)
   {
      String prefix = "PossibleValueSource " + pvsName + " custom code reference: ";

      if(!preAssertionsForCodeReference(errors, codeReference, prefix))
      {
         return;
      }

      //////////////////////////////////////////////////////////////////////////////
      // make sure (at this time) that it's a java type, then do some java checks //
      //////////////////////////////////////////////////////////////////////////////
      if(assertCondition(errors, codeReference.getCodeType().equals(QCodeType.JAVA), prefix + "Only JAVA customizers are supported at this time."))
      {
         ///////////////////////////////////////
         // make sure the class can be loaded //
         ///////////////////////////////////////
         Class<?> customizerClass = getClassForCodeReference(errors, codeReference, prefix);
         if(customizerClass != null)
         {
            //////////////////////////////////////////////////
            // make sure the customizer can be instantiated //
            //////////////////////////////////////////////////
            Object customizerInstance = getInstanceOfCodeReference(errors, prefix, customizerClass);

            ////////////////////////////////////////////////////////////////////////
            // make sure the customizer instance can be cast to the expected type //
            ////////////////////////////////////////////////////////////////////////
            if(customizerInstance != null)
            {
               getCastedObject(errors, prefix, QCustomPossibleValueProvider.class, customizerInstance);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Class<?> getClassForCodeReference(List<String> errors, QCodeReference codeReference, String prefix)
   {
      Class<?> customizerClass = null;
      try
      {
         customizerClass = Class.forName(codeReference.getName());
      }
      catch(ClassNotFoundException e)
      {
         errors.add(prefix + "Class for CodeReference could not be found.");
      }
      return customizerClass;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean preAssertionsForCodeReference(List<String> errors, QCodeReference codeReference, String prefix)
   {
      boolean okay = true;
      if(!assertCondition(errors, StringUtils.hasContent(codeReference.getName()), prefix + " is missing a code reference name"))
      {
         okay = false;
      }

      if(!assertCondition(errors, codeReference.getCodeType() != null, prefix + " is missing a code type"))
      {
         okay = false;
      }

      return (okay);
   }



   /*******************************************************************************
    ** Check if an app's child list can recursively be traversed without finding a
    ** duplicate, which would indicate a cycle (e.g., an error)
    *******************************************************************************/
   private void visitAppCheckingForCycles(QAppMetaData app, Set<String> appsVisited, List<String> errors)
   {
      if(assertCondition(errors, !appsVisited.contains(app.getName()), "Circular app reference detected, involving " + app.getName()))
      {
         appsVisited.add(app.getName());
         if(app.getChildren() != null)
         {
            for(QAppChildMetaData child : app.getChildren())
            {
               if(child instanceof QAppMetaData childApp)
               {
                  visitAppCheckingForCycles(childApp, appsVisited, errors);
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateAppChildHasValidParentAppName(QInstance qInstance, List<String> errors, QAppChildMetaData appChild)
   {
      if(appChild.getParentAppName() != null)
      {
         assertCondition(errors, qInstance.getApp(appChild.getParentAppName()) != null, "Unrecognized parent app " + appChild.getParentAppName() + " for " + appChild.getName() + ".");
      }
   }



   /*******************************************************************************
    ** For the given input condition, if it's true, then we're all good (and return true).
    ** But if it's false, add the provided message to the list of errors (and return false,
    ** e.g., in case you need to stop evaluating rules to avoid exceptions).
    *******************************************************************************/
   private boolean assertCondition(List<String> errors, boolean condition, String message)
   {
      if(!condition)
      {
         errors.add(message);
      }

      return (condition);
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
}
