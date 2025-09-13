/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.help;


import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QHelpContent 
 *******************************************************************************/
class QHelpContentTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetContentAsHtml()
   {
      assertNull(new QHelpContent().withFormat(null).withContent(null).getContentAsHtml());
      assertNull(new QHelpContent().withFormat(HelpFormat.MARKDOWN).withContent(null).getContentAsHtml());
      assertNull(new QHelpContent().withFormat(HelpFormat.HTML).withContent(null).getContentAsHtml());
      assertNull(new QHelpContent().withFormat(HelpFormat.TEXT).withContent(null).getContentAsHtml());

      assertEquals("<p><em>hi</em></p>\n", new QHelpContent().withFormat(HelpFormat.MARKDOWN).withContent("*hi*").getContentAsHtml());
      assertEquals("<i>hi</i>", new QHelpContent().withFormat(HelpFormat.HTML).withContent("<i>hi</i>").getContentAsHtml());
      assertEquals("hi", new QHelpContent().withFormat(HelpFormat.TEXT).withContent("hi").getContentAsHtml());
      assertEquals("*hi*", new QHelpContent().withFormat(HelpFormat.TEXT).withContent("*hi*").getContentAsHtml());
      assertEquals("*hi*", new QHelpContent().withFormat(null).withContent("*hi*").getContentAsHtml());
   }

}