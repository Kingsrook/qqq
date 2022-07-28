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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSCountAction extends AbstractRDBMSAction implements CountInterface
{
   private static final Logger LOG = LogManager.getLogger(RDBMSCountAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public CountOutput execute(CountInput countInput) throws QException
   {
      try
      {
         QTableMetaData table     = countInput.getTable();
         String         tableName = getTableName(table);

         String sql = "SELECT count(*) as record_count FROM " + tableName;

         QQueryFilter       filter = countInput.getFilter();
         List<Serializable> params = new ArrayList<>();
         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getCriteria()))
         {
            sql += " WHERE " + makeWhereClause(table, filter.getCriteria(), params);
         }

         // todo sql customization - can edit sql and/or param list

         CountOutput rs = new CountOutput();

         try(Connection connection = getConnection(countInput))
         {
            QueryManager.executeStatement(connection, sql, ((ResultSet resultSet) ->
            {
               if(resultSet.next())
               {
                  rs.setCount(resultSet.getInt("record_count"));
               }

            }), params);
         }

         return rs;
      }
      catch(Exception e)
      {
         LOG.warn("Error executing count", e);
         throw new QException("Error executing count", e);
      }
   }

}