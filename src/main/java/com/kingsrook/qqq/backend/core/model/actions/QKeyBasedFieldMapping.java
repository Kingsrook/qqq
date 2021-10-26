package com.kingsrook.qqq.backend.core.model.actions;


import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class QKeyBasedFieldMapping extends AbstractQFieldMapping<String>
{
   private Map<String, String> mapping;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getMappedField(String key)
   {
      if(mapping == null)
      {
         return (null);
      }

      return (mapping.get(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addMapping(String key, String fieldName)
   {
      if(mapping == null)
      {
         mapping = new LinkedHashMap<>();
      }
      mapping.put(key, fieldName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QKeyBasedFieldMapping withMapping(String key, String fieldName)
   {
      addMapping(key, fieldName);
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
