/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.query;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQTableRequest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Request data for the Query action
 **
 *******************************************************************************/
public class QueryRequest extends AbstractQTableRequest
{
   private QQueryFilter filter;
   private Integer skip;
   private Integer limit;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryRequest(QInstance instance)
   {
      super(instance);
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
    ** Getter for skip
    **
    *******************************************************************************/
   public Integer getSkip()
   {
      return skip;
   }



   /*******************************************************************************
    ** Setter for skip
    **
    *******************************************************************************/
   public void setSkip(Integer skip)
   {
      this.skip = skip;
   }



   /*******************************************************************************
    ** Getter for limit
    **
    *******************************************************************************/
   public Integer getLimit()
   {
      return limit;
   }



   /*******************************************************************************
    ** Setter for limit
    **
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }
}
