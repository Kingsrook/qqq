/*
 * Copyright © 2022-2023. ColdTrack <contact@coldtrack.com>.  All Rights Reserved.
 */

package com.kingsrook.sampleapp.selenium;


import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.frontend.materialdashboard.selenium.lib.QBaseSeleniumTest;
import com.kingsrook.sampleapp.SampleJavalinServer;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 **
 *******************************************************************************/
public class BaseSampleSeleniumTest extends QBaseSeleniumTest
{
   private static final QLogger LOG = QLogger.getLogger(BaseSampleSeleniumTest.class);

   public static final Integer DEFAULT_WAIT_SECONDS = 10;

   private int port = 8011;


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   @BeforeEach
   public void beforeEach()
   {
      super.beforeEach();
      qSeleniumLib.withBaseUrl("http://localhost:" + port);
      qSeleniumLib.withWaitSeconds(DEFAULT_WAIT_SECONDS);

      new SampleJavalinServer().startJavalinServer(port);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected boolean useInternalJavalin()
   {
      return (false);
   }




   /*******************************************************************************
    **
    *******************************************************************************/
   public void clickLeftNavMenuItem(String text)
   {
      qSeleniumLib.waitForSelectorContaining(".MuiDrawer-paperAnchorLeft .MuiListItem-root", text).click();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void clickLeftNavMenuItemThenSubItem(String text, String subItemText)
   {
      qSeleniumLib.waitForSelectorContaining(".MuiDrawer-paperAnchorLeft .MuiListItem-root", text).click();
      qSeleniumLib.waitForSelectorContaining(".MuiDrawer-paperAnchorLeft .MuiCollapse-vertical.MuiCollapse-entered .MuiListItem-root", subItemText).click();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void goToPathAndWaitForSelectorContaining(String path, String selector, String text)
   {
      driver.get(qSeleniumLib.getBaseUrl() + path);
      qSeleniumLib.waitForSelectorContaining(selector, text);
   }

}

