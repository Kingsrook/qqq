package com.kingsrook.qqq.backend.core.model.metadata.frontend;


import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;


/*******************************************************************************
 * Version of QTableMetaData that's meant for transmitting to a frontend.
 * e.g., it excludes backend-only details.
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendTableMetaData
{
   private String name;
   private String label;
   private String primaryKeyField;
   private Map<String, QFrontendFieldMetaData> fields;

   //////////////////////////////////////////////////////////////////////////////////
   // do not add setters.  take values from the source-object in the constructor!! //
   //////////////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendTableMetaData(QTableMetaData tableMetaData, boolean includeFields)
   {
      this.name = tableMetaData.getName();
      this.label = tableMetaData.getLabel();

      if(includeFields)
      {
         this.primaryKeyField = tableMetaData.getPrimaryKeyField();
         this.fields = new HashMap<>();
         for(Map.Entry<String, QFieldMetaData> entry : tableMetaData.getFields().entrySet())
         {
            this.fields.put(entry.getKey(), new QFrontendFieldMetaData(entry.getValue()));
         }
      }
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
    ** Getter for primaryKeyField
    **
    *******************************************************************************/
   public String getPrimaryKeyField()
   {
      return primaryKeyField;
   }



   /*******************************************************************************
    ** Getter for fields
    **
    *******************************************************************************/
   public Map<String, QFrontendFieldMetaData> getFields()
   {
      return fields;
   }
}