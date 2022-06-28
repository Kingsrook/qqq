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
import java.sql.SQLException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQTableRequest;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** Base class for all core actions in the RDBMS module.
 *******************************************************************************/
public abstract class AbstractRDBMSAction
{

   /*******************************************************************************
    ** Get the table name to use in the RDBMS from a QTableMetaData.
    **
    ** That is, table.backendDetails.tableName if set -- else, table.name
    *******************************************************************************/
   protected String getTableName(QTableMetaData table)
   {
      if(table.getBackendDetails() instanceof RDBMSTableBackendDetails details)
      {
         if(StringUtils.hasContent(details.getTableName()))
         {
            return (details.getTableName());
         }
      }
      return (table.getName());
   }



   /*******************************************************************************
    ** Get the column name to use for a field in the RDBMS, from the fieldMetaData.
    **
    ** That is, field.backendName if set -- else, field.name
    *******************************************************************************/
   protected String getColumnName(QFieldMetaData field)
   {
      if(field.getBackendName() != null)
      {
         return (field.getBackendName());
      }
      return (field.getName());
   }



   /*******************************************************************************
    ** Get a database connection, per the backend in the request.
    *******************************************************************************/
   protected Connection getConnection(AbstractQTableRequest qTableRequest) throws SQLException
   {
      ConnectionManager connectionManager = new ConnectionManager();
      return connectionManager.getConnection((RDBMSBackendMetaData) qTableRequest.getBackend());
   }



   /*******************************************************************************
    ** Handle obvious problems with values - like empty string for integer should be null.
    **
    *******************************************************************************/
   protected Serializable scrubValue(QFieldMetaData field, Serializable value)
   {
      if("".equals(value))
      {
         QFieldType type = field.getType();
         if(type.equals(QFieldType.INTEGER) || type.equals(QFieldType.DECIMAL) || type.equals(QFieldType.DATE) || type.equals(QFieldType.DATE_TIME))
         {
            value = null;
         }
      }

      return (value);
   }
}
