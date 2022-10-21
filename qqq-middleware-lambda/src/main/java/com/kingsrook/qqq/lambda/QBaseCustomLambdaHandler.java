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

package com.kingsrook.qqq.lambda;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.lambda.model.QLambdaRequest;
import com.kingsrook.qqq.lambda.model.QLambdaResponse;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 ** QQQ base class for "Custom" lambda handlers.  e.g., completely custom code
 ** to run in your QQQ app, outside of tables or processes (for those standard
 ** use-cases, see QStandardLambdaHandler).
 **
 ** Subclasses here  can just override `handleJsonRequest`, and avoid seeing the
 ** lambda-ness of lambda.
 **
 ** Such subclasses can then have easy standalone unit tests - just testing their
 ** logic, and not the lambda-ness.
 *******************************************************************************/
public class QBaseCustomLambdaHandler extends QAbstractLambdaHandler
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected QLambdaResponse handleRequest(QLambdaRequest request) throws QException
   {
      String contentType = request.getHeaders().optString("content-type");
      if("application/json".equals(contentType))
      {
         JSONObject bodyJsonObject;
         try
         {
            bodyJsonObject = JsonUtils.toJSONObject(request.getBody());
         }
         catch(JSONException je)
         {
            return (new QLambdaResponse(400, "Unable to parse request body as JSON: " + je.getMessage()));
         }

         return (handleJsonRequest(request, bodyJsonObject));
      }
      else
      {
         return (new QLambdaResponse(400, "Unsupported content-type: " + contentType));
      }
   }



   /*******************************************************************************
    ** Meant to be overridden by subclasses, to provide functionality, if needed.
    *******************************************************************************/
   @Override
   protected QLambdaResponse handleJsonRequest(QLambdaRequest request, JSONObject bodyJsonObject) throws QException
   {
      log(this.getClass().getSimpleName() + " did not override handleJsonRequest - so noop and return 200.");
      return (OK);
   }

}
