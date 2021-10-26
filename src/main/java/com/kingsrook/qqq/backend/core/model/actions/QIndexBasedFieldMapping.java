package com.kingsrook.qqq.backend.core.model.actions;


import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 ** Note;  1-based index!!
 *******************************************************************************/
public class QIndexBasedFieldMapping extends AbstractQFieldMapping<Integer>
{
   private Map<Integer, String> mapping;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getMappedField(Integer key)
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
   public void addMapping(Integer key, String fieldName)
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
   public QIndexBasedFieldMapping withMapping(Integer key, String fieldName)
   {
      addMapping(key, fieldName);
      return (this);
   }



   /*******************************************************************************
    ** Getter for mapping
    **
    *******************************************************************************/
   public Map<Integer, String> getMapping()
   {
      return mapping;
   }



   /*******************************************************************************
    ** Setter for mapping
    **
    *******************************************************************************/
   public void setMapping(Map<Integer, String> mapping)
   {
      this.mapping = mapping;
   }

}
