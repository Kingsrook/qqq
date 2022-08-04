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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


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
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateProcesses(QInstance qInstance, List<String> errors)
   {
      if(!CollectionUtils.nullSafeIsEmpty(qInstance.getProcesses()))
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
      if(!CollectionUtils.nullSafeIsEmpty(qInstance.getApps()))
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

}
