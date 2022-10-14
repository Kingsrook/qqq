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

package com.kingsrook.qqq.lambda.examples;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.lambda.QBaseCustomLambdaHandler;
import com.kingsrook.qqq.lambda.model.QLambdaRequest;
import com.kingsrook.qqq.lambda.model.QLambdaResponse;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class ExampleLambdaHandler extends QBaseCustomLambdaHandler
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected QLambdaResponse handleJsonRequest(QLambdaRequest request, JSONObject bodyJsonObject) throws QException
   {
      log("In subclass: " + getClass().getSimpleName());
      return (OK);
   }

}
