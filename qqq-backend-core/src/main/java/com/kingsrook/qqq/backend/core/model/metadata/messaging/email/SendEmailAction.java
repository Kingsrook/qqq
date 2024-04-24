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

package com.kingsrook.qqq.backend.core.model.metadata.messaging.email;


import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.messaging.MultiParty;
import com.kingsrook.qqq.backend.core.model.actions.messaging.Party;
import com.kingsrook.qqq.backend.core.model.actions.messaging.PartyRole;
import com.kingsrook.qqq.backend.core.model.actions.messaging.SendMessageInput;
import com.kingsrook.qqq.backend.core.model.actions.messaging.SendMessageOutput;
import com.kingsrook.qqq.backend.core.model.actions.messaging.email.EmailPartyRole;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


/*******************************************************************************
 **
 *******************************************************************************/
public class SendEmailAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public SendMessageOutput sendMessage(SendMessageInput sendMessageInput) throws QException
   {
      EmailMessagingProviderMetaData messagingProvider = (EmailMessagingProviderMetaData) QContext.getQInstance().getMessagingProvider(sendMessageInput.getMessagingProviderName());

      /////////////////////////////////////////
      // set up properties to make a session //
      /////////////////////////////////////////
      Properties properties = System.getProperties();
      properties.setProperty("mail.smtp.host", messagingProvider.getSmtpServer());
      properties.setProperty("mail.smtp.port", messagingProvider.getSmtpPort());
      Session session = Session.getDefaultInstance(properties);

      try
      {
         ////////////////////////////////////////////
         // Construct a default MimeMessage object //
         ////////////////////////////////////////////
         MimeMessage emailMessage = new MimeMessage(session);
         emailMessage.setSubject(sendMessageInput.getSubject());
         emailMessage.setText(sendMessageInput.getContentList().get(0).getBody());

         Party to = sendMessageInput.getTo();
         if(to instanceof MultiParty toMultiParty)
         {
            for(Party party : toMultiParty.getPartyList())
            {
               addRecipient(emailMessage, party);
            }
         }
         else
         {
            addRecipient(emailMessage, to);
         }

         Party from = sendMessageInput.getFrom();
         if(from instanceof MultiParty fromMultiParty)
         {
            for(Party party : fromMultiParty.getPartyList())
            {
               addSender(emailMessage, party);
            }
         }
         else
         {
            addSender(emailMessage, from);
         }

         /////////////
         // send it //
         /////////////
         Transport.send(emailMessage);
         System.out.println("Message dispatched successfully...");
      }
      catch(Exception e)
      {
         throw (new QException("Error sending email", e));
      }

      return null;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addSender(MimeMessage emailMessage, Party party) throws Exception
   {
      if(EmailPartyRole.REPLY_TO.equals(party.getRole()))
      {
         InternetAddress internetAddress = getInternetAddressFromParty(party);
         Address[]       replyTo         = emailMessage.getReplyTo();
         if(replyTo == null || replyTo.length == 0)
         {
            emailMessage.setReplyTo(new Address[] { internetAddress });
         }
         else
         {
            List<Address> replyToList = Arrays.asList(replyTo);
            replyToList.add(internetAddress);
            emailMessage.setReplyTo(replyToList.toArray(new Address[0]));
         }
      }
      else if(party.getRole() == null || PartyRole.Default.DEFAULT.equals(party.getRole()) || EmailPartyRole.FROM.equals(party.getRole()))
      {
         emailMessage.setFrom(getInternetAddressFromParty(party));
      }
      else
      {
         throw (new QException("Unrecognized sender role [" + party.getRole() + "]"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecipient(MimeMessage emailMessage, Party party) throws Exception
   {
      Message.RecipientType recipientType;
      if(EmailPartyRole.CC.equals(party.getRole()))
      {
         recipientType = Message.RecipientType.CC;
      }
      else if(EmailPartyRole.BCC.equals(party.getRole()))
      {
         recipientType = Message.RecipientType.BCC;
      }
      else if(party.getRole() == null || PartyRole.Default.DEFAULT.equals(party.getRole()) || EmailPartyRole.TO.equals(party.getRole()))
      {
         recipientType = Message.RecipientType.TO;
      }
      else
      {
         throw (new QException("Unrecognized recipient role [" + party.getRole() + "]"));
      }

      InternetAddress internetAddress = getInternetAddressFromParty(party);
      emailMessage.addRecipient(recipientType, internetAddress);
      System.out.println("add recipient: [" + recipientType + "] => [" + internetAddress + "]");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static InternetAddress getInternetAddressFromParty(Party party) throws AddressException, UnsupportedEncodingException
   {
      InternetAddress internetAddress = new InternetAddress(party.getAddress());
      if(StringUtils.hasContent(party.getLabel()))
      {
         internetAddress.setPersonal(party.getLabel());
      }
      return internetAddress;
   }

}
