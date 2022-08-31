package com.kingsrook.qqq.backend.core.model.metadata.tables.automation;


import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Definition of a specific action to run against a table
 *******************************************************************************/
public class TableAutomationAction
{
   private String       name;
   private TriggerEvent triggerEvent;
   private Integer      priority = 500;
   private QQueryFilter filter;

   ////////////////////////////////
   // mutually-exclusive options //
   ////////////////////////////////
   private QCodeReference codeReference;
   private String         processName;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "TableAutomationAction{name='" + name + "'}";}



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
   public TableAutomationAction withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for triggerEvent
    **
    *******************************************************************************/
   public TriggerEvent getTriggerEvent()
   {
      return triggerEvent;
   }



   /*******************************************************************************
    ** Setter for triggerEvent
    **
    *******************************************************************************/
   public void setTriggerEvent(TriggerEvent triggerEvent)
   {
      this.triggerEvent = triggerEvent;
   }


   /*******************************************************************************
    ** Fluent setter for triggerEvent
    **
    *******************************************************************************/
   public TableAutomationAction withTriggerEvent(TriggerEvent triggerEvent)
   {
      this.triggerEvent = triggerEvent;
      return (this);
   }



   /*******************************************************************************
    ** Getter for priority
    **
    *******************************************************************************/
   public Integer getPriority()
   {
      return priority;
   }



   /*******************************************************************************
    ** Setter for priority
    **
    *******************************************************************************/
   public void setPriority(Integer priority)
   {
      this.priority = priority;
   }


   /*******************************************************************************
    ** Fluent setter for priority
    **
    *******************************************************************************/
   public TableAutomationAction withPriority(Integer priority)
   {
      this.priority = priority;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filter
    **
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return filter;
   }



   /*******************************************************************************
    ** Setter for filter
    **
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }


   /*******************************************************************************
    ** Fluent setter for filter
    **
    *******************************************************************************/
   public TableAutomationAction withFilter(QQueryFilter filter)
   {
      this.filter = filter;
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
   public TableAutomationAction withCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processName
    **
    *******************************************************************************/
   public String getProcessName()
   {
      return processName;
   }



   /*******************************************************************************
    ** Setter for processName
    **
    *******************************************************************************/
   public void setProcessName(String processName)
   {
      this.processName = processName;
   }


   /*******************************************************************************
    ** Fluent setter for processName
    **
    *******************************************************************************/
   public TableAutomationAction withProcessName(String processName)
   {
      this.processName = processName;
      return (this);
   }

}
