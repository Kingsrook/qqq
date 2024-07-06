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

package com.kingsrook.qqq.backend.core.logging;


import com.kingsrook.qqq.backend.core.BaseTest;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QCollectingLogger 
 *******************************************************************************/
class QCollectingLoggerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      ClassThatLogsThings classThatLogsThings = new ClassThatLogsThings();
      classThatLogsThings.logAnInfo("1");

      QCollectingLogger collectingLogger = QLogger.activateCollectingLoggerForClass(ClassThatLogsThings.class);
      classThatLogsThings.logAnInfo("2");
      classThatLogsThings.logAWarn("3");
      QLogger.deactivateCollectingLoggerForClass(ClassThatLogsThings.class);

      classThatLogsThings.logAWarn("4");

      assertEquals(2, collectingLogger.getCollectedMessages().size());

      assertThat(collectingLogger.getCollectedMessages().get(0).getMessage()).contains("""
         "message":"2",""");
      assertEquals("2", collectingLogger.getCollectedMessages().get(0).getMessageAsJSONObject().getString("message"));
      assertEquals(Level.INFO, collectingLogger.getCollectedMessages().get(0).getLevel());

      assertThat(collectingLogger.getCollectedMessages().get(1).getMessage()).contains("""
         "message":"3",""");
      assertEquals(Level.WARN, collectingLogger.getCollectedMessages().get(1).getLevel());
      assertEquals("3", collectingLogger.getCollectedMessages().get(1).getMessageAsJSONObject().getString("message"));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public static class ClassThatLogsThings
   {
      private static final QLogger LOG = QLogger.getLogger(ClassThatLogsThings.class);

      /*******************************************************************************
       **
       *******************************************************************************/
      private void logAnInfo(String message)
      {
         LOG.info(message);
      }

      /*******************************************************************************
       **
       *******************************************************************************/
      private void logAWarn(String message)
      {
         LOG.warn(message);
      }
   }

}