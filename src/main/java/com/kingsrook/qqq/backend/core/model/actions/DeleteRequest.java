package com.kingsrook.qqq.backend.core.model.actions;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 **
 *******************************************************************************/
public class DeleteRequest extends AbstractQTableRequest
{
   private List<Serializable> primaryKeys;



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteRequest(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    ** Getter for ids
    **
    *******************************************************************************/
   public List<Serializable> getPrimaryKeys()
   {
      return primaryKeys;
   }



   /*******************************************************************************
    ** Setter for ids
    **
    *******************************************************************************/
   public void setPrimaryKeys(List<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    ** Setter for ids
    **
    *******************************************************************************/
   public DeleteRequest withPrimaryKeys(List<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
      return (this);
   }
}
