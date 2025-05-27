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

package com.kingsrook.qqq.api.middleware.executors;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.api.model.actions.ApiFieldCustomValueMapper;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.exceptions.QBadRequestException;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrCountInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** shared code for query & count executors
 *******************************************************************************/
public class QueryExecutorUtils
{

   /***************************************************************************
    **
    ***************************************************************************/
   static void manageCriteriaFields(QQueryFilter filter, Map<String, QFieldMetaData> tableApiFields, List<String> badRequestMessages, String apiName, QueryOrCountInputInterface input)
   {
      for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         String         apiFieldName = criteria.getFieldName();
         QFieldMetaData field        = tableApiFields.get(apiFieldName);
         if(field == null)
         {
            badRequestMessages.add("Unrecognized criteria field name: " + apiFieldName + ".");
         }
         else
         {
            try
            {
               ApiFieldMetaData apiFieldMetaData = ObjectUtils.tryAndRequireNonNullElse(() -> ApiFieldMetaDataContainer.of(field).getApiFieldMetaData(apiName), new ApiFieldMetaData());
               if(StringUtils.hasContent(apiFieldMetaData.getReplacedByFieldName()))
               {
                  criteria.setFieldName(apiFieldMetaData.getReplacedByFieldName());
               }
               else if(apiFieldMetaData.getCustomValueMapper() != null)
               {
                  ApiFieldCustomValueMapper customValueMapper = QCodeLoader.getAdHoc(ApiFieldCustomValueMapper.class, apiFieldMetaData.getCustomValueMapper());
                  customValueMapper.customizeFilterCriteriaForQueryOrCount(input, filter, criteria, apiFieldName, apiFieldMetaData);
               }
            }
            catch(Exception e)
            {
               badRequestMessages.add("Error processing criteria field " + apiFieldName + ": " + e.getMessage());
            }
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   static void throwIfBadRequestMessages(List<String> badRequestMessages) throws QBadRequestException
   {
      if(!badRequestMessages.isEmpty())
      {
         if(badRequestMessages.size() == 1)
         {
            throw (new QBadRequestException(badRequestMessages.get(0)));
         }
         else
         {
            throw (new QBadRequestException("Request failed with " + badRequestMessages.size() + " reasons: " + StringUtils.join("\n", badRequestMessages)));
         }
      }
   }

}
