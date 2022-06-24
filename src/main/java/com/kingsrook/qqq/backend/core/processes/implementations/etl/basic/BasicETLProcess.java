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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.basic;


import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Definition for Basic ETL process.
 *******************************************************************************/
public class BasicETLProcess
{
   public static final String PROCESS_NAME            = "etl.basic";
   public static final String FIELD_SOURCE_TABLE      = "sourceTable";
   public static final String FIELD_DESTINATION_TABLE = "destinationTable";
   public static final String FIELD_RECORD_COUNT      = "recordCount";



   /*******************************************************************************
    **
    *******************************************************************************/
   public QProcessMetaData defineProcessMetaData()
   {
      QFunctionMetaData extractFunction = new QFunctionMetaData()
         .withName("extract")
         .withCode(new QCodeReference()
            .withName(BasicETLExtractFunction.class.getName())
            .withCodeType(QCodeType.JAVA)
            .withCodeUsage(QCodeUsage.FUNCTION))
         .withInputData(new QFunctionInputMetaData()
            .addField(new QFieldMetaData(FIELD_SOURCE_TABLE, QFieldType.STRING)));

      QFunctionMetaData loadFunction = new QFunctionMetaData()
         .withName("load")
         .withCode(new QCodeReference()
            .withName(BasicETLLoadFunction.class.getName())
            .withCodeType(QCodeType.JAVA)
            .withCodeUsage(QCodeUsage.FUNCTION))
         .withInputData(new QFunctionInputMetaData()
            .addField(new QFieldMetaData(FIELD_DESTINATION_TABLE, QFieldType.STRING)))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .addField(new QFieldMetaData(FIELD_RECORD_COUNT, QFieldType.INTEGER)));

      return new QProcessMetaData()
         .withName(PROCESS_NAME)
         .addFunction(extractFunction)
         .addFunction(loadFunction);
   }
}
