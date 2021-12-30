/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.query;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 * Full "filter" for a query - a list of criteria and order-bys
 *
 *******************************************************************************/
public class QQueryFilter
{
   private List<QFilterCriteria> criteria = new ArrayList<>();
   private List<QFilterOrderBy> orderBys = new ArrayList<>();



   /*******************************************************************************
    ** Getter for criteria
    **
    *******************************************************************************/
   public List<QFilterCriteria> getCriteria()
   {
      return criteria;
   }



   /*******************************************************************************
    ** Setter for criteria
    **
    *******************************************************************************/
   public void setCriteria(List<QFilterCriteria> criteria)
   {
      this.criteria = criteria;
   }



   /*******************************************************************************
    ** Getter for order
    **
    *******************************************************************************/
   public List<QFilterOrderBy> getOrderBys()
   {
      return orderBys;
   }



   /*******************************************************************************
    ** Setter for order
    **
    *******************************************************************************/
   public void setOrderBys(List<QFilterOrderBy> orderBys)
   {
      this.orderBys = orderBys;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addCriteria(QFilterCriteria qFilterCriteria)
   {
      if(criteria == null)
      {
         criteria = new ArrayList<>();
      }
      criteria.add(qFilterCriteria);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueryFilter withCriteria(QFilterCriteria qFilterCriteria)
   {
      addCriteria(qFilterCriteria);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addOrderBy(QFilterOrderBy qFilterOrderBy)
   {
      if(orderBys == null)
      {
         orderBys = new ArrayList<>();
      }
      orderBys.add(qFilterOrderBy);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueryFilter withOrderBy(QFilterOrderBy qFilterOrderBy)
   {
      addOrderBy(qFilterOrderBy);
      return (this);
   }

}
