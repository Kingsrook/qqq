package com.kingsrook.qqq.backend.core.model.metadata.automation;


/*******************************************************************************
 ** Meta-data definition of a qqq service to drive record automations.
 *******************************************************************************/
public class QAutomationProviderMetaData
{
   private String name;
   private QAutomationProviderType type;


   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }


   /*******************************************************************************
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QAutomationProviderMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public QAutomationProviderType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(QAutomationProviderType type)
   {
      this.type = type;
   }


   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public QAutomationProviderMetaData withType(QAutomationProviderType type)
   {
      this.type = type;
      return (this);
   }

}
