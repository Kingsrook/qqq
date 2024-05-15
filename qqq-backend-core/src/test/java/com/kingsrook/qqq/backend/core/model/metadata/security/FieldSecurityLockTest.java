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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for FieldSecurityLock 
 *******************************************************************************/
class FieldSecurityLockTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetBehaviorForSession()
   {
      QContext.getQInstance().addSecurityKeyType(new QSecurityKeyType()
         .withName("foo")
         .withValueType(QFieldType.STRING));

      FieldSecurityLock fieldSecurityLock = new FieldSecurityLock()
         .withSecurityKeyType("foo")
         .withDefaultBehavior(FieldSecurityLock.Behavior.DENY)
         .withKeyValueBehavior("bar", FieldSecurityLock.Behavior.ALLOW)
         .withKeyValueBehavior("baz", FieldSecurityLock.Behavior.ALLOW)
         .withKeyValueBehavior("boo", FieldSecurityLock.Behavior.DENY);

      QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .getField("firstName").withFieldSecurityLock(fieldSecurityLock);

      ////////////////////////////
      // no key value = default //
      ////////////////////////////
      assertEquals(FieldSecurityLock.Behavior.DENY, fieldSecurityLock.getBehaviorForSession(new QSession()));

      /////////////////////////////////////////////////
      // values specified get the behavior specified //
      /////////////////////////////////////////////////
      assertEquals(FieldSecurityLock.Behavior.ALLOW, fieldSecurityLock.getBehaviorForSession(new QSession().withSecurityKeyValue("foo", "bar")));
      assertEquals(FieldSecurityLock.Behavior.ALLOW, fieldSecurityLock.getBehaviorForSession(new QSession().withSecurityKeyValue("foo", "baz")));
      assertEquals(FieldSecurityLock.Behavior.DENY, fieldSecurityLock.getBehaviorForSession(new QSession().withSecurityKeyValue("foo", "boo")));

      //////////////////////////////////////////////
      // unrecognized values get default behavior //
      //////////////////////////////////////////////
      assertEquals(FieldSecurityLock.Behavior.DENY, fieldSecurityLock.getBehaviorForSession(new QSession().withSecurityKeyValue("foo", "huh")));

      /////////////////////////////////////////////////
      // if multiple key values, the first one wins. //
      /////////////////////////////////////////////////
      assertEquals(FieldSecurityLock.Behavior.ALLOW, fieldSecurityLock.getBehaviorForSession(new QSession().withSecurityKeyValue("foo", "bar").withSecurityKeyValue("foo", "boo")));
      assertEquals(FieldSecurityLock.Behavior.DENY, fieldSecurityLock.getBehaviorForSession(new QSession().withSecurityKeyValue("foo", "boo").withSecurityKeyValue("foo", "foo")));

   }

}