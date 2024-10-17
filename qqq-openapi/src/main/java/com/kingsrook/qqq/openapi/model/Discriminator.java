package com.kingsrook.qqq.openapi.model;


import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class Discriminator
{
   private String              propertyName;
   private Map<String, String> mapping;



   /*******************************************************************************
    ** Getter for propertyName
    *******************************************************************************/
   public String getPropertyName()
   {
      return (this.propertyName);
   }



   /*******************************************************************************
    ** Setter for propertyName
    *******************************************************************************/
   public void setPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
   }



   /*******************************************************************************
    ** Fluent setter for propertyName
    *******************************************************************************/
   public Discriminator withPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
      return (this);
   }


   /*******************************************************************************
    ** Getter for mapping
    *******************************************************************************/
   public Map<String, String> getMapping()
   {
      return (this.mapping);
   }



   /*******************************************************************************
    ** Setter for mapping
    *******************************************************************************/
   public void setMapping(Map<String, String> mapping)
   {
      this.mapping = mapping;
   }



   /*******************************************************************************
    ** Fluent setter for mapping
    *******************************************************************************/
   public Discriminator withMapping(Map<String, String> mapping)
   {
      this.mapping = mapping;
      return (this);
   }


}
