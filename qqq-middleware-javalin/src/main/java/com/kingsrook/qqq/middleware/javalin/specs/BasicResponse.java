/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.middleware.javalin.specs;


import java.util.Map;
import com.kingsrook.qqq.openapi.model.Example;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;


/***************************************************************************
 ** Basic version of a response from a spec/endpoint.
 ***************************************************************************/
public record BasicResponse(String contentType, HttpStatus status, String description, String schemaRefName, Map<String, Example> examples)
{

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BasicResponse(String description, String schemaRefName)
   {
      this(ContentType.APPLICATION_JSON.getMimeType(), HttpStatus.OK, description, schemaRefName, null);
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BasicResponse(String description, String schemaRefName, Map<String, Example> examples)
   {
      this(ContentType.APPLICATION_JSON.getMimeType(), HttpStatus.OK, description, schemaRefName, examples);
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BasicResponse(HttpStatus status, String description, String schemaRefName)
   {
      this(ContentType.APPLICATION_JSON.getMimeType(), status, description, schemaRefName, null);
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BasicResponse(HttpStatus status, String description, String schemaRefName, Map<String, Example> examples)
   {
      this(ContentType.APPLICATION_JSON.getMimeType(), status, description, schemaRefName, examples);
   }

}
