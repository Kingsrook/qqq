package com.kingsrook.qqq.backend.core.model.metadata.frontend;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;


/*******************************************************************************
 * Version of QTableMetaData that's meant for transmitting to a frontend.
 * e.g., it excludes backend-only details.
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendFieldMetaData
{
   private String name;
   private String label;
   private QFieldType type;

   //////////////////////////////////////////////////////////////////////////////////
   // do not add setters.  take values from the source-object in the constructor!! //
   //////////////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendFieldMetaData(QFieldMetaData fieldMetaData)
   {
      this.name = fieldMetaData.getName();
      this.label = fieldMetaData.getLabel();
      this.type = fieldMetaData.getType();
   }



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public QFieldType getType()
   {
      return type;
   }
}
