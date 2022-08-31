package com.kingsrook.qqq.backend.core.actions.automation;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTrackingType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.QTableAutomationDetails;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility class for updating the automation status data for records
 *******************************************************************************/
public class RecordAutomationStatusUpdater
{
   private static final Logger LOG = LogManager.getLogger(RecordAutomationStatusUpdater.class);



   /*******************************************************************************
    ** for a list of records from a table, set their automation status - based on
    ** how the table is configured.
    *******************************************************************************/
   public static boolean setAutomationStatusInRecords(QTableMetaData table, List<QRecord> records, AutomationStatus automationStatus)
   {
      if(table == null || table.getAutomationDetails() == null || CollectionUtils.nullSafeIsEmpty(records))
      {
         return (false);
      }

      if(automationStatus.equals(AutomationStatus.PENDING_INSERT_AUTOMATIONS) || automationStatus.equals(AutomationStatus.PENDING_UPDATE_AUTOMATIONS))
      {
         Exception e = new Exception();
         for(StackTraceElement stackTraceElement : e.getStackTrace())
         {
            String className = stackTraceElement.getClassName();
            if(className.contains("com.kingsrook.qqq.backend.core.actions.automation") && !className.equals(RecordAutomationStatusUpdater.class.getName()) && !className.endsWith("Test"))
            {
               LOG.info("Avoiding re-setting automation status to PENDING while running an automation");
               return (false);
            }
         }
      }

      QTableAutomationDetails automationDetails = table.getAutomationDetails();
      if(automationDetails.getStatusTracking() != null && AutomationStatusTrackingType.FIELD_IN_TABLE.equals(automationDetails.getStatusTracking().getType()))
      {
         for(QRecord record : records)
         {
            record.setValue(automationDetails.getStatusTracking().getFieldName(), automationStatus.getId());
            // todo - another field - for the automation timestamp??
         }
      }

      return (true);
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
         boolean didSetStatusField = setAutomationStatusInRecords(table, records, automationStatus);
         if(didSetStatusField)
         {
            UpdateInput updateInput = new UpdateInput(instance);
            updateInput.setSession(session);
            updateInput.setTableName(table.getName());
            updateInput.setRecords(records);
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
