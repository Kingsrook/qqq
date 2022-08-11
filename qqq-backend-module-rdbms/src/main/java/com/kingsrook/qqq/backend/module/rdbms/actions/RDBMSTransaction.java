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


import java.sql.Connection;
import java.sql.SQLException;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** RDBMS implementation of backend transaction.
 **
 ** Stores a jdbc connection, which is set to autoCommit(false).
 *******************************************************************************/
public class RDBMSTransaction extends QBackendTransaction
{
   private static final Logger LOG = LogManager.getLogger(RDBMSTransaction.class);

   private Connection connection;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RDBMSTransaction(Connection connection) throws SQLException
   {
      connection.setAutoCommit(false);
      this.connection = connection;
   }



   /*******************************************************************************
    ** Getter for connection
    **
    *******************************************************************************/
   public Connection getConnection()
   {
      return connection;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void commit() throws QException
   {
      try
      {
         RDBMSTransaction.LOG.info("Committing transaction");
         connection.commit();
         RDBMSTransaction.LOG.info("Commit complete");
      }
      catch(Exception e)
      {
         RDBMSTransaction.LOG.error("Error committing transaction", e);
         throw new QException("Error committing transaction: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void rollback() throws QException
   {
      try
      {
         RDBMSTransaction.LOG.info("Rolling back transaction");
         connection.rollback();
         RDBMSTransaction.LOG.info("Rollback complete");
      }
      catch(Exception e)
      {
         RDBMSTransaction.LOG.error("Error rolling back transaction", e);
         throw new QException("Error rolling back transaction: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void close()
   {
      try
      {
         if(connection.isClosed())
         {
            return;
         }

         connection.close();
      }
      catch(Exception e)
      {
         LOG.error("Error closing connection - possible jdbc connection leak", e);
      }
   }
}
