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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsOutput;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import org.apache.commons.lang3.BooleanUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** For a given table (name) and API version, return the list of fields that apply
 ** for the API.
 *******************************************************************************/
public class GetTableApiFieldsAction extends AbstractQActionFunction<GetTableApiFieldsInput, GetTableApiFieldsOutput>
{
   private static final QLogger LOG = QLogger.getLogger(GetTableApiFieldsAction.class);

   private static Map<ApiNameVersionAndTableName, List<QFieldMetaData>>        fieldListCache = new HashMap<>();
   private static Map<ApiNameVersionAndTableName, Map<String, QFieldMetaData>> fieldMapCache  = new HashMap<>();



   /*******************************************************************************
    ** Allow tests (that manipulate meta-data) to clear field caches.
    *******************************************************************************/
   public static void clearCaches()
   {
      fieldListCache.clear();
      fieldMapCache.clear();
   }



   /*******************************************************************************
    ** convenience (and caching) wrapper
    *******************************************************************************/
   public static Map<String, QFieldMetaData> getTableApiFieldMap(ApiNameVersionAndTableName apiNameVersionAndTableName) throws QException
   {
      if(!fieldMapCache.containsKey(apiNameVersionAndTableName))
      {
         List<QFieldMetaData>        tableApiFieldList   = getTableApiFieldList(apiNameVersionAndTableName);
         Map<String, QFieldMetaData> map                 = new LinkedHashMap<>();
         Set<String>                 duplicateFieldNames = new HashSet<>();
         for(QFieldMetaData qFieldMetaData : tableApiFieldList)
         {
            String effectiveApiFieldName = ApiFieldMetaData.getEffectiveApiFieldName(apiNameVersionAndTableName.apiName(), qFieldMetaData);
            if(map.containsKey(effectiveApiFieldName))
            {
               duplicateFieldNames.add(effectiveApiFieldName);
            }
            else
            {
               map.put(effectiveApiFieldName, qFieldMetaData);
            }
         }

         if(!duplicateFieldNames.isEmpty())
         {
            throw (new QException("The field names [" + duplicateFieldNames + "] appear in this api table more than once.  (Do you need to exclude a field that is still in the table, but is also marked as removed?)"));
         }

         fieldMapCache.put(apiNameVersionAndTableName, map);
      }

      return (fieldMapCache.get(apiNameVersionAndTableName));
   }



   /*******************************************************************************
    ** convenience (and caching) wrapper
    *******************************************************************************/
   public static List<QFieldMetaData> getTableApiFieldList(ApiNameVersionAndTableName apiNameVersionAndTableName) throws QException
   {
      if(!fieldListCache.containsKey(apiNameVersionAndTableName))
      {
         List<QFieldMetaData> value = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput()
            .withTableName(apiNameVersionAndTableName.tableName())
            .withVersion(apiNameVersionAndTableName.apiVersion())
            .withApiName(apiNameVersionAndTableName.apiName())).getFields();
         fieldListCache.put(apiNameVersionAndTableName, value);
      }
      return (fieldListCache.get(apiNameVersionAndTableName));
   }



   /*******************************************************************************
    ** Input-record for convenience methods
    *******************************************************************************/
   public record ApiNameVersionAndTableName(String apiName, String apiVersion, String tableName)
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetTableApiFieldsOutput execute(GetTableApiFieldsInput input) throws QException
   {
      List<QFieldMetaData> fields = new ArrayList<>();

      QTableMetaData table = QContext.getQInstance().getTable(input.getTableName());
      if(table == null)
      {
         throw (new QNotFoundException("Unrecognized table name: " + input.getTableName()));
      }

      APIVersion version = new APIVersion(input.getVersion());

      APIVersionRange tableApiVersionRange = getApiVersionRange(input.getApiName(), table);
      if(BooleanUtils.isTrue(input.getDoCheckTableApiVersion()) && !tableApiVersionRange.includes(version))
      {
         throw (new QNotFoundException("Table [" + input.getTableName() + "] was not found in this version of this api."));
      }

      ///////////////////////////////////////////////////////
      // get fields on the table which are in this version //
      ///////////////////////////////////////////////////////
      List<QFieldMetaData> fieldList = new ArrayList<>(table.getFields().values());
      fieldList.sort(Comparator.comparing(QFieldMetaData::getLabel));
      for(QFieldMetaData field : fieldList)
      {
         if(ApiFieldUtils.isIncluded(input.getApiName(), field) && ApiFieldUtils.getApiVersionRange(input.getApiName(), field).includes(version))
         {
            fields.add(field);
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////////////////
      // look for removed fields (e.g., not currently in the table anymore), that are in this version //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      for(QFieldMetaData field : CollectionUtils.nonNullList(getRemovedApiFields(input.getApiName(), table)))
      {
         if(ApiFieldUtils.isIncluded(input.getApiName(), field) && ApiFieldUtils.getApiVersionRangeForRemovedField(input.getApiName(), field).includes(version))
         {
            fields.add(field);
         }
      }

      fields.sort(Comparator.comparing(QFieldMetaData::getLabel));

      return (new GetTableApiFieldsOutput().withFields(fields));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private APIVersionRange getApiVersionRange(String apiName, QTableMetaData table) throws QNotFoundException
   {
      ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
      if(apiTableMetaDataContainer == null)
      {
         LOG.debug("Returning not found because table doesn't have an apiTableMetaDataContainer", logPair("tableName", table.getName()));
         throw (new QNotFoundException("Table [" + table.getName() + "] was not found in this api."));
      }

      ApiTableMetaData apiTableMetaData = apiTableMetaDataContainer.getApis().get(apiName);
      if(apiTableMetaData == null)
      {
         LOG.debug("Returning not found because api isn't present in table's apiTableMetaDataContainer", logPair("apiName", apiName), logPair("tableName", table.getName()));
         throw (new QNotFoundException("Table [" + table.getName() + "] was not found in this api."));
      }

      if(apiTableMetaData.getInitialVersion() != null)
      {
         if(apiTableMetaData.getFinalVersion() != null)
         {
            return (APIVersionRange.betweenAndIncluding(apiTableMetaData.getInitialVersion(), apiTableMetaData.getFinalVersion()));
         }
         else
         {
            return (APIVersionRange.afterAndIncluding(apiTableMetaData.getInitialVersion()));
         }
      }

      return (APIVersionRange.none());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QFieldMetaData> getRemovedApiFields(String apiName, QTableMetaData table)
   {
      ApiTableMetaData apiTableMetaData = ObjectUtils.tryAndRequireNonNullElse(() -> ApiTableMetaDataContainer.of(table).getApiTableMetaData(apiName), new ApiTableMetaData());
      if(apiTableMetaData != null)
      {
         return (apiTableMetaData.getRemovedApiFields());
      }
      return (null);
   }
}
