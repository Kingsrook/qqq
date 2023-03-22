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

package com.kingsrook.qqq.api.model.metadata;


import java.util.List;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ApiInstanceMetaData
 *******************************************************************************/
class ApiInstanceMetaDataTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidationPasses()
   {
      assertValidationErrors(new ApiInstanceMetaData()
            .withName("QQQ API")
            .withDescription("Test API for QQQ")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion("2023.Q1"))
            .withSupportedVersions(List.of(new APIVersion("2022.Q3"), new APIVersion("2022.Q4"), new APIVersion("2023.Q1")))
            .withPastVersions(List.of(new APIVersion("2022.Q2"), new APIVersion("2022.Q3"), new APIVersion("2022.Q4")))
            .withFutureVersions(List.of(new APIVersion("2023.Q2"))),
         List.of());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidationMissingThings()
   {
      assertValidationErrors(new ApiInstanceMetaData(), List.of(
         "Missing name",
         "Missing description",
         "Missing contactEmail",
         "Missing currentVersion",
         "Missing supportedVersions"
      ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidationInstanceVersionIssues()
   {
      assertValidationErrors(makeBaselineValidApiInstanceMetaData()
            .withCurrentVersion(new APIVersion("2023.Q1"))
            .withSupportedVersions(List.of(new APIVersion("2022.Q3"), new APIVersion("2022.Q4")))
         , List.of("supportedVersions [[2022.Q3, 2022.Q4]] does not contain currentVersion [2023.Q1]"));

      assertValidationErrors(makeBaselineValidApiInstanceMetaData()
            .withCurrentVersion(new APIVersion("2023.Q1"))
            .withSupportedVersions(List.of(new APIVersion("2023.Q1")))
            .withPastVersions(List.of(new APIVersion("2022.Q4"), new APIVersion("2023.Q1"), new APIVersion("2023.Q2"))),
         List.of(
            "pastVersion [2023.Q2] is not lexicographically before currentVersion",
            "pastVersion [2023.Q1] is not lexicographically before currentVersion"
         ));

      assertValidationErrors(makeBaselineValidApiInstanceMetaData()
            .withCurrentVersion(new APIVersion("2023.Q1"))
            .withSupportedVersions(List.of(new APIVersion("2023.Q1")))
            .withFutureVersions(List.of(new APIVersion("2022.Q4"), new APIVersion("2023.Q1"), new APIVersion("2023.Q2"))),
         List.of(
            "futureVersion [2022.Q4] is not lexicographically after currentVersion",
            "futureVersion [2023.Q1] is not lexicographically after currentVersion"
         ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidationTableVersionIssues()
   {
      QInstance qInstance = new QInstance();

      qInstance.addTable(new QTableMetaData()
         .withName("myValidTable")
         .withMiddlewareMetaData(new ApiTableMetaData().withInitialVersion("2023.Q1")));

      qInstance.addTable(new QTableMetaData()
         .withName("myInvalidTable")
         .withMiddlewareMetaData(new ApiTableMetaData().withInitialVersion("2022.Q1")));

      assertValidationErrors(qInstance, makeBaselineValidApiInstanceMetaData()
            .withCurrentVersion(new APIVersion("2023.Q1"))
            .withSupportedVersions(List.of(new APIVersion("2022.Q4"), new APIVersion("2023.Q1"))),
         List.of("Table myInvalidTable's initial API version is not a recognized version"));

      qInstance.addTable(new QTableMetaData()
         .withName("myFutureValidTable")
         .withMiddlewareMetaData(new ApiTableMetaData().withInitialVersion("2024.Q1")));

      assertValidationErrors(qInstance, makeBaselineValidApiInstanceMetaData()
            .withCurrentVersion(new APIVersion("2023.Q1"))
            .withSupportedVersions(List.of(new APIVersion("2023.Q1")))
            .withFutureVersions(List.of(new APIVersion("2024.Q1"))),
         List.of("Table myInvalidTable's initial API version is not a recognized version"));

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ApiInstanceMetaData makeBaselineValidApiInstanceMetaData()
   {
      return (new ApiInstanceMetaData()
         .withName("QQQ API")
         .withDescription("Test API for QQQ")
         .withContactEmail("contact@kingsrook.com"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertValidationErrors(ApiInstanceMetaData apiInstanceMetaData, List<String> expectedErrors)
   {
      QInstance qInstance = new QInstance();
      assertValidationErrors(qInstance, apiInstanceMetaData, expectedErrors);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertValidationErrors(QInstance qInstance, ApiInstanceMetaData apiInstanceMetaData, List<String> expectedErrors)
   {
      qInstance.withMiddlewareMetaData(apiInstanceMetaData);

      QInstanceValidator validator = new QInstanceValidator();
      apiInstanceMetaData.validate(qInstance, validator);

      List<String> errors = validator.getErrors();
      assertEquals(expectedErrors.size(), errors.size(), "Expected # of validation errors (got: " + errors + ")");

      for(String expectedError : expectedErrors)
      {
         assertThat(errors).withFailMessage("Expected any of:\n   " + StringUtils.join("\n   ", errors) + "\nto contain:\n   " + expectedError).anyMatch(e -> e.contains(expectedError));
      }
   }
}