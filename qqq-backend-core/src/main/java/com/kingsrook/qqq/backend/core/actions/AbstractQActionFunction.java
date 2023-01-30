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


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 ** Base class for QQQ Actions (both framework and application defined) that
 ** have a signature like a Function - taking an Input object as a parameter,
 ** and returning an Output object.
 *******************************************************************************/
public abstract class AbstractQActionFunction<I extends AbstractActionInput, O extends AbstractActionOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract O execute(I input) throws QException;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Future<O> executeAsync(I input)
   {
      CapturedContext      capturedContext   = QContext.capture();
      CompletableFuture<O> completableFuture = new CompletableFuture<>();
      Executors.newCachedThreadPool().submit(() ->
      {
         try
         {
            QContext.init(capturedContext);
            O output = execute(input);
            completableFuture.complete(output);
         }
         catch(QException e)
         {
            completableFuture.completeExceptionally(e);
         }
         finally
         {
            QContext.clear();
         }
      });
      return (completableFuture);
   }

}
