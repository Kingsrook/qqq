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

package com.kingsrook.qqq.api.model.actions;


import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.openapi.model.OpenAPI;


/*******************************************************************************
 **
 *******************************************************************************/
public class GenerateOpenApiSpecOutput extends AbstractActionOutput
{
   private OpenAPI openAPI;
   private String  yaml;
   private String  json;



   /*******************************************************************************
    ** Getter for openAPI
    *******************************************************************************/
   public OpenAPI getOpenAPI()
   {
      return (this.openAPI);
   }



   /*******************************************************************************
    ** Setter for openAPI
    *******************************************************************************/
   public void setOpenAPI(OpenAPI openAPI)
   {
      this.openAPI = openAPI;
   }



   /*******************************************************************************
    ** Fluent setter for openAPI
    *******************************************************************************/
   public GenerateOpenApiSpecOutput withOpenAPI(OpenAPI openAPI)
   {
      this.openAPI = openAPI;
      return (this);
   }



   /*******************************************************************************
    ** Getter for yaml
    *******************************************************************************/
   public String getYaml()
   {
      return (this.yaml);
   }



   /*******************************************************************************
    ** Setter for yaml
    *******************************************************************************/
   public void setYaml(String yaml)
   {
      this.yaml = yaml;
   }



   /*******************************************************************************
    ** Fluent setter for yaml
    *******************************************************************************/
   public GenerateOpenApiSpecOutput withYaml(String yaml)
   {
      this.yaml = yaml;
      return (this);
   }



   /*******************************************************************************
    ** Getter for json
    *******************************************************************************/
   public String getJson()
   {
      return (this.json);
   }



   /*******************************************************************************
    ** Setter for json
    *******************************************************************************/
   public void setJson(String json)
   {
      this.json = json;
   }



   /*******************************************************************************
    ** Fluent setter for json
    *******************************************************************************/
   public GenerateOpenApiSpecOutput withJson(String json)
   {
      this.json = json;
      return (this);
   }

}
