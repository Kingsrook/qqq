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

package com.kingsrook.qqq.backend.core.model.metadata.frontend;


import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.security.FieldSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QFrontendTableMetaData 
 *******************************************************************************/
class QFrontendTableMetaDataTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldLocks()
   {
      QContext.getQInstance().addSecurityKeyType(new QSecurityKeyType()
         .withName("allowedToSeeFirstName")
         .withValueType(QFieldType.BOOLEAN));

      FieldSecurityLock fieldSecurityLock = new FieldSecurityLock()
         .withSecurityKeyType("allowedToSeeFirstName")
         .withDefaultBehavior(FieldSecurityLock.Behavior.DENY)
         .withKeyValueBehavior(true, FieldSecurityLock.Behavior.ALLOW);

      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.getField("firstName").withFieldSecurityLock(fieldSecurityLock);

      Supplier<QFrontendTableMetaData> run = () -> new QFrontendTableMetaData(QContext.getQInstance().getBackendForTable(TestUtils.TABLE_NAME_PERSON_MEMORY), table, true, false);

      //////////////////////////////////////////////////////////////
      // default session (no key) should NOT get to see firstName //
      //////////////////////////////////////////////////////////////
      assertFalse(run.get().getFields().containsKey("firstName"));

      /////////////////////////////////////
      // with the key=true, then allowed //
      /////////////////////////////////////
      QContext.setQSession(new QSession().withSecurityKeyValue("allowedToSeeFirstName", true));
      assertTrue(run.get().getFields().containsKey("firstName"));

      ////////////////////////////////////////
      // try a string version of the key... //
      ////////////////////////////////////////
      QContext.setQSession(new QSession().withSecurityKeyValue("allowedToSeeFirstName", "true"));
      assertTrue(run.get().getFields().containsKey("firstName"));

      ////////////////////////////
      // try unrecognized value //
      ////////////////////////////
      QContext.setQSession(new QSession().withSecurityKeyValue("allowedToSeeFirstName", "nope"));
      assertFalse(run.get().getFields().containsKey("firstName"));
   }

}