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
 ** Field Mapping implementation that uses Integer keys (e.g., from a CSV file
 ** WITHOUT a header row).
 **
 ** Note:  1-based index!!
 **
 *******************************************************************************/
public class QIndexBasedFieldMapping extends AbstractQFieldMapping<Integer>
{
   private Map<String, Integer> mapping;



   /*******************************************************************************
    ** Get the field source  (e.g., integer index of a CSV column) corresponding to a 
    ** propery qqq table fieldName.
    **
    *******************************************************************************/
   @Override
   public Integer getFieldSource(String fieldName)
   {
      if(mapping == null)
      {
         return (null);
      }

      return (mapping.get(fieldName));
   }



   /*******************************************************************************
    ** Tell framework what kind of keys this mapping class uses (INDEX)
    **
    *******************************************************************************/
   @Override
   public SourceType getSourceType()
   {
      return (SourceType.INDEX);
   }



   /*******************************************************************************
    ** Add a single mapping to this mapping object.  fieldName = qqq metaData fieldName,
    ** key = field index (integer) in the CSV
    **
    *******************************************************************************/
   public void addMapping(String fieldName, Integer key)
   {
      if(mapping == null)
      {
         mapping = new LinkedHashMap<>();
      }
      mapping.put(fieldName, key);
   }



   /*******************************************************************************
    ** Fluently add a single mapping to this mapping object.  fieldName = qqq metaData 
    ** fieldName, key = field index (integer) in the CSV
    **
    *******************************************************************************/
   public QIndexBasedFieldMapping withMapping(String fieldName, Integer key)
   {
      addMapping(fieldName, key);
      return (this);
   }



   /*******************************************************************************
    ** Getter for mapping
    **
    *******************************************************************************/
   public Map<String, Integer> getMapping()
   {
      return mapping;
   }



   /*******************************************************************************
    ** Setter for mapping
    **
    *******************************************************************************/
   public void setMapping(Map<String, Integer> mapping)
   {
      this.mapping = mapping;
   }

}
