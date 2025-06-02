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


import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetTableApiFieldsInput extends AbstractActionInput
{
   private String apiName;
   private String tableName;
   private String version;

   /////////////////////////////////////////////////////////////////////////////////////
   // by default, this action will throw if the input table isn't in the api version. //
   // but, to preserve legacy behavior where that didn't happen, allow this input.    //
   /////////////////////////////////////////////////////////////////////////////////////
   private Boolean doCheckTableApiVersion = true;



   /*******************************************************************************
    ** Getter for version
    *******************************************************************************/
   public String getVersion()
   {
      return (this.version);
   }



   /*******************************************************************************
    ** Setter for version
    *******************************************************************************/
   public void setVersion(String version)
   {
      this.version = version;
   }



   /*******************************************************************************
    ** Fluent setter for version
    *******************************************************************************/
   public GetTableApiFieldsInput withVersion(String version)
   {
      this.version = version;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public GetTableApiFieldsInput withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiName
    *******************************************************************************/
   public String getApiName()
   {
      return (this.apiName);
   }



   /*******************************************************************************
    ** Setter for apiName
    *******************************************************************************/
   public void setApiName(String apiName)
   {
      this.apiName = apiName;
   }



   /*******************************************************************************
    ** Fluent setter for apiName
    *******************************************************************************/
   public GetTableApiFieldsInput withApiName(String apiName)
   {
      this.apiName = apiName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for doCheckTableApiVersion
    *******************************************************************************/
   public Boolean getDoCheckTableApiVersion()
   {
      return (this.doCheckTableApiVersion);
   }



   /*******************************************************************************
    ** Setter for doCheckTableApiVersion
    *******************************************************************************/
   public void setDoCheckTableApiVersion(Boolean doCheckTableApiVersion)
   {
      this.doCheckTableApiVersion = doCheckTableApiVersion;
   }



   /*******************************************************************************
    ** Fluent setter for doCheckTableApiVersion
    *******************************************************************************/
   public GetTableApiFieldsInput withDoCheckTableApiVersion(Boolean doCheckTableApiVersion)
   {
      this.doCheckTableApiVersion = doCheckTableApiVersion;
      return (this);
   }

}
