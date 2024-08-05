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

package com.kingsrook.qqq.backend.core.processes.locks;


import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Utility class for working with ProcessLock table - creating, checking-in,
 ** and releasing process locks.
 *******************************************************************************/
public class ProcessLockUtils
{
   private static final QLogger LOG = QLogger.getLogger(ProcessLockUtils.class);

   private static Memoization<String, ProcessLockType> getProcessLockTypeByNameMemoization = new Memoization<String, ProcessLockType>()
      .withTimeout(Duration.ofHours(1))
      .withMayStoreNullValues(false);

   private static Memoization<Integer, ProcessLockType> getProcessLockTypeByIdMemoization = new Memoization<Integer, ProcessLockType>()
      .withTimeout(Duration.ofHours(1))
      .withMayStoreNullValues(false);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ProcessLock create(String key, String typeName, String details) throws UnableToObtainProcessLockException, QException
   {
      ProcessLockType lockType = getProcessLockTypeByName(typeName);
      if(lockType == null)
      {
         throw (new QException("Unrecognized process lock type: " + typeName));
      }

      QSession qSession = QContext.getQSession();

      Instant now = Instant.now();
      ProcessLock processLock = new ProcessLock()
         .withKey(key)
         .withProcessLockTypeId(lockType.getId())
         .withSessionUUID(ObjectUtils.tryAndRequireNonNullElse(() -> qSession.getUuid(), null))
         .withUserId(ObjectUtils.tryAndRequireNonNullElse(() -> qSession.getUser().getIdReference(), null))
         .withDetails(details)
         .withCheckInTimestamp(now);

      Integer defaultExpirationSeconds = lockType.getDefaultExpirationSeconds();
      if(defaultExpirationSeconds != null)
      {
         processLock.setExpiresAtTimestamp(now.plusSeconds(defaultExpirationSeconds));
      }

      QRecord insertOutputRecord = tryToInsert(processLock);

      ////////////////////////////////////////////////////////////
      // if inserting failed... see if we can get existing lock //
      ////////////////////////////////////////////////////////////
      StringBuilder existingLockDetails = new StringBuilder();
      ProcessLock   existingLock        = null;
      if(CollectionUtils.nullSafeHasContents(insertOutputRecord.getErrors()))
      {
         QRecord existingLockRecord = new GetAction().executeForRecord(new GetInput(ProcessLock.TABLE_NAME).withUniqueKey(Map.of("key", key, "processLockTypeId", lockType.getId())));
         if(existingLockRecord != null)
         {
            existingLock = new ProcessLock(existingLockRecord);
            if(StringUtils.hasContent(existingLock.getUserId()))
            {
               existingLockDetails.append("Held by: ").append(existingLock.getUserId());
            }

            if(StringUtils.hasContent(existingLock.getDetails()))
            {
               existingLockDetails.append("; with details: ").append(existingLock.getDetails());
            }

            Instant expiresAtTimestamp = existingLock.getExpiresAtTimestamp();
            if(expiresAtTimestamp != null)
            {
               ZonedDateTime zonedExpiresAt = expiresAtTimestamp.atZone(ValueUtils.getSessionOrInstanceZoneId());
               existingLockDetails.append("; expiring at: ").append(QValueFormatter.formatDateTimeWithZone(zonedExpiresAt));
            }

            if(expiresAtTimestamp != null && expiresAtTimestamp.isBefore(now))
            {
               /////////////////////////////////////////////////////////////////////////////////
               // if existing lock has expired, then we can delete it and try to insert again //
               /////////////////////////////////////////////////////////////////////////////////
               LOG.info("Existing lock has expired - deleting it and trying again.", logPair("id", existingLock.getId()),
                  logPair("key", key), logPair("type", typeName), logPair("details", details), logPair("expiresAtTimestamp", expiresAtTimestamp));
               new DeleteAction().execute(new DeleteInput(ProcessLock.TABLE_NAME).withPrimaryKey(existingLock.getId()));
               insertOutputRecord = tryToInsert(processLock);
            }
         }
         else
         {
            /////////////////////////////////////////////////////////
            // if existing lock doesn't exist, try to insert again //
            /////////////////////////////////////////////////////////
            insertOutputRecord = tryToInsert(processLock);
         }
      }

      if(CollectionUtils.nullSafeHasContents(insertOutputRecord.getErrors()))
      {
         /////////////////////////////////////////////////////////////////////////////////
         // if at this point, we have errors on the last attempted insert, then give up //
         /////////////////////////////////////////////////////////////////////////////////
         LOG.info("Errors in process lock record after attempted insert", logPair("errors", insertOutputRecord.getErrors()),
            logPair("key", key), logPair("type", typeName), logPair("details", details));
         throw (new UnableToObtainProcessLockException("A Process Lock already exists for key [" + key + "] of type [" + typeName + "], " + existingLockDetails)
            .withExistingLock(existingLock));
      }

      LOG.info("Created process lock", logPair("id", processLock.getId()),
         logPair("key", key), logPair("type", typeName), logPair("details", details), logPair("expiresAtTimestamp", processLock.getExpiresAtTimestamp()));
      return new ProcessLock(insertOutputRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord tryToInsert(ProcessLock processLock) throws QException
   {
      return new InsertAction().execute(new InsertInput(ProcessLock.TABLE_NAME).withRecordEntity(processLock)).getRecords().get(0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ProcessLock create(String key, String type, String holderId, Duration sleepBetweenTries, Duration maxWait) throws UnableToObtainProcessLockException, QException
   {
      Instant giveUpTime = Instant.now().plus(maxWait);

      UnableToObtainProcessLockException lastCaughtUnableToObtainProcessLockException = null;
      while(true)
      {
         try
         {
            ProcessLock processLock = create(key, type, holderId);
            return (processLock);
         }
         catch(UnableToObtainProcessLockException e)
         {
            lastCaughtUnableToObtainProcessLockException = e;
            if(Instant.now().plus(sleepBetweenTries).isBefore(giveUpTime))
            {
               SleepUtils.sleep(sleepBetweenTries);
            }
            else
            {
               break;
            }
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // this variable can never be null with current code-path, but prefer to be defensive regardless //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      @SuppressWarnings("ConstantValue")
      String suffix = lastCaughtUnableToObtainProcessLockException == null ? "" : ": " + lastCaughtUnableToObtainProcessLockException.getMessage();

      //noinspection ConstantValue
      throw (new UnableToObtainProcessLockException("Unable to obtain process lock for key [" + key + "] in type [" + type + "] after [" + maxWait + "]" + suffix)
         .withExistingLock(lastCaughtUnableToObtainProcessLockException == null ? null : lastCaughtUnableToObtainProcessLockException.getExistingLock()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ProcessLock getById(Integer id) throws QException
   {
      if(id == null)
      {
         return (null);
      }

      QRecord existingLockRecord = new GetAction().executeForRecord(new GetInput(ProcessLock.TABLE_NAME).withPrimaryKey(id));
      if(existingLockRecord != null)
      {
         return (new ProcessLock(existingLockRecord));
      }
      return (null);
   }



   /*******************************************************************************
    ** input wrapper for an overload of the checkin method, to allow more flexibility
    ** w/ whether or not you want to update details & expiresAtTimestamp (e.g., so a
    ** null can be passed in, to mean "set it to null" vs. "don't update it").
    *******************************************************************************/
   public static class CheckInInput
   {
      private ProcessLock processLock;
      private Instant     expiresAtTimestamp         = null;
      private boolean     wasGivenExpiresAtTimestamp = false;
      private String      details                    = null;
      private boolean     wasGivenDetails            = false;



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public CheckInInput(ProcessLock processLock)
      {
         this.processLock = processLock;
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public CheckInInput withExpiresAtTimestamp(Instant expiresAtTimestamp)
      {
         this.expiresAtTimestamp = expiresAtTimestamp;
         this.wasGivenExpiresAtTimestamp = true;
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public CheckInInput withDetails(String details)
      {
         this.details = details;
         this.wasGivenDetails = true;
         return (this);
      }
   }



   /*******************************************************************************
    ** Do a check-in, with a specific value for the expiresAtTimestamp - which can
    ** be set to null to make it null in the lock.
    **
    ** If you don't want to specify the expiresAtTimestamp, call the overload that
    ** doesn't take the timestamp - in which case it'll either stay the same as it
    ** was, or will be set based on the type's default.
    *******************************************************************************/
   public static void checkIn(CheckInInput input)
   {
      ProcessLock processLock = input.processLock;

      try
      {
         if(processLock == null)
         {
            LOG.debug("Null processLock passed in - will not checkin.");
            return;
         }

         QRecord recordToUpdate = new QRecord()
            .withValue("id", processLock.getId())
            .withValue("checkInTimestamp", Instant.now());

         ///////////////////////////////////////////////////////////////////
         // if the input was given a details string, update the details   //
         // use boolean instead of null to know whether or not to do this //
         ///////////////////////////////////////////////////////////////////
         if(input.wasGivenDetails)
         {
            recordToUpdate.setValue("details", input.details);
         }

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if the input object had an expires-at timestamp put in it, then use that value (null or otherwise) for the expires-at-timestamp //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(input.wasGivenExpiresAtTimestamp)
         {
            recordToUpdate.setValue("expiresAtTimestamp", input.expiresAtTimestamp);
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////
            // else, do the default thing - which is, look for a default in the lock type //
            ////////////////////////////////////////////////////////////////////////////////
            ProcessLockType lockType = getProcessLockTypeById(processLock.getProcessLockTypeId());
            if(lockType != null)
            {
               Integer defaultExpirationSeconds = lockType.getDefaultExpirationSeconds();
               if(defaultExpirationSeconds != null)
               {
                  recordToUpdate.setValue("expiresAtTimestamp", Instant.now().plusSeconds(defaultExpirationSeconds));
               }
            }
         }

         new UpdateAction().execute(new UpdateInput(ProcessLock.TABLE_NAME).withRecord(recordToUpdate));
         LOG.debug("Checked in on process lock", logPair("id", processLock.getId()));
      }
      catch(Exception e)
      {
         LOG.warn("Error checking-in on process lock", e, logPair("processLockId", () -> processLock.getId()));
      }
   }



   /*******************************************************************************
    ** Do a check-in, with a specific value for the expiresAtTimestamp - which can
    ** be set to null to make it null in the lock.
    **
    ** If you don't want to specify the expiresAtTimestamp, call the overload that
    ** doesn't take the timestamp - in which case it'll either stay the same as it
    ** was, or will be set based on the type's default.
    *******************************************************************************/
   public static void checkIn(ProcessLock processLock, Instant expiresAtTimestamp)
   {
      checkIn(new CheckInInput(processLock).withExpiresAtTimestamp(expiresAtTimestamp));
   }



   /*******************************************************************************
    ** Do a check-in, updating the expires-timestamp based on the lock type's default.
    ** (or leaving it the same as it was (null or otherwise) if there is no default
    ** on the type).
    *******************************************************************************/
   public static void checkIn(ProcessLock processLock)
   {
      checkIn(new CheckInInput(processLock));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void releaseById(Integer id)
   {
      if(id == null)
      {
         LOG.debug("No id passed in to releaseById - returning with noop");
         return;
      }

      ProcessLock processLock = null;
      try
      {
         processLock = ProcessLockUtils.getById(id);
         if(processLock == null)
         {
            LOG.info("Process lock not found in releaseById call", logPair("id", id));
         }
      }
      catch(QException e)
      {
         LOG.warn("Exception releasing processLock byId", e, logPair("id", id));
      }

      if(processLock != null)
      {
         release(processLock);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void release(ProcessLock processLock)
   {
      try
      {
         if(processLock == null)
         {
            LOG.debug("No process lock passed in to release - returning with noop");
            return;
         }

         DeleteOutput deleteOutput = new DeleteAction().execute(new DeleteInput(ProcessLock.TABLE_NAME).withPrimaryKey(processLock.getId()));
         if(CollectionUtils.nullSafeHasContents(deleteOutput.getRecordsWithErrors()))
         {
            throw (new QException("Error deleting processLock record: " + deleteOutput.getRecordsWithErrors().get(0).getErrorsAsString()));
         }

         LOG.info("Released process lock", logPair("id", processLock.getId()), logPair("key", processLock.getKey()), logPair("typeId", processLock.getProcessLockTypeId()), logPair("details", processLock.getDetails()));
      }
      catch(QException e)
      {
         LOG.warn("Exception releasing processLock", e, logPair("processLockId", () -> processLock.getId()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ProcessLockType getProcessLockTypeByName(String name)
   {
      Optional<ProcessLockType> result = getProcessLockTypeByNameMemoization.getResult(name, n ->
      {
         QRecord qRecord = new GetAction().executeForRecord(new GetInput(ProcessLockType.TABLE_NAME).withUniqueKey(Map.of("name", name)));

         if(qRecord != null)
         {
            return (new ProcessLockType(qRecord));
         }

         return (null);
      });

      return (result.orElse(null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ProcessLockType getProcessLockTypeById(Integer id)
   {
      Optional<ProcessLockType> result = getProcessLockTypeByIdMemoization.getResult(id, i ->
      {
         QRecord qRecord = new GetAction().executeForRecord(new GetInput(ProcessLockType.TABLE_NAME).withPrimaryKey(id));

         if(qRecord != null)
         {
            return (new ProcessLockType(qRecord));
         }

         return (null);
      });

      return (result.orElse(null));
   }
}
