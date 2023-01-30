/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.audits;


import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditInput;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Insert an audit (e.g., the same one) against 1 or more records.
 **
 ** Takes care of managing the foreign key tables.
 **
 ** Enforces that security key values are provided, if the table has any.
 *******************************************************************************/
public class AuditAction extends AbstractQActionFunction<AuditInput, AuditOutput>
{
   private static final QLogger LOG = QLogger.getLogger(AuditAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void execute(String tableName, Integer recordId, Map<String, Serializable> securityKeyValues, String message)
   {
      new AuditAction().execute(new AuditInput()
         .withAuditTableName(tableName)
         .withRecordIdList(List.of(recordId))
         .withSecurityKeyValues(securityKeyValues)
         .withMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public AuditOutput execute(AuditInput input)
   {
      AuditOutput auditOutput = new AuditOutput();
      try
      {
         QTableMetaData table = QContext.getQInstance().getTable(input.getAuditTableName());
         if(table == null)
         {
            throw (new QException("Requested audit for an unrecognized table name: " + input.getAuditTableName()));
         }

         for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(table.getRecordSecurityLocks()))
         {
            if(input.getSecurityKeyValues() == null || !input.getSecurityKeyValues().containsKey(recordSecurityLock.getSecurityKeyType()))
            {
               throw (new QException("Missing securityKeyValue [" + recordSecurityLock.getSecurityKeyType() + "] in audit request for table " + input.getAuditTableName()));
            }
         }

         Integer auditTableId = getIdForName("auditTable", input.getAuditTableName());
         Integer auditUserId  = getIdForName("auditUser", Objects.requireNonNullElse(input.getAuditUserName(), getSessionUserName()));
         Instant timestamp    = Objects.requireNonNullElse(input.getTimestamp(), Instant.now());

         List<QRecord> auditRecords = new ArrayList<>();
         for(Integer recordId : input.getRecordIdList())
         {
            QRecord record = new QRecord()
               .withValue("auditTableId", auditTableId)
               .withValue("auditUserId", auditUserId)
               .withValue("timestamp", timestamp)
               .withValue("message", input.getMessage())
               .withValue("recordId", recordId);

            if(input.getSecurityKeyValues() != null)
            {
               for(Map.Entry<String, Serializable> entry : input.getSecurityKeyValues().entrySet())
               {
                  record.setValue(entry.getKey(), entry.getValue());
               }
            }

            auditRecords.add(record);
         }

         InsertInput insertInput = new InsertInput();
         insertInput.setTableName("audit");
         insertInput.setRecords(auditRecords);
         new InsertAction().execute(insertInput);
      }
      catch(Exception e)
      {
         LOG.error("Error performing an audit", e);
      }
      return (auditOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getSessionUserName()
   {
      QUser user = QContext.getQSession().getUser();
      if(user == null)
      {
         return ("Unknown");
      }
      return (user.getFullName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Integer getIdForName(String tableName, String nameValue) throws QException
   {
      Integer id = fetchIdFromName(tableName, nameValue);
      if(id != null)
      {
         return id;
      }

      try
      {
         LOG.debug("Inserting " + tableName + " named " + nameValue);
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(tableName);
         QRecord record = new QRecord().withValue("name", nameValue);

         if(tableName.equals("auditTable"))
         {
            QTableMetaData table = QContext.getQInstance().getTable(nameValue);
            if(table != null)
            {
               record.setValue("label", table.getLabel());
            }
         }

         insertInput.setRecords(List.of(record));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         id = insertOutput.getRecords().get(0).getValueInteger("id");
         if(id != null)
         {
            return id;
         }
      }
      catch(Exception e)
      {
         ////////////////////////////////////////////////////////////////////
         // assume this may mean a dupe-key - so - try another fetch below //
         ////////////////////////////////////////////////////////////////////
         LOG.debug("Caught error inserting " + tableName + " named " + nameValue + " - will try to re-fetch", e);
      }

      id = fetchIdFromName(tableName, nameValue);
      if(id != null)
      {
         return id;
      }

      /////////////
      // give up //
      /////////////
      throw (new QException("Unable to get id for " + tableName + " named " + nameValue));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer fetchIdFromName(String tableName, String nameValue) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName(tableName);
      getInput.setUniqueKey(Map.of("name", nameValue));
      GetOutput getOutput = new GetAction().execute(getInput);
      if(getOutput.getRecord() != null)
      {
         return (getOutput.getRecord().getValueInteger("id"));
      }
      return null;
   }

}
