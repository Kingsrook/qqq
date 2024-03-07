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

package com.kingsrook.qqq.backend.core.model.metadata.security;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for NullValueBehaviorUtil 
 *******************************************************************************/
class NullValueBehaviorUtilTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      ////////////////////////////////////////////////////////////////////////////////////////
      // if session doesn't have a null-value key, then always get back the lock's behavior //
      ////////////////////////////////////////////////////////////////////////////////////////
      for(RecordSecurityLock.NullValueBehavior lockNullValueBehavior : RecordSecurityLock.NullValueBehavior.values())
      {
         assertEquals(lockNullValueBehavior, NullValueBehaviorUtil.getEffectiveNullValueBehavior(new RecordSecurityLock()
            .withSecurityKeyType(TestUtils.SECURITY_KEY_TYPE_STORE)
            .withNullValueBehavior(lockNullValueBehavior)));
      }

      /////////////////////////////////////////////////////////////////////
      // if session DOES have a null-value key, then always gete it back //
      /////////////////////////////////////////////////////////////////////
      for(RecordSecurityLock.NullValueBehavior sessionNullValueBehavior : RecordSecurityLock.NullValueBehavior.values())
      {
         QContext.getQSession().withSecurityKeyValues(Map.of(TestUtils.SECURITY_KEY_TYPE_STORE_NULL_BEHAVIOR, List.of(sessionNullValueBehavior.toString())));

         for(RecordSecurityLock.NullValueBehavior lockNullValueBehavior : RecordSecurityLock.NullValueBehavior.values())
         {
            assertEquals(sessionNullValueBehavior, NullValueBehaviorUtil.getEffectiveNullValueBehavior(new RecordSecurityLock()
               .withSecurityKeyType(TestUtils.SECURITY_KEY_TYPE_STORE)
               .withNullValueBehavior(lockNullValueBehavior)));
         }
      }

      ////////////////////////////////////////////////////////////////////
      // if session has an invalid key, always get back lock's behavior //
      ////////////////////////////////////////////////////////////////////
      for(RecordSecurityLock.NullValueBehavior lockNullValueBehavior : RecordSecurityLock.NullValueBehavior.values())
      {
         QContext.getQSession().withSecurityKeyValues(Map.of(TestUtils.SECURITY_KEY_TYPE_STORE_NULL_BEHAVIOR, List.of("xyz")));

         assertEquals(lockNullValueBehavior, NullValueBehaviorUtil.getEffectiveNullValueBehavior(new RecordSecurityLock()
            .withSecurityKeyType(TestUtils.SECURITY_KEY_TYPE_STORE)
            .withNullValueBehavior(lockNullValueBehavior)));
      }
   }

}