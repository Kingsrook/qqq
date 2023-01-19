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


import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;


/*******************************************************************************
 ** Utility methods to be shared by all of the various Actions (e.g., InsertAction)
 *******************************************************************************/
public class ActionHelper
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static void validateSession(AbstractActionInput request) throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QSession  qSession  = QContext.getQSession();

      if(qInstance == null)
      {
         throw (new QException("QInstance was not set in QContext."));
      }

      if(qSession == null)
      {
         throw (new QException("QSession was not set in QContext."));
      }

      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());
      if(!authenticationModule.isSessionValid(qInstance, qSession))
      {
         throw new QAuthenticationException("Invalid session in request");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void editFirstValue(List<Serializable> values, Function<String, String> editFunction)
   {
      if(values.size() > 0)
      {
         values.set(0, editFunction.apply(String.valueOf(values.get(0))));
      }
   }
}
