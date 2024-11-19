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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertWideLayoutMapping
{
   private List<ChildRecordMapping> childRecordMappings;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BulkInsertWideLayoutMapping(List<ChildRecordMapping> childRecordMappings)
   {
      this.childRecordMappings = childRecordMappings;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class ChildRecordMapping
   {
      Map<String, String>                      fieldNameToHeaderNameMaps;
      Map<String, BulkInsertWideLayoutMapping> associationNameToChildRecordMappingMap;



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public ChildRecordMapping(Map<String, String> fieldNameToHeaderNameMaps)
      {
         this.fieldNameToHeaderNameMaps = fieldNameToHeaderNameMaps;
      }



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public ChildRecordMapping(Map<String, String> fieldNameToHeaderNameMaps, Map<String, BulkInsertWideLayoutMapping> associationNameToChildRecordMappingMap)
      {
         this.fieldNameToHeaderNameMaps = fieldNameToHeaderNameMaps;
         this.associationNameToChildRecordMappingMap = associationNameToChildRecordMappingMap;
      }



      /*******************************************************************************
       ** Getter for fieldNameToHeaderNameMaps
       *******************************************************************************/
      public Map<String, String> getFieldNameToHeaderNameMaps()
      {
         return (this.fieldNameToHeaderNameMaps);
      }



      /*******************************************************************************
       ** Setter for fieldNameToHeaderNameMaps
       *******************************************************************************/
      public void setFieldNameToHeaderNameMaps(Map<String, String> fieldNameToHeaderNameMaps)
      {
         this.fieldNameToHeaderNameMaps = fieldNameToHeaderNameMaps;
      }



      /*******************************************************************************
       ** Fluent setter for fieldNameToHeaderNameMaps
       *******************************************************************************/
      public ChildRecordMapping withFieldNameToHeaderNameMaps(Map<String, String> fieldNameToHeaderNameMaps)
      {
         this.fieldNameToHeaderNameMaps = fieldNameToHeaderNameMaps;
         return (this);
      }



      /*******************************************************************************
       ** Getter for associationNameToChildRecordMappingMap
       *******************************************************************************/
      public Map<String, BulkInsertWideLayoutMapping> getAssociationNameToChildRecordMappingMap()
      {
         return (this.associationNameToChildRecordMappingMap);
      }



      /*******************************************************************************
       ** Setter for associationNameToChildRecordMappingMap
       *******************************************************************************/
      public void setAssociationNameToChildRecordMappingMap(Map<String, BulkInsertWideLayoutMapping> associationNameToChildRecordMappingMap)
      {
         this.associationNameToChildRecordMappingMap = associationNameToChildRecordMappingMap;
      }



      /*******************************************************************************
       ** Fluent setter for associationNameToChildRecordMappingMap
       *******************************************************************************/
      public ChildRecordMapping withAssociationNameToChildRecordMappingMap(Map<String, BulkInsertWideLayoutMapping> associationNameToChildRecordMappingMap)
      {
         this.associationNameToChildRecordMappingMap = associationNameToChildRecordMappingMap;
         return (this);
      }



      /***************************************************************************
       **
       ***************************************************************************/
      public Map<String, Integer> getFieldIndexes(BulkLoadFileRow headerRow)
      {
         // todo memoize or otherwise don't recompute
         Map<String, Integer> rs = new HashMap<>();

         ////////////////////////////////////////////////////////
         // for the current file, map header values to indexes //
         ////////////////////////////////////////////////////////
         Map<String, Integer> headerToIndexMap = new HashMap<>();
         for(int i = 0; i < headerRow.size(); i++)
         {
            String headerValue = ValueUtils.getValueAsString(headerRow.getValue(i));
            headerToIndexMap.put(headerValue, i);
         }

         /////////////////////////////////////////////////////////////////////////////////////////////////////////
         // loop over fields - finding what header name they are mapped to - then what index that header is at. //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////
         for(Map.Entry<String, String> entry : fieldNameToHeaderNameMaps.entrySet())
         {
            String headerName = entry.getValue();
            if(headerName != null)
            {
               Integer headerIndex = headerToIndexMap.get(headerName);
               if(headerIndex != null)
               {
                  rs.put(entry.getKey(), headerIndex);
               }
            }
         }

         return (rs);
      }
   }



   /*******************************************************************************
    ** Getter for childRecordMappings
    *******************************************************************************/
   public List<ChildRecordMapping> getChildRecordMappings()
   {
      return (this.childRecordMappings);
   }



   /*******************************************************************************
    ** Setter for childRecordMappings
    *******************************************************************************/
   public void setChildRecordMappings(List<ChildRecordMapping> childRecordMappings)
   {
      this.childRecordMappings = childRecordMappings;
   }



   /*******************************************************************************
    ** Fluent setter for childRecordMappings
    *******************************************************************************/
   public BulkInsertWideLayoutMapping withChildRecordMappings(List<ChildRecordMapping> childRecordMappings)
   {
      this.childRecordMappings = childRecordMappings;
      return (this);
   }

}
