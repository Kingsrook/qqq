/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.model.metadata.processes;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.api.model.actions.HttpApiResponse;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qqq.openapi.model.Response;
import org.eclipse.jetty.http.HttpStatus;


/*******************************************************************************
 **
 *******************************************************************************/
public interface ApiProcessOutputInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   Serializable getOutputForProcess(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException;

   /*******************************************************************************
    **
    *******************************************************************************/
   default HttpStatus.Code getSuccessStatusCode(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput)
   {
      return (HttpStatus.Code.NO_CONTENT);
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default Map<Integer, Response> getSpecResponses(String apiName)
   {
      return (MapBuilder.of(
         HttpStatus.Code.NO_CONTENT.getCode(), new Response()
            .withDescription("Process has been successfully executed.")
      ));
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default void customizeHttpApiResponse(HttpApiResponse httpApiResponse, RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException
   {

   }

}
