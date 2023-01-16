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

package com.kingsrook.qqq.backend.core.actions.automation;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTrackingType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.QTableAutomationDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TriggerEvent;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 ** Utility class for updating the automation status data for records
 *******************************************************************************/
public class RecordAutomationStatusUpdater
{
   private static final QLogger LOG = QLogger.getLogger(RecordAutomationStatusUpdater.class);



   /*******************************************************************************
    ** for a list of records from a table, set their automation status - based on
    ** how the table is configured.
    *******************************************************************************/
   public static boolean setAutomationStatusInRecords(QSession session, QTableMetaData table, List<QRecord> records, AutomationStatus automationStatus)
   {
      if(table == null || table.getAutomationDetails() == null || CollectionUtils.nullSafeIsEmpty(records))
      {
         return (false);
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // In case an automation is running, and it updates records - don't let those records be marked  //
      // as PENDING_UPDATE_AUTOMATIONS... this is meant to avoid having a record's automation update   //
      // itself, and then continue to do so in a loop (infinitely).                                    //
      // BUT - shouldn't this be allowed to update OTHER records to be pending updates?  It seems like //
      // yes - so -that'll probably be a bug to fix at some point in the future todo                   //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      if(automationStatus.equals(AutomationStatus.PENDING_UPDATE_AUTOMATIONS))
      {
         Exception e = new Exception();
         for(StackTraceElement stackTraceElement : e.getStackTrace())
         {
            String className = stackTraceElement.getClassName();
            if(className.contains("com.kingsrook.qqq.backend.core.actions.automation") && !className.equals(RecordAutomationStatusUpdater.class.getName()) && !className.endsWith("Test"))
            {
               LOG.debug("Avoiding re-setting automation status to PENDING_UPDATE while running an automation");
               return (false);
            }
         }
      }

      if(canWeSkipPendingAndGoToOkay(table, automationStatus))
      {
         automationStatus = AutomationStatus.OK;
      }

      QTableAutomationDetails automationDetails = table.getAutomationDetails();
      if(automationDetails.getStatusTracking() != null && AutomationStatusTrackingType.FIELD_IN_TABLE.equals(automationDetails.getStatusTracking().getType()))
      {
         for(QRecord record : records)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // todo - seems like there's some case here, where if an order was in PENDING_INSERT, but then some other job updated the record, that we'd  //
            //  lose that pending status, which would be a Bad Thing™...                                                                                 //
            //  problem is - we may not have the full record in here, so we can't necessarily check the record to see what status it's currently in...   //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            record.setValue(automationDetails.getStatusTracking().getFieldName(), automationStatus.getId());
            // todo - another field - for the automation timestamp??
         }
      }

      return (true);
   }



   /*******************************************************************************
    ** If a table has no automation actions defined for Insert (or Update), and we're
    ** being asked to set status to PENDING_INSERT (or PENDING_UPDATE), then just
    ** move the status straight to OK.
    *******************************************************************************/
   private static boolean canWeSkipPendingAndGoToOkay(QTableMetaData table, AutomationStatus automationStatus)
   {
      List<TableAutomationAction> tableActions = Objects.requireNonNullElse(table.getAutomationDetails().getActions(), new ArrayList<>());

      if(automationStatus.equals(AutomationStatus.PENDING_INSERT_AUTOMATIONS))
      {
         return tableActions.stream().noneMatch(a -> TriggerEvent.POST_INSERT.equals(a.getTriggerEvent()));
      }
      else if(automationStatus.equals(AutomationStatus.PENDING_UPDATE_AUTOMATIONS))
      {
         return tableActions.stream().noneMatch(a -> TriggerEvent.POST_UPDATE.equals(a.getTriggerEvent()));
      }

      return (false);
   }



   /*******************************************************************************
    ** for a list of records, update their automation status and actually Update the
    ** backend as well.
    *******************************************************************************/
   public static void setAutomationStatusInRecordsAndUpdate(QInstance instance, QSession session, QTableMetaData table, List<QRecord> records, AutomationStatus automationStatus) throws QException
   {
      QTableAutomationDetails automationDetails = table.getAutomationDetails();
      if(automationDetails != null && AutomationStatusTrackingType.FIELD_IN_TABLE.equals(automationDetails.getStatusTracking().getType()))
      {
         boolean didSetStatusField = setAutomationStatusInRecords(session, table, records, automationStatus);
         if(didSetStatusField)
         {
            UpdateInput updateInput = new UpdateInput();
            updateInput.setTableName(table.getName());

            /////////////////////////////////////////////////////////////////////////////////////
            // build records with just their pkey & status field for this update, to avoid     //
            // changing other values (relies on assumption of Patch semantics in UpdateAction) //
            /////////////////////////////////////////////////////////////////////////////////////
            updateInput.setRecords(records.stream().map(r -> new QRecord()
               .withTableName(r.getTableName())
               .withValue(table.getPrimaryKeyField(), r.getValue(table.getPrimaryKeyField()))
               .withValue(automationDetails.getStatusTracking().getFieldName(), r.getValue(automationDetails.getStatusTracking().getFieldName()))).toList());
            updateInput.setAreAllValuesBeingUpdatedTheSame(true);

            new UpdateAction().execute(updateInput);
         }
      }
      else
      {
         // todo - verify if this is valid as other types are built
         throw (new NotImplementedException("Updating record automation status is not implemented for table [" + table + "], tracking type: "
            + (automationDetails == null ? "null" : automationDetails.getStatusTracking().getType())));
      }
   }

}
