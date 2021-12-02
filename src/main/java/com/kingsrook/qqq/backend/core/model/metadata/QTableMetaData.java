package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 ** Meta-Data to define a table in a QQQ instance.
 **
 *******************************************************************************/
public class QTableMetaData
{
   private String name;
   private String label;
   private String backendName;
   private String primaryKeyField;
   private Map<String, QFieldMetaData> fields;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData getField(String fieldName)
   {
      if(fields == null)
      {
         throw (new IllegalArgumentException("Table [" + name + "] does not have its fields defined."));
      }

      QFieldMetaData field = getFields().get(fieldName);
      if(field == null)
      {
         throw (new IllegalArgumentException("Field [" + fieldName + "] was not found in table [" + name + "]."));
      }

      return (field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendName
    **
    *******************************************************************************/
   public String getBackendName()
   {
      return backendName;
   }



   /*******************************************************************************
    ** Setter for backendName
    **
    *******************************************************************************/
   public void setBackendName(String backendName)
   {
      this.backendName = backendName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getPrimaryKeyField()
   {
      return primaryKeyField;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setPrimaryKeyField(String primaryKeyField)
   {
      this.primaryKeyField = primaryKeyField;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withPrimaryKeyField(String primaryKeyField)
   {
      this.primaryKeyField = primaryKeyField;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, QFieldMetaData> getFields()
   {
      return fields;
   }



   /*******************************************************************************
    ** Setter for fields
    **
    *******************************************************************************/
   public void setFields(Map<String, QFieldMetaData> fields)
   {
      this.fields = fields;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withFields(Map<String, QFieldMetaData> fields)
   {
      this.fields = fields;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new LinkedHashMap<>();
      }
      this.fields.put(field.getName(), field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new LinkedHashMap<>();
      }
      this.fields.put(field.getName(), field);
      return (this);
   }

}
