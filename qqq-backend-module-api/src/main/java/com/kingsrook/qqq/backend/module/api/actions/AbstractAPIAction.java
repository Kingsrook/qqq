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

package com.kingsrook.qqq.backend.module.api.actions;


import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.api.model.metadata.APIBackendMetaData;


/*******************************************************************************
 ** Base class for all Backend-module-API Actions
 *******************************************************************************/
public abstract class AbstractAPIAction
{
   protected APIBackendMetaData backendMetaData;
   protected BaseAPIActionUtil  apiActionUtil;
   protected QSession           session;



   /*******************************************************************************
    ** Setup the s3 utils object to be used for this action.
    *******************************************************************************/
   public void preAction(AbstractTableActionInput actionInput)
   {
      QBackendMetaData baseBackendMetaData = QContext.getQInstance().getBackendForTable(actionInput.getTableName());
      this.backendMetaData = (APIBackendMetaData) baseBackendMetaData;
      this.session = QContext.getQSession();

      if(backendMetaData.getActionUtil() != null)
      {
         apiActionUtil = QCodeLoader.getAdHoc(BaseAPIActionUtil.class, backendMetaData.getActionUtil());
      }
      else
      {
         apiActionUtil = new BaseAPIActionUtil();
      }

      apiActionUtil.setBackendMetaData(this.backendMetaData);
      apiActionUtil.setActionInput(actionInput);
      apiActionUtil.setSession(session);
   }

}

