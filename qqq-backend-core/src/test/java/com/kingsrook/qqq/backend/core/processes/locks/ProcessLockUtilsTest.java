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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for ProcessLockUtils 
 *******************************************************************************/
class ProcessLockUtilsTest extends BaseTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws QException
   {
      QInstance                   qInstance = QContext.getQInstance();
      MetaDataProducerMultiOutput metaData  = new ProcessLockMetaDataProducer().produce(qInstance);

      for(QTableMetaData table : metaData.getEach(QTableMetaData.class))
      {
         table.setBackendName(TestUtils.MEMORY_BACKEND_NAME);
      }

      metaData.addSelfToInstance(qInstance);
      new QInstanceValidator().revalidate(qInstance);

      new InsertAction().execute(new InsertInput(ProcessLockType.TABLE_NAME).withRecordEntities(List.of(
         new ProcessLockType()
            .withName("typeA")
            .withLabel("Type A"),
         new ProcessLockType()
            .withName("typeB")
            .withLabel("Type B")
            .withDefaultExpirationSeconds(1),
         new ProcessLockType()
            .withName("typeC")
            .withLabel("Type C")
            .withDefaultExpirationSeconds(10)
      )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      /////////////////////////////////////////
      // make sure that we can create a lock //
      /////////////////////////////////////////
      ProcessLock processLock = ProcessLockUtils.create("1", "typeA", "me");
      assertNotNull(processLock.getId());
      assertNotNull(processLock.getCheckInTimestamp());
      assertNull(processLock.getExpiresAtTimestamp());

      /////////////////////////////////////////////////////////
      // make sure we can't create a second for the same key //
      /////////////////////////////////////////////////////////
      assertThatThrownBy(() -> ProcessLockUtils.create("1", "typeA", "you"))
         .isInstanceOf(UnableToObtainProcessLockException.class)
         .hasMessageContaining("Held by: " + QContext.getQSession().getUser().getIdReference())
         .hasMessageContaining("with details: me")
         .hasMessageNotContaining("expiring at: 20");

      /////////////////////////////////////////////////////////
      // make sure we can create another for a different key //
      /////////////////////////////////////////////////////////
      ProcessLockUtils.create("2", "typeA", "him");

      /////////////////////////////////////////////////////////////////////
      // make sure we can create another for a different type (same key) //
      /////////////////////////////////////////////////////////////////////
      ProcessLockUtils.create("1", "typeB", "her");

      //////////////////////////////
      // make sure we can release //
      //////////////////////////////
      ProcessLockUtils.release(processLock);

      //////////////////////
      // and then you can //
      //////////////////////
      processLock = ProcessLockUtils.create("1", "typeA", "you");
      assertNotNull(processLock.getId());
      assertEquals("you", processLock.getDetails());

      assertThatThrownBy(() -> ProcessLockUtils.create("1", "notAType", "you"))
         .isInstanceOf(QException.class)
         .hasMessageContaining("Unrecognized process lock type");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSucceedWaitingForExpiration() throws QException
   {
      ProcessLock processLock = ProcessLockUtils.create("1", "typeB", "me");
      assertNotNull(processLock.getId());
      assertNotNull(processLock.getCheckInTimestamp());
      assertNotNull(processLock.getExpiresAtTimestamp());

      /////////////////////////////////////////////////////////////////////////
      // make sure someone else can, if they wait longer than the expiration //
      /////////////////////////////////////////////////////////////////////////
      processLock = ProcessLockUtils.create("1", "typeB", "you", Duration.of(1, ChronoUnit.SECONDS), Duration.of(3, ChronoUnit.SECONDS));
      assertNotNull(processLock.getId());
      assertThat(processLock.getDetails()).endsWith("you");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFailWaitingForExpiration() throws QException
   {
      ProcessLock processLock = ProcessLockUtils.create("1", "typeC", "me");
      assertNotNull(processLock.getId());
      assertNotNull(processLock.getCheckInTimestamp());
      assertNotNull(processLock.getExpiresAtTimestamp());

      //////////////////////////////////////////////////////////////////
      // make sure someone else fails, if they don't wait long enough //
      //////////////////////////////////////////////////////////////////
      assertThatThrownBy(() -> ProcessLockUtils.create("1", "typeC", "you", Duration.of(1, ChronoUnit.SECONDS), Duration.of(3, ChronoUnit.SECONDS)))
         .isInstanceOf(UnableToObtainProcessLockException.class)
         .hasMessageContaining("Held by: " + QContext.getQSession().getUser().getIdReference())
         .hasMessageContaining("with details: me")
         .hasMessageContaining("expiring at: 20");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCheckInUpdatesExpiration() throws QException
   {
      ProcessLock processLock = ProcessLockUtils.create("1", "typeB", "me");
      assertNotNull(processLock.getId());
      Instant originalCheckIn    = processLock.getCheckInTimestamp();
      Instant originalExpiration = processLock.getExpiresAtTimestamp();

      SleepUtils.sleep(5, TimeUnit.MILLISECONDS);
      ProcessLockUtils.checkIn(processLock);

      ProcessLock freshLock = ProcessLockUtils.getById(processLock.getId());
      assertNotNull(freshLock);
      assertNotEquals(originalCheckIn, freshLock.getCheckInTimestamp());
      assertNotEquals(originalExpiration, freshLock.getExpiresAtTimestamp());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReleaseById() throws QException
   {
      ////////////////////////////////////////////
      // assert no exceptions for these 2 cases //
      ////////////////////////////////////////////
      ProcessLockUtils.releaseById(null);
      ProcessLockUtils.releaseById(1);

      ProcessLock processLock = ProcessLockUtils.create("1", "typeA", "me");
      ProcessLockUtils.releaseById(processLock.getId());
      assertNull(ProcessLockUtils.getById(processLock.getId()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUserAndSessionNullness() throws QException
   {
      {
         QContext.getQSession().setUser(new QUser().withIdReference("me"));
         ProcessLock processLock = ProcessLockUtils.create("1", "typeA", null);
         assertNull(processLock.getDetails());
         assertEquals("me", processLock.getUserId());
         assertEquals(QContext.getQSession().getUuid(), processLock.getSessionUUID());
      }

      {
         ProcessLock processLock = ProcessLockUtils.create("2", "typeA", "foo");
         assertEquals("foo", processLock.getDetails());
         assertEquals("me", processLock.getUserId());
         assertEquals(QContext.getQSession().getUuid(), processLock.getSessionUUID());
      }

      {
         QContext.getQSession().setUser(null);
         ProcessLock processLock = ProcessLockUtils.create("3", "typeA", "bar");
         assertEquals("bar", processLock.getDetails());
         assertNull(processLock.getUserId());
         assertEquals(QContext.getQSession().getUuid(), processLock.getSessionUUID());
      }

      {
         QContext.getQSession().setUuid(null);
         ProcessLock processLock = ProcessLockUtils.create("4", "typeA", "baz");
         assertEquals("baz", processLock.getDetails());
         assertNull(processLock.getUserId());
         assertNull(processLock.getSessionUUID());
      }

      {
         QContext.getQSession().setUuid(null);
         ProcessLock processLock = ProcessLockUtils.create("5", "typeA", "");
         assertEquals("", processLock.getDetails());
         assertNull(processLock.getUserId());
         assertNull(processLock.getSessionUUID());
      }
   }

}