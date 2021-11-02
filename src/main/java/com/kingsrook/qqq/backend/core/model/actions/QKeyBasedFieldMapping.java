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
   public String getFieldSource(String fieldName)
   {
      if(mapping == null)
      {
         return (null);
      }

      return (mapping.get(fieldName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public SourceType getSourceType()
   {
      return (SourceType.KEY);
   }



   /*******************************************************************************
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
