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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Part of query (or count, aggregate) input, to do a Join as part of a query.
 *******************************************************************************/
public class QueryJoin
{
   private String        leftTableOrAlias;
   private String        rightTable;
   private QJoinMetaData joinMetaData;
   private String        alias;
   private boolean       select = false;
   private Type          type   = Type.INNER;



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum Type
   {INNER, LEFT, RIGHT, FULL}



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryJoin()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryJoin(String leftTableOrAlias, String rightTable)
   {
      this.leftTableOrAlias = leftTableOrAlias;
      this.rightTable = rightTable;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryJoin(QJoinMetaData joinMetaData)
   {
      setJoinMetaData(joinMetaData);
   }



   /*******************************************************************************
    ** Getter for leftTableOrAlias
    **
    *******************************************************************************/
   public String getLeftTableOrAlias()
   {
      return leftTableOrAlias;
   }



   /*******************************************************************************
    ** Setter for leftTableOrAlias
    **
    *******************************************************************************/
   public void setLeftTableOrAlias(String leftTableOrAlias)
   {
      this.leftTableOrAlias = leftTableOrAlias;
   }



   /*******************************************************************************
    ** Fluent setter for leftTableOrAlias
    **
    *******************************************************************************/
   public QueryJoin withLeftTableOrAlias(String leftTableOrAlias)
   {
      this.leftTableOrAlias = leftTableOrAlias;
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
   public QueryJoin withRightTable(String rightTable)
   {
      this.rightTable = rightTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for alias
    **
    *******************************************************************************/
   public String getAlias()
   {
      return alias;
   }



   /*******************************************************************************
    ** Setter for alias
    **
    *******************************************************************************/
   public void setAlias(String alias)
   {
      this.alias = alias;
   }



   /*******************************************************************************
    ** Fluent setter for alias
    **
    *******************************************************************************/
   public QueryJoin withAlias(String alias)
   {
      this.alias = alias;
      return (this);
   }



   /*******************************************************************************
    ** Getter for select
    **
    *******************************************************************************/
   public boolean getSelect()
   {
      return select;
   }



   /*******************************************************************************
    ** Setter for select
    **
    *******************************************************************************/
   public void setSelect(boolean select)
   {
      this.select = select;
   }



   /*******************************************************************************
    ** Fluent setter for select
    **
    *******************************************************************************/
   public QueryJoin withSelect(boolean select)
   {
      this.select = select;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getAliasOrRightTable()
   {
      if(StringUtils.hasContent(alias))
      {
         return (alias);
      }
      return (rightTable);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public Type getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(Type type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public QueryJoin withType(Type type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinMetaData
    **
    *******************************************************************************/
   public QJoinMetaData getJoinMetaData()
   {
      return joinMetaData;
   }



   /*******************************************************************************
    ** Setter for joinMetaData
    **
    *******************************************************************************/
   public void setJoinMetaData(QJoinMetaData joinMetaData)
   {
      this.joinMetaData = joinMetaData;

      if(!StringUtils.hasContent(this.leftTableOrAlias) && !StringUtils.hasContent(this.rightTable))
      {
         setLeftTableOrAlias(joinMetaData.getLeftTable());
         setRightTable(joinMetaData.getRightTable());
      }
   }



   /*******************************************************************************
    ** Fluent setter for joinMetaData
    **
    *******************************************************************************/
   public QueryJoin withJoinMetaData(QJoinMetaData joinMetaData)
   {
      setJoinMetaData(joinMetaData);
      return (this);
   }

}
