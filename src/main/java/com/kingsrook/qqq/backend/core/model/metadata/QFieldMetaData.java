package com.kingsrook.qqq.backend.core.model.metadata;


/*******************************************************************************
 ** Meta-data to represent a single field in a table.
 **
 *******************************************************************************/
public class QFieldMetaData
{
   private String name;
   private String label;
   private String backendName;
   private QFieldType type;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData(String name, QFieldType type)
   {
      this.name = name;
      this.type = type;
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
   public QFieldMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public QFieldType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(QFieldType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withType(QFieldType type)
   {
      this.type = type;
      return (this);
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
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData withLabel(String label)
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
   public QFieldMetaData withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }
}
