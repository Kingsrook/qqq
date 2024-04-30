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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.serialization.QFilterCriteriaDeserializer;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 * A single criteria Component of a Query
 *
 *******************************************************************************/
@JsonDeserialize(using = QFilterCriteriaDeserializer.class)
public class QFilterCriteria implements Serializable, Cloneable
{
   private static final QLogger LOG = QLogger.getLogger(QFilterCriteria.class);

   private String fieldName;
   private QCriteriaOperator  operator;
   private List<Serializable> values;

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // todo - probably implement this as a type of expression - though would require a little special handling i think when evaluating... //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private String otherFieldName;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QFilterCriteria clone()
   {
      try
      {
         QFilterCriteria clone = (QFilterCriteria) super.clone();
         if(values != null)
         {
            clone.values = new ArrayList<>();
            clone.values.addAll(values);
         }
         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteria()
   {
      ///////////////////////////////
      // don't let values be null. //
      ///////////////////////////////
      values = new ArrayList<>();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public QFilterCriteria(String fieldName, QCriteriaOperator operator, List<? extends Serializable> values)
   {
      this.fieldName = fieldName;
      this.operator = operator;
      this.values = values == null ? new ArrayList<>() : (List<Serializable>) values;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public QFilterCriteria(String fieldName, QCriteriaOperator operator, Collection<? extends Serializable> values)
   {
      this.fieldName = fieldName;
      this.operator = operator;

      if(values == null)
      {
         this.values = new ArrayList<>();
      }
      else if(values instanceof List list)
      {
         this.values = list;
      }
      else
      {
         this.values = new ArrayList<>(values);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteria(String fieldName, QCriteriaOperator operator, Serializable... values)
   {
      this.fieldName = fieldName;
      this.operator = operator;

      if(values == null || (values.length == 1 && values[0] == null))
      {
         ////////////////////////////////////////////////////////////////////
         // this ... could be a sign of an issue... debug juuuust in case? //
         ////////////////////////////////////////////////////////////////////
         LOG.debug("null passed as singleton varargs array will be ignored");
         this.values = new ArrayList<>();
      }
      else
      {
         this.values = new ArrayList<>(Arrays.stream(values).toList());
      }
   }



   /*******************************************************************************
    ** Getter for fieldName
    **
    *******************************************************************************/
   public String getFieldName()
   {
      return fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public QFilterCriteria withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return this;
   }



   /*******************************************************************************
    ** Getter for operator
    **
    *******************************************************************************/
   public QCriteriaOperator getOperator()
   {
      return operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public void setOperator(QCriteriaOperator operator)
   {
      this.operator = operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public QFilterCriteria withOperator(QCriteriaOperator operator)
   {
      this.operator = operator;
      return this;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteria withOperator(String operatorName)
   {
      return withOperator(QCriteriaOperator.valueOf(operatorName.toUpperCase()));
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public List<Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(List<Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public QFilterCriteria withValues(List<Serializable> values)
   {
      this.values = values;
      return this;
   }



   /*******************************************************************************
    ** Getter for otherFieldName
    **
    *******************************************************************************/
   public String getOtherFieldName()
   {
      return otherFieldName;
   }



   /*******************************************************************************
    ** Setter for otherFieldName
    **
    *******************************************************************************/
   public void setOtherFieldName(String otherFieldName)
   {
      this.otherFieldName = otherFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for otherFieldName
    **
    *******************************************************************************/
   public QFilterCriteria withOtherFieldName(String otherFieldName)
   {
      this.otherFieldName = otherFieldName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      if(fieldName == null)
      {
         return ("<null-field-criteria>");
      }

      StringBuilder rs = new StringBuilder(fieldName);
      try
      {
         rs.append(" ").append(operator).append(" ");
         if(CollectionUtils.nullSafeHasContents(values))
         {
            if(StringUtils.hasContent(otherFieldName))
            {
               rs.append(otherFieldName);
            }
            else
            {
               if(values.size() == 1)
               {
                  rs.append(values.get(0));
               }
               else
               {
                  int index = 0;
                  for(Serializable value : values)
                  {
                     if(index++ > 9)
                     {
                        rs.append("and ").append(values.size() - index).append(" more");
                        break;
                     }
                     rs.append(value).append(",");
                  }
               }
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error in toString", e);
         rs.append("Error generating toString...");
      }

      return (rs.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }

      if(o == null || getClass() != o.getClass())
      {
         return false;
      }

      QFilterCriteria that = (QFilterCriteria) o;
      return Objects.equals(fieldName, that.fieldName) && operator == that.operator && Objects.equals(values, that.values) && Objects.equals(otherFieldName, that.otherFieldName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(fieldName, operator, values, otherFieldName);
   }

}
