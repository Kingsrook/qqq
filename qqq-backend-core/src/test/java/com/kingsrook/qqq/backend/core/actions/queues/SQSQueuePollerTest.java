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

package com.kingsrook.qqq.backend.core.actions.queues;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSPollerSettings;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for SQSQueuePoller 
 *******************************************************************************/
class SQSQueuePollerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetSqsPollerSettings()
   {
      ///////////////////
      // defaults only //
      ///////////////////
      assertSettings(Integer.MAX_VALUE, 10, 20, SQSQueuePoller.getSqsPollerSettings(null, null));
      assertSettings(Integer.MAX_VALUE, 10, 20, SQSQueuePoller.getSqsPollerSettings(new SQSQueueProviderMetaData(), new SQSQueueMetaData()));
      assertSettings(Integer.MAX_VALUE, 10, 20, SQSQueuePoller.getSqsPollerSettings(new SQSQueueProviderMetaData().withPollerSettings(new SQSPollerSettings()), new SQSQueueMetaData().withPollerSettings(new SQSPollerSettings())));

      ///////////////////////////////////
      // settings only in the provider //
      ///////////////////////////////////
      assertSettings(100, 5, 1, SQSQueuePoller.getSqsPollerSettings(
         new SQSQueueProviderMetaData().withPollerSettings(new SQSPollerSettings().withMaxLoops(100).withMaxNumberOfMessages(5).withWaitTimeSeconds(1)),
         new QQueueMetaData()));

      ////////////////////////////////
      // settings only in the queue //
      ////////////////////////////////
      assertSettings(90, 4, 2, SQSQueuePoller.getSqsPollerSettings(
         new SQSQueueProviderMetaData(),
         new SQSQueueMetaData().withPollerSettings(new SQSPollerSettings().withMaxLoops(90).withMaxNumberOfMessages(4).withWaitTimeSeconds(2))));

      /////////////////////////////////////////
      // mix of default, provider, and queue //
      /////////////////////////////////////////
      assertSettings(Integer.MAX_VALUE, 5, 2, SQSQueuePoller.getSqsPollerSettings(
         new SQSQueueProviderMetaData().withPollerSettings(new SQSPollerSettings().withMaxNumberOfMessages(5)),
         new SQSQueueMetaData().withPollerSettings(new SQSPollerSettings().withWaitTimeSeconds(2))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertSettings(Integer maxLoops, Integer maxNumberOfMessages, Integer waitTimeSeconds, SQSPollerSettings sqsPollerSettings)
   {
      assertEquals(maxLoops, sqsPollerSettings.getMaxLoops());
      assertEquals(maxNumberOfMessages, sqsPollerSettings.getMaxNumberOfMessages());
      assertEquals(waitTimeSeconds, sqsPollerSettings.getWaitTimeSeconds());
   }

}