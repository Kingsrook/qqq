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

package com.kingsrook.sampleapp.selenium;


import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.sampleapp.SampleJavalinServer;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 **
 *******************************************************************************/
public class BaseSampleSeleniumTest // extends QBaseSeleniumTest
{
   private static final QLogger LOG = QLogger.getLogger(BaseSampleSeleniumTest.class);

   public static final Integer DEFAULT_WAIT_SECONDS = 10;

   private int port = 8011;


   ///*******************************************************************************
   // **
   // *******************************************************************************/
   //@Override
   //@BeforeEach
   //public void beforeEach()
   //{
   //   super.beforeEach();
   //   qSeleniumLib.withBaseUrl("http://localhost:" + port);
   //   qSeleniumLib.withWaitSeconds(DEFAULT_WAIT_SECONDS);
   //
   //   new SampleJavalinServer().startJavalinServer(port);
   //}
   //
   //
   //
   ///*******************************************************************************
   // **
   // *******************************************************************************/
   //@Override
   //protected boolean useInternalJavalin()
   //{
   //   return (false);
   //}
   //
   //
   //
   //
   ///*******************************************************************************
   // **
   // *******************************************************************************/
   //public void clickLeftNavMenuItem(String text)
   //{
   //   qSeleniumLib.waitForSelectorContaining(".MuiDrawer-paperAnchorLeft .MuiListItem-root", text).click();
   //}
   //
   //
   //
   ///*******************************************************************************
   // **
   // *******************************************************************************/
   //public void clickLeftNavMenuItemThenSubItem(String text, String subItemText)
   //{
   //   qSeleniumLib.waitForSelectorContaining(".MuiDrawer-paperAnchorLeft .MuiListItem-root", text).click();
   //   qSeleniumLib.waitForSelectorContaining(".MuiDrawer-paperAnchorLeft .MuiCollapse-vertical.MuiCollapse-entered .MuiListItem-root", subItemText).click();
   //}
   //
   //
   //
   ///*******************************************************************************
   // **
   // *******************************************************************************/
   //public void goToPathAndWaitForSelectorContaining(String path, String selector, String text)
   //{
   //   driver.get(qSeleniumLib.getBaseUrl() + path);
   //   qSeleniumLib.waitForSelectorContaining(selector, text);
   //}

}

