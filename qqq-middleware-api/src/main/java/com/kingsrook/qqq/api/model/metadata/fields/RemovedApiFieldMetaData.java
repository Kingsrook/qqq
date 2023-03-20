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

package com.kingsrook.qqq.api.model.metadata.fields;


import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;


/*******************************************************************************
 **
 *******************************************************************************/
public class RemovedApiFieldMetaData extends QFieldMetaData
{
   private String finalVersion;
   private String replacedByFieldName;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public RemovedApiFieldMetaData()
   {
      super();
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public RemovedApiFieldMetaData(String name, QFieldType type)
   {
      super(name, type);
   }



   /*******************************************************************************
    ** Getter for replacedByFieldName
    *******************************************************************************/
   public String getReplacedByFieldName()
   {
      return (this.replacedByFieldName);
   }



   /*******************************************************************************
    ** Setter for replacedByFieldName
    *******************************************************************************/
   public void setReplacedByFieldName(String replacedByFieldName)
   {
      this.replacedByFieldName = replacedByFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for replacedByFieldName
    *******************************************************************************/
   public RemovedApiFieldMetaData withReplacedByFieldName(String replacedByFieldName)
   {
      this.replacedByFieldName = replacedByFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for finalVersion
    *******************************************************************************/
   public String getFinalVersion()
   {
      return (this.finalVersion);
   }



   /*******************************************************************************
    ** Setter for finalVersion
    *******************************************************************************/
   public void setFinalVersion(String finalVersion)
   {
      this.finalVersion = finalVersion;
   }



   /*******************************************************************************
    ** Fluent setter for finalVersion
    *******************************************************************************/
   public RemovedApiFieldMetaData withFinalVersion(String finalVersion)
   {
      this.finalVersion = finalVersion;
      return (this);
   }

}
