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

package com.kingsrook.qqq.backend.core.model.session;


import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.Auth0AuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.FullyAnonymousAuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.MockAuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.TableBasedAuthenticationModule;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QSystemUserSession 
 *******************************************************************************/
class QSystemUserSessionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      QSystemUserSession systemUserSession = new QSystemUserSession();

      assertEquals(List.of(true), systemUserSession.getSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS));

      assertTrue(new Auth0AuthenticationModule().isSessionValid(QContext.getQInstance(), systemUserSession));
      assertTrue(new TableBasedAuthenticationModule().isSessionValid(QContext.getQInstance(), systemUserSession));
      assertTrue(new MockAuthenticationModule().isSessionValid(QContext.getQInstance(), systemUserSession));
      assertTrue(new FullyAnonymousAuthenticationModule().isSessionValid(QContext.getQInstance(), systemUserSession));

      assertTrue(systemUserSession.hasPermission(null));
      assertTrue(systemUserSession.hasPermission(""));
      assertTrue(systemUserSession.hasPermission("anything"));
      assertTrue(systemUserSession.hasPermission(UUID.randomUUID().toString()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWeDoNotBlowUpIfInstanceIsntInContextWhenPrimingAllAccessKeyNames()
   {
      QInstance qInstance = QContext.getQInstance();
      QSystemUserSession.unsetAllAccessKeyNames();

      QContext.clear();
      QSystemUserSession systemUserSession = new QSystemUserSession();
      assertEquals(Collections.emptyList(), systemUserSession.getSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS));

      QContext.setQInstance(qInstance);
      systemUserSession = new QSystemUserSession();
      assertEquals(List.of(true), systemUserSession.getSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS));
   }

}