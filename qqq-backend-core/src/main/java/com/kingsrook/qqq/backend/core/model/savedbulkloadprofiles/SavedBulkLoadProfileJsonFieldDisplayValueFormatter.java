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

package com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldDisplayBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedBulkLoadProfileJsonFieldDisplayValueFormatter implements FieldDisplayBehavior<SavedBulkLoadProfileJsonFieldDisplayValueFormatter>
{
   private static SavedBulkLoadProfileJsonFieldDisplayValueFormatter savedReportJsonFieldDisplayValueFormatter = null;



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private SavedBulkLoadProfileJsonFieldDisplayValueFormatter()
   {

   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static SavedBulkLoadProfileJsonFieldDisplayValueFormatter getInstance()
   {
      if(savedReportJsonFieldDisplayValueFormatter == null)
      {
         savedReportJsonFieldDisplayValueFormatter = new SavedBulkLoadProfileJsonFieldDisplayValueFormatter();
      }
      return (savedReportJsonFieldDisplayValueFormatter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public SavedBulkLoadProfileJsonFieldDisplayValueFormatter getDefault()
   {
      return getInstance();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field)
   {
      for(QRecord record : CollectionUtils.nonNullList(recordList))
      {
         if(field.getName().equals("mappingJson"))
         {
            String mappingJson = record.getValueString("mappingJson");
            if(StringUtils.hasContent(mappingJson))
            {
               try
               {
                  record.setDisplayValue("mappingJson", jsonToDisplayValue(mappingJson));
               }
               catch(Exception e)
               {
                  record.setDisplayValue("mappingJson", "Invalid Mapping...");
               }
            }
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String jsonToDisplayValue(String mappingJson)
   {
      JSONObject jsonObject = new JSONObject(mappingJson);

      List<String> parts = new ArrayList<>();

      if(jsonObject.has("fieldList"))
      {
         JSONArray fieldListArray = jsonObject.getJSONArray("fieldList");
         parts.add(fieldListArray.length() + " field" + StringUtils.plural(fieldListArray.length()));
      }

      if(jsonObject.has("hasHeaderRow"))
      {
         boolean hasHeaderRow = jsonObject.getBoolean("hasHeaderRow");
         parts.add((hasHeaderRow ? "With" : "Without") + " header row");
      }

      if(jsonObject.has("layout"))
      {
         String layout = jsonObject.getString("layout");
         parts.add("Layout: " + StringUtils.allCapsToMixedCase(layout));
      }

      return StringUtils.join("; ", parts);
   }

}
