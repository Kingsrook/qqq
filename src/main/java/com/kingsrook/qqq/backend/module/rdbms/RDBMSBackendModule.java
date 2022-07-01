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


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSDeleteAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSInsertAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSQueryAction;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSUpdateAction;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** QQQ Backend module for working with Relational Databases (RDBMS's).
 *******************************************************************************/
public class RDBMSBackendModule implements QBackendModuleInterface
{
   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   public String getBackendType()
   {
      return ("rdbms");
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

}
