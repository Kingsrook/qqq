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

package com.kingsrook.sampleapp;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class SampleMetaDataProvider
{
   private static final String BACKEND_NAME = "default";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(SampleMetaDataProvider.defineBackend());
      qInstance.addTable(SampleMetaDataProvider.defineTableCarrier());
      qInstance.addTable(SampleMetaDataProvider.defineTablePerson());
      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendMetaData defineBackend()
   {
      QBackendMetaData backend = new QBackendMetaData();
      backend.setName(BACKEND_NAME);
      backend.setType("rdbms");
      backend.setValue("vendor", "mysql");
      backend.setValue("hostName", "127.0.0.1");
      backend.setValue("port", "3306");
      backend.setValue("databaseName", "opspath");
      backend.setValue("username", "root");
      backend.setValue("password", "8BNWyoav8s79oi}Lqk"); // todo - load securely
      return (backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTableCarrier()
   {
      QTableMetaData table = new QTableMetaData();
      table.setName("carrier");
      table.setBackendName(BACKEND_NAME);
      table.setPrimaryKeyField("id");

      table.addField(new QFieldMetaData("id", QFieldType.INTEGER));

      table.addField(new QFieldMetaData("name", QFieldType.STRING));

      table.addField(new QFieldMetaData("company_code", QFieldType.STRING) // todo enum
         .withLabel("Company")
         .withBackendName("comp_code"));

      table.addField(new QFieldMetaData("service_level", QFieldType.STRING)); // todo enum

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName("person")
         .withLabel("Person")
         .withBackendName(BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name"))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name"))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING));
   }
}
