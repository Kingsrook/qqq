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

package com.kingsrook.qqq.backend.core.instances.loaders.implementations;


import java.util.Map;
import com.kingsrook.qqq.backend.core.instances.loaders.AbstractMetaDataLoader;
import com.kingsrook.qqq.backend.core.instances.loaders.LoadingContext;
import com.kingsrook.qqq.backend.core.instances.loaders.QMetaDataLoaderException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class QStepDataLoader extends AbstractMetaDataLoader<QStepMetaData>
{
   private static final QLogger LOG = QLogger.getLogger(QStepDataLoader.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QStepMetaData mapToMetaDataObject(QInstance qInstance, Map<String, Object> map, LoadingContext context) throws QMetaDataLoaderException
   {
      String stepType = ValueUtils.getValueAsString(map.get("stepType"));

      if(!StringUtils.hasContent(stepType))
      {
         throw (new QMetaDataLoaderException("stepType was not specified for process step"));
      }

      QStepMetaData step;
      if("backend".equalsIgnoreCase(stepType))
      {
         step = new QBackendStepMetaData();
         reflectivelyMap(qInstance, step, map, context);
      }
      else if("frontend".equalsIgnoreCase(stepType))
      {
         step = new QFrontendStepMetaData();
         reflectivelyMap(qInstance, step, map, context);
      }
      // todo - we have custom factory methods for this, so, maybe needs all custom loader?
      // else if("stateMachine".equalsIgnoreCase(stepType))
      // {
      //    step = new QStateMachineStep();
      //    reflectivelyMap(qInstance, step, map, context);
      // }
      else
      {
         throw (new QMetaDataLoaderException("Unsupported step stepType: " + stepType));
      }

      return (step);
   }

}
