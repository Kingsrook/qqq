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

package com.kingsrook.qqq.backend.core.modules.backend;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for QBackendModuleInterface
 **
 *******************************************************************************/
class QBackendModuleInterfaceTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test()
   {
      TestClass tc = new TestClass();
      Class     c  = tc.getTableBackendDetailsClass();
      assertEquals(c.getName(), QTableBackendDetails.class.getName(), "classname should be QTableBackendDetails");

      try
      {
         tc.getCountInterface();
      }
      catch(IllegalStateException iae)
      {
         try
         {
            tc.getQueryInterface();
         }
         catch(IllegalStateException iae2)
         {
            try
            {
               tc.getInsertInterface();
            }
            catch(IllegalStateException iae3)
            {
               try
               {
                  tc.getUpdateInterface();
               }
               catch(IllegalStateException iae4)
               {
                  try
                  {
                     tc.getDeleteInterface();
                  }
                  catch(IllegalStateException iae5)
                  {
                     return;
                  }
               }
            }
         }
      }

      fail("should not get here...");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private class TestClass implements QBackendModuleInterface
   {
      @Override
      public String getBackendType()
      {
         return null;
      }



      @Override
      public Class<? extends QBackendMetaData> getBackendMetaDataClass()
      {
         return null;
      }
   }

}
