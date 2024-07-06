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

package com.kingsrook.qqq.backend.core.modules.backend;


import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.GetInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.QStorageInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;


/*******************************************************************************
 ** Interface that a QBackendModule must implement.
 **
 ** Note, all methods have a default version, which throws a 'not implemented'
 ** exception.
 **
 *******************************************************************************/
public interface QBackendModuleInterface
{
   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   String getBackendType();

   /*******************************************************************************
    ** Method to identify the class used for backend meta data for this module.
    *******************************************************************************/
   default Class<? extends QBackendMetaData> getBackendMetaDataClass()
   {
      return (QBackendMetaData.class);
   }

   /*******************************************************************************
    ** Method to identify the class used for table-backend details for this module.
    *******************************************************************************/
   default Class<? extends QTableBackendDetails> getTableBackendDetailsClass()
   {
      return QTableBackendDetails.class;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default CountInterface getCountInterface()
   {
      throwNotImplemented("Count");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default QueryInterface getQueryInterface()
   {
      throwNotImplemented("Query");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default GetInterface getGetInterface()
   {
      throwNotImplemented("Get");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default InsertInterface getInsertInterface()
   {
      throwNotImplemented("Insert");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default UpdateInterface getUpdateInterface()
   {
      throwNotImplemented("Update");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default DeleteInterface getDeleteInterface()
   {
      throwNotImplemented("Delete");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default AggregateInterface getAggregateInterface()
   {
      throwNotImplemented("Aggregate");
      return null;
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default QStorageInterface getStorageInterface()
   {
      throwNotImplemented("StorageInterface");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default QBackendTransaction openTransaction(AbstractTableActionInput input) throws QException
   {
      return (new QBackendTransaction());
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   private void throwNotImplemented(String actionName)
   {
      throw new IllegalStateException(actionName + " is not implemented in this module: " + this.getClass().getSimpleName());
   }

}
