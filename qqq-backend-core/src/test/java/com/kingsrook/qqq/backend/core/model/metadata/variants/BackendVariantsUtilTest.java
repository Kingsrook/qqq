/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.variants;


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for BackendVariantsUtil 
 *******************************************************************************/
class BackendVariantsUtilTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetVariantId() throws QException
   {
      QBackendMetaData myBackend = getBackendMetaData();

      assertThatThrownBy(() -> BackendVariantsUtil.getVariantId(myBackend))
         .hasMessageContaining("Could not find Backend Variant information in session under key 'yourSelectedShape' for Backend 'TestBackend'");

      QContext.getQSession().setBackendVariants(Map.of("yourSelectedShape", 1701));
      assertEquals(1701, BackendVariantsUtil.getVariantId(myBackend));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static QBackendMetaData getBackendMetaData()
   {
      QBackendMetaData myBackend = new QBackendMetaData()
         .withName("TestBackend")
         .withUsesVariants(true)
         .withBackendVariantsConfig(new BackendVariantsConfig()
            .withOptionsTableName(TestUtils.TABLE_NAME_SHAPE)
            .withVariantTypeKey("yourSelectedShape"));
      return myBackend;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetVariantRecord() throws QException
   {
      QBackendMetaData myBackend = getBackendMetaData();

      TestUtils.insertDefaultShapes(QContext.getQInstance());

      assertThatThrownBy(() -> BackendVariantsUtil.getVariantRecord(myBackend))
         .hasMessageContaining("Could not find Backend Variant information in session under key 'yourSelectedShape' for Backend 'TestBackend'");

      QContext.getQSession().setBackendVariants(Map.of("yourSelectedShape", 1701));
      assertThatThrownBy(() -> BackendVariantsUtil.getVariantRecord(myBackend))
         .hasMessageContaining("Could not find Backend Variant in table shape with id '1701'");

      QContext.getQSession().setBackendVariants(Map.of("yourSelectedShape", 1));
      QRecord variantRecord = BackendVariantsUtil.getVariantRecord(myBackend);
      assertEquals(1, variantRecord.getValueInteger("id"));
      assertNotNull(variantRecord.getValue("name"));
   }

}