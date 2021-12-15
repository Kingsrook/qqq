/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions;


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
