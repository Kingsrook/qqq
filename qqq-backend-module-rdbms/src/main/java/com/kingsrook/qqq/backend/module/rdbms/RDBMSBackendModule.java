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

package com.kingsrook.qqq.backend.module.rdbms;


import java.sql.Connection;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.module.rdbms.actions.AbstractRDBMSAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSAggregateAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSCountAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSDeleteAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSInsertAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSQueryAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSTransaction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSUpdateAction;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** QQQ Backend module for working with Relational Databases (RDBMS's).
 *******************************************************************************/
public class RDBMSBackendModule implements QBackendModuleInterface
{
   private static final QLogger LOG = QLogger.getLogger(RDBMSBackendModule.class);

   public static final String NAME = "rdbms";

   static
   {
      QBackendModuleDispatcher.registerBackendModule(new RDBMSBackendModule());
   }

   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   public String getBackendType()
   {
      return NAME;
   }



   /*******************************************************************************
    ** Method to identify the class used for backend meta data for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QBackendMetaData> getBackendMetaDataClass()
   {
      return (RDBMSBackendMetaData.class);
   }



   /*******************************************************************************
    ** Method to identify the class used for table-backend details for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QTableBackendDetails> getTableBackendDetailsClass()
   {
      return (RDBMSTableBackendDetails.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public CountInterface getCountInterface()
   {
      return (new RDBMSCountAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryInterface getQueryInterface()
   {
      return (new RDBMSQueryAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInterface getInsertInterface()
   {
      return (new RDBMSInsertAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public UpdateInterface getUpdateInterface()
   {
      return (new RDBMSUpdateAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DeleteInterface getDeleteInterface()
   {
      return (new RDBMSDeleteAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public AggregateInterface getAggregateInterface()
   {
      return (new RDBMSAggregateAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QBackendTransaction openTransaction(AbstractTableActionInput input) throws QException
   {
      try
      {
         LOG.debug("Opening transaction");
         Connection connection = AbstractRDBMSAction.getConnection(input);
         return (new RDBMSTransaction(connection));
      }
      catch(Exception e)
      {
         throw new QException("Error opening transaction: " + e.getMessage(), e);
      }
   }

}
