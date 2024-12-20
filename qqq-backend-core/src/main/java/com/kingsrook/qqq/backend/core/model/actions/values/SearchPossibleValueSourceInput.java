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

package com.kingsrook.qqq.backend.core.model.actions.values;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;


/*******************************************************************************
 ** Input for the Search possible value source action
 *******************************************************************************/
public class SearchPossibleValueSourceInput extends AbstractActionInput implements Cloneable
{
   private String             possibleValueSourceName;
   private QQueryFilter       defaultQueryFilter;
   private String             searchTerm;
   private List<Serializable> idList;
   private List<String>       labelList;

   private Integer skip  = 0;
   private Integer limit = 250;



   /*******************************************************************************
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public SearchPossibleValueSourceInput clone()
   {
      try
      {
         SearchPossibleValueSourceInput clone = (SearchPossibleValueSourceInput) super.clone();
         if(defaultQueryFilter != null)
         {
            clone.setDefaultQueryFilter(defaultQueryFilter.clone());
         }
         if(idList != null)
         {
            clone.setIdList(new ArrayList<>(idList));
         }
         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }



   /*******************************************************************************
    ** Getter for possibleValueSourceName
    **
    *******************************************************************************/
   public String getPossibleValueSourceName()
   {
      return possibleValueSourceName;
   }



   /*******************************************************************************
    ** Setter for possibleValueSourceName
    **
    *******************************************************************************/
   public void setPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
   }



   /*******************************************************************************
    ** Fluent setter for possibleValueSourceName
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput withPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultQueryFilter
    **
    *******************************************************************************/
   public QQueryFilter getDefaultQueryFilter()
   {
      return defaultQueryFilter;
   }



   /*******************************************************************************
    ** Setter for defaultQueryFilter
    **
    *******************************************************************************/
   public void setDefaultQueryFilter(QQueryFilter defaultQueryFilter)
   {
      this.defaultQueryFilter = defaultQueryFilter;
   }



   /*******************************************************************************
    ** Fluent setter for defaultQueryFilter
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput withDefaultQueryFilter(QQueryFilter defaultQueryFilter)
   {
      this.defaultQueryFilter = defaultQueryFilter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for searchTerm
    **
    *******************************************************************************/
   public String getSearchTerm()
   {
      return searchTerm;
   }



   /*******************************************************************************
    ** Setter for searchTerm
    **
    *******************************************************************************/
   public void setSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
   }



   /*******************************************************************************
    ** Fluent setter for searchTerm
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput withSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
      return (this);
   }



   /*******************************************************************************
    ** Getter for idList
    **
    *******************************************************************************/
   public List<Serializable> getIdList()
   {
      return idList;
   }



   /*******************************************************************************
    ** Setter for idList
    **
    *******************************************************************************/
   public void setIdList(List<Serializable> idList)
   {
      this.idList = idList;
   }



   /*******************************************************************************
    ** Fluent setter for idList
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput withIdList(List<Serializable> idList)
   {
      this.idList = idList;
      return (this);
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
    ** Fluent setter for skip
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput withSkip(Integer skip)
   {
      this.skip = skip;
      return (this);
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



   /*******************************************************************************
    ** Fluent setter for limit
    **
    *******************************************************************************/
   public SearchPossibleValueSourceInput withLimit(Integer limit)
   {
      this.limit = limit;
      return (this);
   }


   /*******************************************************************************
    ** Getter for labelList
    *******************************************************************************/
   public List<String> getLabelList()
   {
      return (this.labelList);
   }



   /*******************************************************************************
    ** Setter for labelList
    *******************************************************************************/
   public void setLabelList(List<String> labelList)
   {
      this.labelList = labelList;
   }



   /*******************************************************************************
    ** Fluent setter for labelList
    *******************************************************************************/
   public SearchPossibleValueSourceInput withLabelList(List<String> labelList)
   {
      this.labelList = labelList;
      return (this);
   }

}
