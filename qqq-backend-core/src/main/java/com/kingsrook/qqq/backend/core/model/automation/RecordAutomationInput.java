package com.kingsrook.qqq.backend.core.model.automation;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;


/*******************************************************************************
 ** Input data for the RecordAutomationHandler interface.
 *******************************************************************************/
public class RecordAutomationInput extends AbstractTableActionInput
{
   private TableAutomationAction action;

   ////////////////////////////////////////////
   // todo - why both?  pick one?  or don't? //
   // maybe - if recordList is null and primaryKeyList isn't, then do the record query in here?
   ////////////////////////////////////////////
   private List<QRecord>      recordList;
   private List<Serializable> primaryKeyList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RecordAutomationInput(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    ** Getter for action
    **
    *******************************************************************************/
   public TableAutomationAction getAction()
   {
      return action;
   }



   /*******************************************************************************
    ** Setter for action
    **
    *******************************************************************************/
   public void setAction(TableAutomationAction action)
   {
      this.action = action;
   }



   /*******************************************************************************
    ** Fluent setter for action
    **
    *******************************************************************************/
   public RecordAutomationInput withAction(TableAutomationAction action)
   {
      this.action = action;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordList
    **
    *******************************************************************************/
   public List<QRecord> getRecordList()
   {
      return recordList;
   }



   /*******************************************************************************
    ** Setter for recordList
    **
    *******************************************************************************/
   public void setRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
   }



   /*******************************************************************************
    ** Fluent setter for recordList
    **
    *******************************************************************************/
   public RecordAutomationInput withRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for primaryKeyList
    **
    *******************************************************************************/
   public List<Serializable> getPrimaryKeyList()
   {
      return primaryKeyList;
   }



   /*******************************************************************************
    ** Setter for primaryKeyList
    **
    *******************************************************************************/
   public void setPrimaryKeyList(List<Serializable> primaryKeyList)
   {
      this.primaryKeyList = primaryKeyList;
   }



   /*******************************************************************************
    ** Fluent setter for primaryKeyList
    **
    *******************************************************************************/
   public RecordAutomationInput withPrimaryKeyList(List<Serializable> primaryKeyList)
   {
      this.primaryKeyList = primaryKeyList;
      return (this);
   }

}
