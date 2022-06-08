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

package com.kingsrook.qqq.backend.module.rdbms;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;


/*******************************************************************************
 ** RDBMSBackendMetaData
 *
 *******************************************************************************/
public class RDBMSBackendMetaData extends QBackendMetaData
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public RDBMSBackendMetaData(QBackendMetaData source)
   {
      super();
      setName(source.getName());
      setValues(source.getValues());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getVendor()
   {
      return getValue("vendor");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getHostName()
   {
      return getValue("hostName");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getPort()
   {
      return getValue("port");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getDatabaseName()
   {
      return getValue("databaseName");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getUsername()
   {
      return getValue("username");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getPassword()
   {
      return getValue("password");
   }

}
