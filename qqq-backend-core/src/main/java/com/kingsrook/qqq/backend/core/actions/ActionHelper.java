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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
   /////////////////////////////////////////////////////////////////////////////
   // we would probably use Executors.newCachedThreadPool() - but - it has no //
   // maxPoolSize...  we think some limit is good, so that at a large number  //
   // of attempted concurrent jobs we'll have new jobs block, rather than     //
   // exhausting all server resources and locking up "everything"             //
   // also, it seems like keeping a handful of core-threads around is very    //
   // little actual waste, and better than ever wasting time starting a new   //
   // one, which we know we'll often be doing.                                //
   /////////////////////////////////////////////////////////////////////////////
   private static Integer         CORE_THREADS    = 8;
   private static Integer         MAX_THREADS     = 500;
   private static ExecutorService executorService = new ThreadPoolExecutor(CORE_THREADS, MAX_THREADS, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());



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
    ** access an executor service for sharing among the executeAsync methods of all
    ** actions.
    *******************************************************************************/
   static ExecutorService getExecutorService()
   {
      return (executorService);
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
