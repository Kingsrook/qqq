/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.model.metadata.joins;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class QJoinMetaData
{
   private String   name;
   private JoinType type;
   private String   leftTable;
   private String   rightTable;

   private List<JoinOn>         joinOns;
   private List<QFilterOrderBy> orderBys;



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
   public QJoinMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public JoinType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(JoinType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public QJoinMetaData withType(JoinType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for leftTable
    **
    *******************************************************************************/
   public String getLeftTable()
   {
      return leftTable;
   }



   /*******************************************************************************
    ** Setter for leftTable
    **
    *******************************************************************************/
   public void setLeftTable(String leftTable)
   {
      this.leftTable = leftTable;
   }



   /*******************************************************************************
    ** Fluent setter for leftTable
    **
    *******************************************************************************/
   public QJoinMetaData withLeftTable(String leftTable)
   {
      this.leftTable = leftTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for rightTable
    **
    *******************************************************************************/
   public String getRightTable()
   {
      return rightTable;
   }



   /*******************************************************************************
    ** Setter for rightTable
    **
    *******************************************************************************/
   public void setRightTable(String rightTable)
   {
      this.rightTable = rightTable;
   }



   /*******************************************************************************
    ** Fluent setter for rightTable
    **
    *******************************************************************************/
   public QJoinMetaData withRightTable(String rightTable)
   {
      this.rightTable = rightTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinOns
    **
    *******************************************************************************/
   public List<JoinOn> getJoinOns()
   {
      return joinOns;
   }



   /*******************************************************************************
    ** Setter for joinOns
    **
    *******************************************************************************/
   public void setJoinOns(List<JoinOn> joinOns)
   {
      this.joinOns = joinOns;
   }



   /*******************************************************************************
    ** Fluent setter for joinOns
    **
    *******************************************************************************/
   public QJoinMetaData withJoinOns(List<JoinOn> joinOns)
   {
      this.joinOns = joinOns;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for joinOns
    **
    *******************************************************************************/
   public QJoinMetaData withJoinOn(JoinOn joinOn)
   {
      if(this.joinOns == null)
      {
         this.joinOns = new ArrayList<>();
      }
      this.joinOns.add(joinOn);
      return (this);
   }



   /*******************************************************************************
    ** Getter for orderBys
    **
    *******************************************************************************/
   public List<QFilterOrderBy> getOrderBys()
   {
      return orderBys;
   }



   /*******************************************************************************
    ** Setter for orderBys
    **
    *******************************************************************************/
   public void setOrderBys(List<QFilterOrderBy> orderBys)
   {
      this.orderBys = orderBys;
   }



   /*******************************************************************************
    ** Fluent setter for orderBys
    **
    *******************************************************************************/
   public QJoinMetaData withOrderBys(List<QFilterOrderBy> orderBys)
   {
      this.orderBys = orderBys;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for orderBys
    **
    *******************************************************************************/
   public QJoinMetaData withOrderBy(QFilterOrderBy orderBy)
   {
      if(this.orderBys == null)
      {
         this.orderBys = new ArrayList<>();
      }
      this.orderBys.add(orderBy);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJoinMetaData withInferredName()
   {
      if(!StringUtils.hasContent(getLeftTable()) || !StringUtils.hasContent(getRightTable()))
      {
         throw (new IllegalStateException("Missing either a left or right table name when trying to set inferred name for join"));
      }
      return (withName(getLeftTable() + "Join" + getRightTable()));
   }
}
