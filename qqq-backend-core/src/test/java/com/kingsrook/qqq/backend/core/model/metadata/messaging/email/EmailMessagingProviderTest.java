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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.messaging.SendMessageAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.messaging.Content;
import com.kingsrook.qqq.backend.core.model.actions.messaging.MultiParty;
import com.kingsrook.qqq.backend.core.model.actions.messaging.Party;
import com.kingsrook.qqq.backend.core.model.actions.messaging.SendMessageInput;
import com.kingsrook.qqq.backend.core.model.actions.messaging.email.EmailContentRole;
import com.kingsrook.qqq.backend.core.model.actions.messaging.email.EmailPartyRole;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for EmailMessagingProvider
 *******************************************************************************/
class EmailMessagingProviderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @DisabledOnOs(OS.LINUX)
   void test() throws QException
   {
      new SendMessageAction().execute(new SendMessageInput()
         .withMessagingProviderName(TestUtils.EMAIL_MESSAGING_PROVIDER_NAME)
         .withTo(new MultiParty()
            .withParty(new Party().withAddress("darin.kelkhoff@gmail.com").withLabel("Darin Kelkhoff").withRole(EmailPartyRole.TO))
            .withParty(new Party().withAddress("james.maes@kingsrook.com").withLabel("Mames Maes").withRole(EmailPartyRole.CC))
            .withParty(new Party().withAddress("tyler.samples@kingsrook.com").withLabel("Tylers Ample").withRole(EmailPartyRole.BCC))
         )
         .withFrom(new Party().withAddress("darin.kelkhoff@gmail.com").withLabel("Darin Kelkhoff"))
         .withSubject("This is another qqq test message.")
         .withContent(new Content().withContentRole(EmailContentRole.TEXT).withBody("This is a text body"))
         .withContent(new Content().withContentRole(EmailContentRole.HTML).withBody("This <u>is</u> an <b>HTML</b> body!"))
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMissingInputs()
   {
      assertThatThrownBy(() -> new SendMessageAction().execute(new SendMessageInput()))
         .hasMessageContaining("provider name was not given");

      assertThatThrownBy(() -> new SendMessageAction().execute(new SendMessageInput().withMessagingProviderName("notFound")))
         .hasMessageContaining("was not found");
   }

}
