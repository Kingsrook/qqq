package com.kingsrook.qqq.backend.core.model.metadata.tables.automation;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** Details about how this table's record automations are set up.
 *******************************************************************************/
public class QTableAutomationDetails
{
   private AutomationStatusTracking    statusTracking;
   private String                      providerName;
   private List<TableAutomationAction> actions;



   /*******************************************************************************
    ** Getter for statusTracking
    **
    *******************************************************************************/
   public AutomationStatusTracking getStatusTracking()
   {
      return statusTracking;
   }



   /*******************************************************************************
    ** Setter for statusTracking
    **
    *******************************************************************************/
   public void setStatusTracking(AutomationStatusTracking statusTracking)
   {
      this.statusTracking = statusTracking;
   }



   /*******************************************************************************
    ** Fluent setter for statusTracking
    **
    *******************************************************************************/
   public QTableAutomationDetails withStatusTracking(AutomationStatusTracking statusTracking)
   {
      this.statusTracking = statusTracking;
      return (this);
   }



   /*******************************************************************************
    ** Getter for providerName
    **
    *******************************************************************************/
   public String getProviderName()
   {
      return providerName;
   }



   /*******************************************************************************
    ** Setter for providerName
    **
    *******************************************************************************/
   public void setProviderName(String providerName)
   {
      this.providerName = providerName;
   }



   /*******************************************************************************
    ** Fluent setter for providerName
    **
    *******************************************************************************/
   public QTableAutomationDetails withProviderName(String providerName)
   {
      this.providerName = providerName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for actions
    **
    *******************************************************************************/
   public List<TableAutomationAction> getActions()
   {
      return actions;
   }



   /*******************************************************************************
    ** Setter for actions
    **
    *******************************************************************************/
   public void setActions(List<TableAutomationAction> actions)
   {
      this.actions = actions;
   }



   /*******************************************************************************
    ** Fluent setter for actions
    **
    *******************************************************************************/
   public QTableAutomationDetails withActions(List<TableAutomationAction> actions)
   {
      this.actions = actions;
      return (this);
   }



   /*******************************************************************************
    ** Fluently add an action to this table's automations.
    *******************************************************************************/
   public QTableAutomationDetails withAction(TableAutomationAction action)
   {
      if(this.actions == null)
      {
         this.actions = new ArrayList<>();
      }
      this.actions.add(action);
      return (this);
   }
}
