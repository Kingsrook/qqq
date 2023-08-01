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

package com.kingsrook.qqq.backend.core.context;


import java.util.Optional;
import java.util.Stack;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** A collection of thread-local variables, to define the current context of the
 ** QQQ code that is running.  e.g., what QInstance is being used, what QSession
 ** is active, etc.
 *******************************************************************************/
public class QContext
{
   private static final QLogger LOG = QLogger.getLogger(QContext.class);

   private static ThreadLocal<QInstance>                  qInstanceThreadLocal           = new ThreadLocal<>();
   private static ThreadLocal<QSession>                   qSessionThreadLocal            = new ThreadLocal<>();
   private static ThreadLocal<QBackendTransaction>        qBackendTransactionThreadLocal = new ThreadLocal<>();
   private static ThreadLocal<Stack<AbstractActionInput>> actionStackThreadLocal         = new ThreadLocal<>();



   /*******************************************************************************
    ** private constructor - class is not meant to be instantiated.
    *******************************************************************************/
   private QContext()
   {
   }



   /*******************************************************************************
    ** Most common method to set or init the context - e.g., set the current thread
    ** with a QInstance and QSession.
    *******************************************************************************/
   public static void init(QInstance qInstance, QSession qSession)
   {
      init(qInstance, qSession, null, null);
   }



   /*******************************************************************************
    ** Full flavor init method - also take a transaction and action input (to seed the stack).
    *******************************************************************************/
   public static void init(QInstance qInstance, QSession qSession, QBackendTransaction transaction, AbstractActionInput actionInput)
   {
      qInstanceThreadLocal.set(qInstance);
      qSessionThreadLocal.set(qSession);
      qBackendTransactionThreadLocal.set(transaction);

      actionStackThreadLocal.set(new Stack<>());
      if(actionInput != null)
      {
         actionStackThreadLocal.get().add(actionInput);
      }

      if(qInstance != null && !qInstance.getHasBeenValidated())
      {
         try
         {
            new QInstanceValidator().validate(qInstance);
         }
         catch(QInstanceValidationException e)
         {
            LOG.warn(e);
            throw (new IllegalArgumentException("QInstance failed validation" + e.getMessage(), e));
         }
      }
   }



   /*******************************************************************************
    ** Init a new thread with the context captured from a different thread.  e.g.,
    ** when starting some async task.
    *******************************************************************************/
   public static void init(CapturedContext capturedContext)
   {
      init(capturedContext.qInstance(), capturedContext.qSession(), capturedContext.qBackendTransaction(), null);
      actionStackThreadLocal.set(capturedContext.actionStack());
   }



   /*******************************************************************************
    ** Capture all values from the current thread - meant to be used with the init
    ** overload that takes a CapturedContext, for setting up a child thread.
    *******************************************************************************/
   public static CapturedContext capture()
   {
      return (new CapturedContext(getQInstance(), getQSession(), getQBackendTransaction(), getActionStack()));
   }



   /*******************************************************************************
    ** Clear all values in the current thread.
    *******************************************************************************/
   public static void clear()
   {
      qInstanceThreadLocal.remove();
      qSessionThreadLocal.remove();
      qBackendTransactionThreadLocal.remove();
      actionStackThreadLocal.remove();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance getQInstance()
   {
      return (qInstanceThreadLocal.get());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QSession getQSession()
   {
      return (qSessionThreadLocal.get());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendTransaction getQBackendTransaction()
   {
      return (qBackendTransactionThreadLocal.get());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Stack<AbstractActionInput> getActionStack()
   {
      return (actionStackThreadLocal.get());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void pushAction(AbstractActionInput action)
   {
      if(actionStackThreadLocal.get() == null)
      {
         actionStackThreadLocal.set(new Stack<>());
      }
      actionStackThreadLocal.get().push(action);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void popAction()
   {
      try
      {
         actionStackThreadLocal.get().pop();
      }
      catch(Exception e)
      {
         LOG.debug("Error popping action stack", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setQInstance(QInstance qInstance)
   {
      qInstanceThreadLocal.set(qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setQSession(QSession qSession)
   {
      qSessionThreadLocal.set(qSession);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setTransaction(QBackendTransaction transaction)
   {
      qBackendTransactionThreadLocal.set(transaction);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void clearTransaction()
   {
      qBackendTransactionThreadLocal.remove();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<AbstractActionInput> getFirstActionInStack()
   {
      if(actionStackThreadLocal.get() == null || actionStackThreadLocal.get().isEmpty())
      {
         return (Optional.empty());
      }

      return (Optional.of(actionStackThreadLocal.get().get(0)));
   }
}
