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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QTableMetaData
 *******************************************************************************/
class QTableMetaDataTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testIsCapabilityEnabled()
   {
      Capability capability = Capability.TABLE_GET;

      // table:null & backend:null = true
      assertTrue(new QTableMetaData().isCapabilityEnabled(new QBackendMetaData(), capability));

      // table:null & backend:true = true
      assertTrue(new QTableMetaData().isCapabilityEnabled(new QBackendMetaData().withCapability(capability), capability));

      // table:null & backend:false = false
      assertFalse(new QTableMetaData().isCapabilityEnabled(new QBackendMetaData().withoutCapability(capability), capability));

      // table:true & backend:null = true
      assertTrue(new QTableMetaData().withCapability(capability).isCapabilityEnabled(new QBackendMetaData(), capability));

      // table:false & backend:null = false
      assertFalse(new QTableMetaData().withoutCapability(capability).isCapabilityEnabled(new QBackendMetaData(), capability));

      // table:true & backend:true = true
      assertTrue(new QTableMetaData().withCapability(capability).isCapabilityEnabled(new QBackendMetaData().withCapability(capability), capability));

      // table:true & backend:false = true
      assertTrue(new QTableMetaData().withCapability(capability).isCapabilityEnabled(new QBackendMetaData().withoutCapability(capability), capability));

      // table:false & backend:true = false
      assertFalse(new QTableMetaData().withoutCapability(capability).isCapabilityEnabled(new QBackendMetaData().withCapability(capability), capability));

      // table:false & backend:false = false
      assertFalse(new QTableMetaData().withoutCapability(capability).isCapabilityEnabled(new QBackendMetaData().withoutCapability(capability), capability));

      // backend false, but then true = true
      assertTrue(new QTableMetaData().isCapabilityEnabled(new QBackendMetaData().withoutCapability(capability).withCapability(capability), capability));

      // backend true, but then false = false
      assertFalse(new QTableMetaData().isCapabilityEnabled(new QBackendMetaData().withCapability(capability).withoutCapability(capability), capability));

      // table true, but then false = true
      assertFalse(new QTableMetaData().withCapability(capability).withoutCapability(capability).isCapabilityEnabled(new QBackendMetaData(), capability));

   }

}