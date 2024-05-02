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

package com.kingsrook.qqq.backend.core.model.metadata.sharing;


import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.instances.QInstanceValidatorTest.assertValidationFailureReasons;
import static com.kingsrook.qqq.backend.core.instances.QInstanceValidatorTest.assertValidationFailureReasonsAllowingExtraReasons;
import static com.kingsrook.qqq.backend.core.instances.QInstanceValidatorTest.assertValidationSuccess;


/*******************************************************************************
 ** Unit test for ShareableTableMetaData 
 *******************************************************************************/
class ShareableTableMetaDataTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidation()
   {
      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData())),
         "missing sharedRecordTableName");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName("notATable")
      )), "unrecognized sharedRecordTableName");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withAudienceTypesPossibleValueSourceName("notAPVS")
      )), "unrecognized audienceTypesPossibleValueSourceName");

      assertValidationFailureReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
            .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         )), "missing assetIdFieldName",
         "missing scopeFieldName",
         "missing audienceTypes");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withAssetIdFieldName("notAField")
      )), "unrecognized assertIdFieldName");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withScopeFieldName("notAField")
      )), "unrecognized scopeFieldName");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withAudienceType(new ShareableAudienceType().withName("myType"))
      )), "missing fieldName for shareableAudienceType");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withAudienceType(new ShareableAudienceType().withName("myType").withFieldName("notAField"))
      )), "unrecognized fieldName");

      /* todo - corresponding todo in main class
      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withAudienceType(new ShareableAudienceType().withName("myType").withFieldName("firstName").withSourceTableName("notATable"))
      )), "unrecognized sourceTableName");

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withAudienceType(new ShareableAudienceType().withName("myType").withFieldName("firstName").withSourceTableName(TestUtils.TABLE_NAME_SHAPE).withSourceTableKeyFieldName("notAField"))
      )), "unrecognized sourceTableKeyFieldName");
      */

      assertValidationFailureReasonsAllowingExtraReasons(qInstance -> qInstance.addTable(newTable().withShareableTableMetaData(new ShareableTableMetaData()
         .withThisTableOwnerIdFieldName("notAField")
      )), "unrecognized thisTableOwnerIdFieldName");

      assertValidationSuccess(qInstance -> qInstance.addTable(newTable()
         .withField(new QFieldMetaData("userId", QFieldType.INTEGER))
         .withShareableTableMetaData(new ShareableTableMetaData()
            .withSharedRecordTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
            .withAssetIdFieldName("firstName")
            .withScopeFieldName("firstName")
            .withThisTableOwnerIdFieldName("userId")
            .withAudienceTypesPossibleValueSourceName(TestUtils.POSSIBLE_VALUE_SOURCE_STATE)
            .withAudienceType(new ShareableAudienceType().withName("myType").withFieldName("lastName").withSourceTableName(TestUtils.TABLE_NAME_SHAPE).withSourceTableKeyFieldName("id"))
         )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QTableMetaData newTable()
   {
      QTableMetaData tableMetaData = new QTableMetaData()
         .withName("A")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id");

      tableMetaData.addField(new QFieldMetaData("id", QFieldType.INTEGER));

      return (tableMetaData);
   }

}