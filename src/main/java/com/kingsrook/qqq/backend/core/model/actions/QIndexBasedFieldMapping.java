/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions;


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
