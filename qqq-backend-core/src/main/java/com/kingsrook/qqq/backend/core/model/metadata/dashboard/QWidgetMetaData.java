package com.kingsrook.qqq.backend.core.model.metadata.dashboard;


import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class QWidgetMetaData implements QWidgetMetaDataInterface
{
   protected String         name;
   protected QCodeReference codeReference;



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
   public QWidgetMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeReference
    **
    *******************************************************************************/
   public QCodeReference getCodeReference()
   {
      return codeReference;
   }



   /*******************************************************************************
    ** Setter for codeReference
    **
    *******************************************************************************/
   public void setCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
   }



   /*******************************************************************************
    ** Fluent setter for codeReference
    **
    *******************************************************************************/
   public QWidgetMetaData withCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }

}
