package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** For processes that may show a review & result screen, this class provides a
 ** standard way to summarize information about the records in the process.
 **
 *******************************************************************************/
public class ProcessSummaryLine implements Serializable
{
   private Status  status;
   private Integer count;
   private String  message;

   //////////////////////////////////////////////////////////////////////////
   // using ArrayList, because we need to be Serializable, and List is not //
   //////////////////////////////////////////////////////////////////////////
   private ArrayList<Serializable> primaryKeys;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLine(Status status, Integer count, String message, ArrayList<Serializable> primaryKeys)
   {
      this.status = status;
      this.count = count;
      this.message = message;
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLine(Status status, Integer count, String message)
   {
      this.status = status;
      this.count = count;
      this.message = message;
   }



   /*******************************************************************************
    ** Getter for status
    **
    *******************************************************************************/
   public Status getStatus()
   {
      return status;
   }



   /*******************************************************************************
    ** Setter for status
    **
    *******************************************************************************/
   public void setStatus(Status status)
   {
      this.status = status;
   }



   /*******************************************************************************
    ** Getter for primaryKeys
    **
    *******************************************************************************/
   public List<Serializable> getPrimaryKeys()
   {
      return primaryKeys;
   }



   /*******************************************************************************
    ** Setter for primaryKeys
    **
    *******************************************************************************/
   public void setPrimaryKeys(ArrayList<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    ** Getter for count
    **
    *******************************************************************************/
   public Integer getCount()
   {
      return count;
   }



   /*******************************************************************************
    ** Setter for count
    **
    *******************************************************************************/
   public void setCount(Integer count)
   {
      this.count = count;
   }



   /*******************************************************************************
    ** Getter for message
    **
    *******************************************************************************/
   public String getMessage()
   {
      return message;
   }



   /*******************************************************************************
    ** Setter for message
    **
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void incrementCount()
   {
      if(count == null)
      {
         count = 0;
      }
      count++;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void incrementCountAndAddPrimaryKey(Serializable primaryKey)
   {
      incrementCount();

      if(primaryKeys == null)
      {
         primaryKeys = new ArrayList<>();
      }
      primaryKeys.add(primaryKey);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addSelfToListIfAnyCount(ArrayList<ProcessSummaryLine> rs)
   {
      if(count != null && count > 0)
      {
         rs.add(this);
      }
   }
}
