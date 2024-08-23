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

package com.kingsrook.qqq.api.implementations.savedreports;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessCustomizers;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessInput;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessInputFieldsContainer;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessMetaData;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessMetaDataContainer;
import com.kingsrook.qqq.api.model.openapi.ExampleWithListValue;
import com.kingsrook.qqq.api.model.openapi.ExampleWithSingleValue;
import com.kingsrook.qqq.api.model.openapi.HttpMethod;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Class that helps prepare the RenderSavedReport process for use in an API
 *******************************************************************************/
public class RenderSavedReportProcessApiMetaDataEnricher
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static ApiProcessMetaData setupProcessForApi(QProcessMetaData process, String apiName, String initialApiVersion)
   {
      ApiProcessMetaDataContainer apiProcessMetaDataContainer = ApiProcessMetaDataContainer.ofOrWithNew(process);

      ApiProcessInput input = new ApiProcessInput()
         .withPathParams(new ApiProcessInputFieldsContainer()
            .withField(new QFieldMetaData("reportId", QFieldType.INTEGER)
               .withIsRequired(true)
               .withSupplementalMetaData(newDefaultApiFieldMetaData("Saved Report Id", 1701))))
         .withQueryStringParams(new ApiProcessInputFieldsContainer()
            .withField(new QFieldMetaData("reportFormat", QFieldType.STRING)
               .withIsRequired(true)
               .withPossibleValueSourceName(ReportFormatPossibleValueEnum.NAME)
               .withSupplementalMetaData(newDefaultApiFieldMetaData("Requested file format", "XLSX"))));
      // todo (when implemented) - probably a JSON doc w/ input values.

      RenderSavedReportProcessApiProcessOutput output = new RenderSavedReportProcessApiProcessOutput();

      ApiProcessMetaData apiProcessMetaData = new ApiProcessMetaData()
         .withInitialVersion(initialApiVersion)
         .withCustomizer(ApiProcessCustomizers.PRE_RUN.getRole(), new QCodeReference(RenderSavedReportProcessApiCustomizer.class))
         .withAsyncMode(ApiProcessMetaData.AsyncMode.OPTIONAL)
         .withMethod(HttpMethod.GET)
         .withInput(input)
         .withOutput(output);

      apiProcessMetaDataContainer.withApiProcessMetaData(apiName, apiProcessMetaData);

      return (apiProcessMetaData);
   }



   /*******************************************************************************
    ** todo - move to higher-level utility
    *******************************************************************************/
   public static ApiFieldMetaDataContainer newDefaultApiFieldMetaData(String description, Serializable example)
   {
      ApiFieldMetaData          defaultApiFieldMetaData   = new ApiFieldMetaData().withDescription(description);
      ApiFieldMetaDataContainer apiFieldMetaDataContainer = new ApiFieldMetaDataContainer().withDefaultApiFieldMetaData(defaultApiFieldMetaData);
      if(example instanceof List)
      {
         @SuppressWarnings("unchecked")
         List<String> stringList = (List<String>) example;
         defaultApiFieldMetaData.withExample(new ExampleWithListValue().withValue(stringList));
      }
      else
      {
         defaultApiFieldMetaData.withExample(new ExampleWithSingleValue().withValue(example));
      }

      return (apiFieldMetaDataContainer);
   }

}
