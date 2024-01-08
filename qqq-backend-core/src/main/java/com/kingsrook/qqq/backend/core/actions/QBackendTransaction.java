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

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Container wherein backend modules can track data and/or objects that are
 ** part of a transaction.
 **
 ** Most obvious use-case would be a JDBC Connection.  See subclass in rdbms module.
 ** Ditto MongoDB.
 **
 ** Note:  One would imagine that this class shouldn't ever implement Serializable...
 *******************************************************************************/
public class QBackendTransaction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendTransaction openFor(AbstractTableActionInput input) throws QException
   {
      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(input.getBackend());
      QBackendTransaction      transaction              = qModule.openTransaction(input);
      return (transaction);
   }



   /*******************************************************************************
    ** Commit the transaction.
    *******************************************************************************/
   public void commit() throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    ** Rollback the transaction.
    *******************************************************************************/
   public void rollback() throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    ** Close any resources associated with the transaction.  In theory, should only
    ** be called after a commit or rollback was done.
    *******************************************************************************/
   public void close()
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }
}
