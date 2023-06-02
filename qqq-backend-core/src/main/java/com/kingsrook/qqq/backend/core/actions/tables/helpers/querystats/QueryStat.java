/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables.helpers.querystats;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class QueryStat extends QRecordEntity
{
   public static final String TABLE_NAME = "queryStat";

   @QField()
   private Integer id;

   @QField()
   private String tableName;

   @QField()
   private Instant startTimestamp;

   @QField()
   private Instant firstResultTimestamp;

   @QField()
   private Integer firstResultMillis;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE)
   private String joinTables;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE)
   private String orderBys;

   @QAssociation(name = "queryStatFilterCriteria")
   private List<QueryStatFilterCriteria> queryStatFilterCriteriaList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setJoinTables(Collection<String> joinTableNames)
   {
      if(CollectionUtils.nullSafeIsEmpty(joinTableNames))
      {
         setJoinTables((String) null);
      }

      setJoinTables(joinTableNames.stream().sorted().collect(Collectors.joining(",")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setQQueryFilter(QQueryFilter filter)
   {
      if(filter == null)
      {
         setQueryStatFilterCriteriaList(null);
         setOrderBys(null);
      }
      else
      {
         /////////////////////////////////////////////
         // manage list of sub-records for criteria //
         /////////////////////////////////////////////
         if(CollectionUtils.nullSafeIsEmpty(filter.getCriteria()) && CollectionUtils.nullSafeIsEmpty(filter.getSubFilters()))
         {
            setQueryStatFilterCriteriaList(null);
         }
         else
         {
            ArrayList<QueryStatFilterCriteria> criteriaList = new ArrayList<>();
            setQueryStatFilterCriteriaList(criteriaList);
            processFilterCriteria(filter, criteriaList);
         }

         //////////////////////////////////////////////////////////////
         // set orderBys (comma-delimited concatenated string field) //
         //////////////////////////////////////////////////////////////
         if(CollectionUtils.nullSafeIsEmpty(filter.getOrderBys()))
         {
            setOrderBys(null);
         }
         else
         {
            setOrderBys(filter.getOrderBys().stream().map(ob -> ob.getFieldName()).collect(Collectors.joining(",")));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void processFilterCriteria(QQueryFilter filter, ArrayList<QueryStatFilterCriteria> criteriaList)
   {
      for(QFilterCriteria criterion : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         criteriaList.add(new QueryStatFilterCriteria()
            .withFieldName(criterion.getFieldName())
            .withOperator(criterion.getOperator().name())
            .withValues(CollectionUtils.nonNullList(criterion.getValues()).stream().map(v -> ValueUtils.getValueAsString(v)).collect(Collectors.joining(","))));
      }

      for(QQueryFilter subFilter : CollectionUtils.nonNullList(filter.getSubFilters()))
      {
         processFilterCriteria(subFilter, criteriaList);
      }
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public QueryStat withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startTimestamp
    *******************************************************************************/
   public Instant getStartTimestamp()
   {
      return (this.startTimestamp);
   }



   /*******************************************************************************
    ** Setter for startTimestamp
    *******************************************************************************/
   public void setStartTimestamp(Instant startTimestamp)
   {
      this.startTimestamp = startTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for startTimestamp
    *******************************************************************************/
   public QueryStat withStartTimestamp(Instant startTimestamp)
   {
      this.startTimestamp = startTimestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for firstResultTimestamp
    *******************************************************************************/
   public Instant getFirstResultTimestamp()
   {
      return (this.firstResultTimestamp);
   }



   /*******************************************************************************
    ** Setter for firstResultTimestamp
    *******************************************************************************/
   public void setFirstResultTimestamp(Instant firstResultTimestamp)
   {
      this.firstResultTimestamp = firstResultTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for firstResultTimestamp
    *******************************************************************************/
   public QueryStat withFirstResultTimestamp(Instant firstResultTimestamp)
   {
      this.firstResultTimestamp = firstResultTimestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for firstResultMillis
    *******************************************************************************/
   public Integer getFirstResultMillis()
   {
      return (this.firstResultMillis);
   }



   /*******************************************************************************
    ** Setter for firstResultMillis
    *******************************************************************************/
   public void setFirstResultMillis(Integer firstResultMillis)
   {
      this.firstResultMillis = firstResultMillis;
   }



   /*******************************************************************************
    ** Fluent setter for firstResultMillis
    *******************************************************************************/
   public QueryStat withFirstResultMillis(Integer firstResultMillis)
   {
      this.firstResultMillis = firstResultMillis;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinTables
    *******************************************************************************/
   public String getJoinTables()
   {
      return (this.joinTables);
   }



   /*******************************************************************************
    ** Setter for joinTables
    *******************************************************************************/
   public void setJoinTables(String joinTables)
   {
      this.joinTables = joinTables;
   }



   /*******************************************************************************
    ** Fluent setter for joinTables
    *******************************************************************************/
   public QueryStat withJoinTables(String joinTables)
   {
      this.joinTables = joinTables;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryStatFilterCriteriaList
    *******************************************************************************/
   public List<QueryStatFilterCriteria> getQueryStatFilterCriteriaList()
   {
      return (this.queryStatFilterCriteriaList);
   }



   /*******************************************************************************
    ** Setter for queryStatFilterCriteriaList
    *******************************************************************************/
   public void setQueryStatFilterCriteriaList(List<QueryStatFilterCriteria> queryStatFilterCriteriaList)
   {
      this.queryStatFilterCriteriaList = queryStatFilterCriteriaList;
   }



   /*******************************************************************************
    ** Fluent setter for queryStatFilterCriteriaList
    *******************************************************************************/
   public QueryStat withQueryStatFilterCriteriaList(List<QueryStatFilterCriteria> queryStatFilterCriteriaList)
   {
      this.queryStatFilterCriteriaList = queryStatFilterCriteriaList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for orderBys
    *******************************************************************************/
   public String getOrderBys()
   {
      return (this.orderBys);
   }



   /*******************************************************************************
    ** Setter for orderBys
    *******************************************************************************/
   public void setOrderBys(String orderBys)
   {
      this.orderBys = orderBys;
   }



   /*******************************************************************************
    ** Fluent setter for orderBys
    *******************************************************************************/
   public QueryStat withOrderBys(String orderBys)
   {
      this.orderBys = orderBys;
      return (this);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public QueryStat withId(Integer id)
   {
      this.id = id;
      return (this);
   }

}
