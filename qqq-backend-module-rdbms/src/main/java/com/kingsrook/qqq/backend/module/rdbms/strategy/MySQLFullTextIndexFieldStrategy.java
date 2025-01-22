/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.rdbms.strategy;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 ** RDBMS action strategy for a field with a FULLTEXT INDEX on it in a MySQL
 ** database.  Makes a LIKE or CONTAINS (or NOT those) query use the special
 ** syntax that hits the FULLTEXT INDEX.
 *******************************************************************************/
public class MySQLFullTextIndexFieldStrategy extends BaseRDBMSActionStrategy
{
   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public Integer appendCriterionToWhereClause(QFilterCriteria criterion, StringBuilder clause, String column, List<Serializable> values, QFieldMetaData field)
   {
      switch(criterion.getOperator())
      {
         case LIKE, CONTAINS ->
         {
            clause.append(" MATCH (").append(column).append(") AGAINST (?) ");
            return (1);
         }
         case NOT_LIKE, NOT_CONTAINS ->
         {
            clause.append(" NOT MATCH (").append(column).append(") AGAINST (?) ");
            return (1);
         }
         default ->
         {
            return super.appendCriterionToWhereClause(criterion, clause, column, values, field);
         }
      }
   }
}
