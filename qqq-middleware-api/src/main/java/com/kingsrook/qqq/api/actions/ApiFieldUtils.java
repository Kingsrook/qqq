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

package com.kingsrook.qqq.api.actions;


import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QVirtualFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 ** utility methods for working with fields
 **
 *******************************************************************************/
public class ApiFieldUtils
{


   /*******************************************************************************
    **
    *******************************************************************************/
   public static boolean isIncluded(String apiName, QFieldMetaData field)
   {
      ApiFieldMetaData apiFieldMetaData = getApiFieldMetaData(apiName, field);
      if(apiFieldMetaData != null && BooleanUtils.isTrue(apiFieldMetaData.getIsExcluded()))
      {
         return (false);
      }

      return (true);
   }



   /*******************************************************************************
    * determine if a virtual field should be included in the api.
    *
    * <p>Note that the logic here is stricter than for normal fields. a virtual field
    * must explicitly have api field meta data applied to it to be in the api.</p>
    *******************************************************************************/
   public static boolean isVirtualFieldIncluded(String apiName, QVirtualFieldMetaData field)
   {
      ApiFieldMetaData apiFieldMetaData = getApiFieldMetaData(apiName, field);
      if(apiFieldMetaData != null && !BooleanUtils.isTrue(apiFieldMetaData.getIsExcluded()))
      {
         return (true);
      }

      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static APIVersionRange getApiVersionRangeForRemovedField(String apiName, QFieldMetaData field)
   {
      ApiFieldMetaData apiFieldMetaData = getApiFieldMetaData(apiName, field);
      if(apiFieldMetaData != null && apiFieldMetaData.getInitialVersion() != null)
      {
         if(StringUtils.hasContent(apiFieldMetaData.getFinalVersion()))
         {
            return (APIVersionRange.betweenAndIncluding(apiFieldMetaData.getInitialVersion(), apiFieldMetaData.getFinalVersion()));
         }
         else
         {
            throw (new IllegalStateException("RemovedApiFieldMetaData for field [" + field.getName() + "] did not specify a finalVersion."));
         }
      }
      else
      {
         throw (new IllegalStateException("RemovedApiFieldMetaData for field [" + field.getName() + "] did not specify an initialVersion."));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static APIVersionRange getApiVersionRange(String apiName, QFieldMetaData field)
   {
      ApiFieldMetaData apiFieldMetaData = getApiFieldMetaData(apiName, field);
      if(apiFieldMetaData != null && apiFieldMetaData.getInitialVersion() != null)
      {
         return (APIVersionRange.afterAndIncluding(apiFieldMetaData.getInitialVersion()));
      }

      return (APIVersionRange.none());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ApiFieldMetaData getApiFieldMetaData(String apiName, QFieldMetaData field)
   {
      return ObjectUtils.tryAndRequireNonNullElse(() -> ApiFieldMetaDataContainer.of(field).getApiFieldMetaData(apiName), new ApiFieldMetaData());
   }
}
