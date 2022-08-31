package com.kingsrook.qqq.backend.core.model.metadata.tables.automation;


/*******************************************************************************
 ** Table-automation meta-data to define how this table's per-record automation
 ** status is tracked.
 *******************************************************************************/
public class AutomationStatusTracking
{
   private AutomationStatusTrackingType type;

   private String fieldName; // used when type is FIELD_IN_TABLE

   // todo - fields for additional types (e.g., 1-1 table, shared-table)



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public AutomationStatusTrackingType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(AutomationStatusTrackingType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public AutomationStatusTracking withType(AutomationStatusTrackingType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldName
    **
    *******************************************************************************/
   public String getFieldName()
   {
      return fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    **
    *******************************************************************************/
   public AutomationStatusTracking withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }

}
