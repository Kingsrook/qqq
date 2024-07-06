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

package com.kingsrook.qqq.backend.core.actions.messaging;


import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.messaging.SendMessageInput;
import com.kingsrook.qqq.backend.core.model.actions.messaging.SendMessageOutput;
import com.kingsrook.qqq.backend.core.model.metadata.messaging.QMessagingProviderMetaData;
import com.kingsrook.qqq.backend.core.modules.messaging.MessagingProviderInterface;
import com.kingsrook.qqq.backend.core.modules.messaging.QMessagingProviderDispatcher;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SendMessageAction extends AbstractQActionFunction<SendMessageInput, SendMessageOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public SendMessageOutput execute(SendMessageInput input) throws QException
   {
      if(!StringUtils.hasContent(input.getMessagingProviderName()))
      {
         throw (new QException("Messaging provider name was not given in SendMessageInput."));
      }

      QMessagingProviderMetaData messagingProvider = QContext.getQInstance().getMessagingProvider(input.getMessagingProviderName());
      if(messagingProvider == null)
      {
         throw (new QException("Messaging provider named [" + input.getMessagingProviderName() + "] was not found in this QInstance."));
      }

      MessagingProviderInterface messagingProviderInterface = new QMessagingProviderDispatcher().getMessagingProviderInterface(messagingProvider.getType());

      return (messagingProviderInterface.sendMessage(input));
   }

}
