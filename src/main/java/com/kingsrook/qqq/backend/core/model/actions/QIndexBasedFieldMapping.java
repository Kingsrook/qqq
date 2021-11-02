package com.kingsrook.qqq.backend.core.model.actions;


import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 ** Note;  1-based index!!
 *******************************************************************************/
public class QIndexBasedFieldMapping extends AbstractQFieldMapping<Integer>
{
   private Map<String, Integer> mapping;



   /*******************************************************************************
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
    **
    *******************************************************************************/
   @Override
   public SourceType getSourceType()
   {
      return (SourceType.INDEX);
   }



   /*******************************************************************************
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
