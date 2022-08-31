package com.kingsrook.qqq.backend.core.actions.automation;


import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;


/*******************************************************************************
 ** enum of possible values for a record's Automation Status.
 *******************************************************************************/
public enum AutomationStatus implements PossibleValueEnum<Integer>
{
   PENDING_INSERT_AUTOMATIONS(1, "Pending Insert Automations"),
   RUNNING_INSERT_AUTOMATIONS(2, "Running Insert Automations"),
   FAILED_INSERT_AUTOMATIONS(3, "Failed Insert Automations"),
   PENDING_UPDATE_AUTOMATIONS(4, "Pending Update Automations"),
   RUNNING_UPDATE_AUTOMATIONS(5, "Running Update Automations"),
   FAILED_UPDATE_AUTOMATIONS(6, "Failed Update Automations"),
   OK(7, "OK");


   private final Integer id;
   private final String  label;



   /*******************************************************************************
    **
    *******************************************************************************/
   AutomationStatus(int id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return (id);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return (label);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getPossibleValueId()
   {
      return (getId());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (getLabel());
   }
}
