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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsOutput;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 ** For a given table (name) and API version, return the list of fields that apply
 ** for the API.
 *******************************************************************************/
public class GetTableApiFieldsAction extends AbstractQActionFunction<GetTableApiFieldsInput, GetTableApiFieldsOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public GetTableApiFieldsOutput execute(GetTableApiFieldsInput input) throws QException
   {
      List<QFieldMetaData> fields = new ArrayList<>();

      QTableMetaData table = QContext.getQInstance().getTable(input.getTableName());
      if(table == null)
      {
         throw (new QException("Unrecognized table name: " + input.getTableName()));
      }

      // todo - verify the table is in this version?

      APIVersion version = new APIVersion(input.getVersion());
      // todo - validate the version?

      ///////////////////////////////////////////////////////
      // get fields on the table which are in this version //
      ///////////////////////////////////////////////////////
      List<QFieldMetaData> fieldList = new ArrayList<>(table.getFields().values());
      fieldList.sort(Comparator.comparing(QFieldMetaData::getLabel));
      for(QFieldMetaData field : fieldList)
      {
         if(!isExcluded(field) && getApiVersionRange(field).includes(version))
         {
            fields.add(field);
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////////////////
      // look for removed fields (e.g., not currently in the table anymore), that are in this version //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      for(QFieldMetaData field : CollectionUtils.nonNullList(getRemovedApiFields(table)))
      {
         if(!isExcluded(field) && getApiVersionRangeForRemovedField(field).includes(version))
         {
            fields.add(field);
         }
      }

      return (new GetTableApiFieldsOutput().withFields(fields));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean isExcluded(QFieldMetaData field)
   {
      ApiFieldMetaData apiFieldMetaData = ApiFieldMetaData.of(field);
      if(apiFieldMetaData != null && BooleanUtils.isTrue(apiFieldMetaData.getIsExcluded()))
      {
         return (true);
      }

      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private APIVersionRange getApiVersionRangeForRemovedField(QFieldMetaData field)
   {
      ApiFieldMetaData middlewareMetaData = ApiFieldMetaData.of(field);
      if(middlewareMetaData != null && middlewareMetaData.getInitialVersion() != null)
      {
         if(StringUtils.hasContent(middlewareMetaData.getFinalVersion()))
         {
            return (APIVersionRange.betweenAndIncluding(middlewareMetaData.getInitialVersion(), middlewareMetaData.getFinalVersion()));
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
   private APIVersionRange getApiVersionRange(QFieldMetaData field)
   {
      ApiFieldMetaData middlewareMetaData = ApiFieldMetaData.of(field);
      if(middlewareMetaData != null && middlewareMetaData.getInitialVersion() != null)
      {
         return (APIVersionRange.afterAndIncluding(middlewareMetaData.getInitialVersion()));
      }

      return (APIVersionRange.none());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QFieldMetaData> getRemovedApiFields(QTableMetaData table)
   {
      ApiTableMetaData apiTableMetaData = ApiTableMetaData.of(table);
      if(apiTableMetaData != null)
      {
         return (apiTableMetaData.getRemovedApiFields());
      }
      return (null);
   }
}