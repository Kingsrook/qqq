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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode;


import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.queues.GetQueueSize;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class QueueSizeWidgetValue extends AbstractWidgetValueSource
{
   private static final QLogger LOG = QLogger.getLogger(QueueSizeWidgetValue.class);

   private String queueName;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object evaluate(Map<String, Object> context, RenderWidgetInput input) throws QException
   {
      QQueueMetaData         queue         = QContext.getQInstance().getQueue(queueName);
      QQueueProviderMetaData queueProvider = QContext.getQInstance().getQueueProvider(queue.getProviderName());
      return (new GetQueueSize().getQueueSize(queueProvider, queue));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueueSizeWidgetValue withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for queueName
    *******************************************************************************/
   public String getQueueName()
   {
      return (this.queueName);
   }



   /*******************************************************************************
    ** Setter for queueName
    *******************************************************************************/
   public void setQueueName(String queueName)
   {
      this.queueName = queueName;
   }



   /*******************************************************************************
    ** Fluent setter for queueName
    *******************************************************************************/
   public QueueSizeWidgetValue withQueueName(String queueName)
   {
      this.queueName = queueName;
      return (this);
   }

}
