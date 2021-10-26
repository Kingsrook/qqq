package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class QTableMetaData
{
   private String name;
   private String backendName;
   private String primaryKeyField;
   private List<QFieldMetaData> fields;
   private Map<String, QFieldMetaData> _fieldMap;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData getField(String fieldName)
   {
      if(fields == null)
      {
         throw (new IllegalArgumentException("Table [" + name + "] does not have its fields defined."));
      }

      QFieldMetaData field = getFieldMap().get(fieldName);
      if(field == null)
      {
         throw (new IllegalArgumentException("Field [" + fieldName + "] was not found in table [" + name + "]."));
      }

      return (field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, QFieldMetaData> getFieldMap()
   {
      if(_fieldMap == null)
      {
         _fieldMap = new LinkedHashMap<>();
         for(QFieldMetaData field : fields)
         {
            _fieldMap.put(field.getName(), field);
         }
      }

      return (_fieldMap);
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
   public List<QFieldMetaData> getFields()
   {
      return fields;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setFields(List<QFieldMetaData> fields)
   {
      this.fields = fields;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withFields(List<QFieldMetaData> fields)
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
         this.fields = new ArrayList<>();
      }
      this.fields.add(field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData withField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new ArrayList<>();
      }
      this.fields.add(field);
      return (this);
   }

}
