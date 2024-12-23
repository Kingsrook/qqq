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

package com.kingsrook.qqq.backend.core.instances.loaders;


import java.nio.charset.StandardCharsets;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.instances.loaders.implementations.QTableMetaDataLoader;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.DenyBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.YamlUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for QTableMetaDataLoader 
 *******************************************************************************/
class QTableMetaDataLoaderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testToYaml() throws QMetaDataLoaderException
   {
      QTableMetaData expectedTable = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      String expectedYaml = YamlUtils.toYaml(expectedTable);
      QTableMetaData actualTable = new QTableMetaDataLoader().fileToMetaDataObject(new QInstance(), IOUtils.toInputStream(expectedYaml, StandardCharsets.UTF_8), "person.yaml");
      String actualYaml = YamlUtils.toYaml(actualTable);
      assertEquals(expectedYaml, actualYaml);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testYaml() throws QMetaDataLoaderException
   {
      QTableMetaData table = new QTableMetaDataLoader().fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
         class: QTableMetaData
         version: 1.0
         name: myTable
         icon:
            name: account_tree
         fields:
            id:
               name: id
               type: INTEGER
            name:
               name: name
               type: STRING
         uniqueKeys:
         -  label: Name
            fieldNames:
            - name
         associations:
         -  name: A1
            associatedTableName: yourTable
            joinName: myTableJoinYourTable
         -  name: A2
            associatedTableName: theirTable
            joinName: myTableJoinTheirTable
         permissionRules:
            level: READ_WRITE_PERMISSIONS
            denyBehavior: HIDDEN
            permissionBaseName: myTablePermissions
            customPermissionChecker:
               name: com.kingsrook.SomeChecker
               codeType: JAVA
         ## todo recordSecurityLocks
         ## todo auditRules
         ## todo backendDetails
         ## todo automationDetails
         customizers:
            postQueryRecord:
               name: com.kingsrook.SomePostQuery
               codeType: JAVA
            preDeleteRecord:
               name: com.kingsrook.SomePreDelete
               codeType: JAVA
         """, StandardCharsets.UTF_8), "myTable.yaml");

      assertEquals("myTable", table.getName());

      assertEquals(2, table.getFields().size());
      // assertEquals("id", table.getFields().get("id").getName());
      assertEquals(QFieldType.INTEGER, table.getFields().get("id").getType());
      // assertEquals("name", table.getFields().get("name").getName());
      assertEquals(QFieldType.STRING, table.getFields().get("name").getType());

      assertNotNull(table.getIcon());
      assertEquals("account_tree", table.getIcon().getName());

      assertEquals(1, table.getUniqueKeys().size());
      assertEquals(List.of("name"), table.getUniqueKeys().get(0).getFieldNames());
      assertEquals("Name", table.getUniqueKeys().get(0).getLabel());

      assertEquals(2, table.getAssociations().size());
      assertEquals("A1", table.getAssociations().get(0).getName());
      assertEquals("theirTable", table.getAssociations().get(1).getAssociatedTableName());

      assertNotNull(table.getPermissionRules());
      assertEquals(PermissionLevel.READ_WRITE_PERMISSIONS, table.getPermissionRules().getLevel());
      assertEquals(DenyBehavior.HIDDEN, table.getPermissionRules().getDenyBehavior());
      assertEquals("myTablePermissions", table.getPermissionRules().getPermissionBaseName());
      assertNotNull(table.getPermissionRules().getCustomPermissionChecker());
      assertEquals("com.kingsrook.SomeChecker", table.getPermissionRules().getCustomPermissionChecker().getName());
      assertEquals(QCodeType.JAVA, table.getPermissionRules().getCustomPermissionChecker().getCodeType());

      assertEquals(2, table.getCustomizers().size());
      assertEquals("com.kingsrook.SomePostQuery", table.getCustomizers().get(TableCustomizers.POST_QUERY_RECORD.getRole()).getName());
      assertEquals("com.kingsrook.SomePreDelete", table.getCustomizers().get(TableCustomizers.PRE_DELETE_RECORD.getRole()).getName());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleJson() throws QMetaDataLoaderException
   {
      QTableMetaData table = new QTableMetaDataLoader().fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
         {
            "class": "QTableMetaData",
            "version": "1.0",
            "name": "myTable",
            "fields":
            {
               "id": {"name": "id", "type": "INTEGER"},
               "name": {"name": "name", "type": "STRING"}
            }
         }
         """, StandardCharsets.UTF_8), "myTable.json");

      assertEquals("myTable", table.getName());
      assertEquals(2, table.getFields().size());
      assertEquals("id", table.getFields().get("id").getName());
      assertEquals(QFieldType.INTEGER, table.getFields().get("id").getType());
      assertEquals("name", table.getFields().get("name").getName());
      assertEquals(QFieldType.STRING, table.getFields().get("name").getType());
   }


}