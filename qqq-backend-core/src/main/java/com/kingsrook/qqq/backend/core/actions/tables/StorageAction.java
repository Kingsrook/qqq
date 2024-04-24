/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.io.InputStream;
import java.io.OutputStream;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.interfaces.QStorageInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Action to do (generally, "mass") storage operations in a backend.
 **
 ** e.g., store a (potentially large) file - specifically - by working with it
 ** as either an InputStream or OutputStream.
 **
 ** May not be implemented in all backends.
 **
 *******************************************************************************/
public class StorageAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public OutputStream createOutputStream(StorageInput storageInput) throws QException
   {
      QBackendModuleInterface qBackendModuleInterface = preAction(storageInput);
      QStorageInterface       storageInterface        = qBackendModuleInterface.getStorageInterface();
      return (storageInterface.createOutputStream(storageInput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public InputStream getInputStream(StorageInput storageInput) throws QException
   {
      QBackendModuleInterface qBackendModuleInterface = preAction(storageInput);
      QStorageInterface       storageInterface        = qBackendModuleInterface.getStorageInterface();
      return (storageInterface.getInputStream(storageInput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QBackendModuleInterface preAction(StorageInput storageInput) throws QException
   {
      ActionHelper.validateSession(storageInput);

      if(storageInput.getTableName() == null)
      {
         throw (new QException("Table name was not specified in storage input"));
      }

      QTableMetaData table = storageInput.getTable();
      if(table == null)
      {
         throw (new QException("A table named [" + storageInput.getTableName() + "] was not found in the active QInstance"));
      }

      QBackendMetaData         backend                  = storageInput.getBackend();
      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(backend);
      return (qModule);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void makePublic(StorageInput storageInput) throws QException
   {
      QBackendModuleInterface qBackendModuleInterface = preAction(storageInput);
      QStorageInterface       storageInterface        = qBackendModuleInterface.getStorageInterface();
      storageInterface.makePublic(storageInput);
   }
}
