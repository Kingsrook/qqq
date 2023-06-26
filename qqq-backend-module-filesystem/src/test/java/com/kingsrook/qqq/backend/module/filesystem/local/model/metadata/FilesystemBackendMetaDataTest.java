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

package com.kingsrook.qqq.backend.module.filesystem.local.model.metadata;


import java.io.IOException;
import com.kingsrook.qqq.backend.core.adapters.QInstanceAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for FilesystemBackendMetaData
 *******************************************************************************/
@Disabled("This concept doesn't seem right any more.  We will want/need custom JSON/YAML serialization, so, let us disable this test, at least for now, and maybe permanently")
class FilesystemBackendMetaDataTest
{


   /*******************************************************************************
    ** Test that an instance can be serialized as expected
    *******************************************************************************/
   @Test
   public void testSerializingToJson() throws QException
   {
      TestUtils.resetTestInstanceCounter();
      QInstance qInstance = TestUtils.defineInstance();
      String    json      = new QInstanceAdapter().qInstanceToJsonIncludingBackend(qInstance);
      System.out.println(JsonUtils.prettyPrint(json));
      System.out.println(json);
      String expectToContain = """
         "local-filesystem":{"disabledCapabilities":["QUERY_STATS"],"basePath":"/tmp/filesystem-tests/0","backendType":"filesystem","name":"local-filesystem","usesVariants":false}""";
      assertTrue(json.contains(expectToContain));
   }



   /*******************************************************************************
    ** Test that an instance can be deserialized as expected
    *******************************************************************************/
   @Test
   public void testDeserializingFromJson() throws IOException, QException
   {
      QInstanceAdapter qInstanceAdapter = new QInstanceAdapter();

      QInstance qInstance = TestUtils.defineInstance();
      String    json      = qInstanceAdapter.qInstanceToJsonIncludingBackend(qInstance);

      QInstance deserialized = qInstanceAdapter.jsonToQInstanceIncludingBackends(json);
      assertThat(deserialized.getBackends()).usingRecursiveComparison()
         // TODO seeing occassional flaps on this field - where it can be null 1 out of 10 runs... unclear why.
         .ignoringFields("mock.backendType")
         .isEqualTo(qInstance.getBackends());
   }
}
