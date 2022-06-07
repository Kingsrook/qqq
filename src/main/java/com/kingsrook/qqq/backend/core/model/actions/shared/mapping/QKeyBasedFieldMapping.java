/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.actions.shared.mapping;


import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 ** Field Mapping implementation that uses string keys (e.g., from a CSV file
 ** with a header row, or from one JSON object to the proper qqq field names)
 **
 *******************************************************************************/
public class QKeyBasedFieldMapping extends AbstractQFieldMapping<String>
{
   private Map<String, String> mapping;



   /*******************************************************************************
    ** Get the source field (e.g., name that's in the CSV header or the input json
    ** object) corresponding to a proper qqq table fieldName.
    **
    *******************************************************************************/
   @Override
   public String getFieldSource(String fieldName)
   {
      if(mapping == null)
      {
         return (null);
      }

      return (mapping.get(fieldName));
   }



   /*******************************************************************************
    ** Tell framework what kind of keys this mapping class uses (KEY)
    **
    *******************************************************************************/
   @Override
   public SourceType getSourceType()
   {
      return (SourceType.KEY);
   }



   /*******************************************************************************
    ** Add a single mapping to this mapping object.  fieldName = qqq metaData fieldName,
    ** key = field name in the CSV or source-json, for example.
    **
    *******************************************************************************/
   public void addMapping(String fieldName, String key)
   {
      if(mapping == null)
      {
         mapping = new LinkedHashMap<>();
      }
      mapping.put(fieldName, key);
   }



   /*******************************************************************************
    ** Fluently add a single mapping to this mapping object.  fieldName = qqq metaData fieldName,
    ** key = field name in the CSV or source-json, for example.
    **
    *******************************************************************************/
   public QKeyBasedFieldMapping withMapping(String fieldName, String key)
   {
      addMapping(fieldName, key);
      return (this);
   }



   /*******************************************************************************
    ** Getter for mapping
    **
    *******************************************************************************/
   public Map<String, String> getMapping()
   {
      return mapping;
   }



   /*******************************************************************************
    ** Setter for mapping
    **
    *******************************************************************************/
   public void setMapping(Map<String, String> mapping)
   {
      this.mapping = mapping;
   }

}
