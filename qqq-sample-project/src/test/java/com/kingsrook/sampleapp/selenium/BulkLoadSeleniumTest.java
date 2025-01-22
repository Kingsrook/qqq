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


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkLoadSeleniumTest extends BaseSampleSeleniumTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled("selenium not working in circleci at this time...")
   void testSimple() throws IOException
   {
      String email = "jtkirk@starfleet.com";
      String tablePath = "/peopleApp/greetingsApp/person";

      ////////////////////////////////////
      // write a file to be bulk-loaded //
      ////////////////////////////////////
      String path = "/tmp/" + UUID.randomUUID() + ".csv";
      String csv = String.format("""
         email,firstName,lastName
         %s,James T.,Kirk
         """, email);
      FileUtils.writeStringToFile(new File(path), csv, StandardCharsets.UTF_8);

      //goToPathAndWaitForSelectorContaining(tablePath + "/person.bulkInsert", ".MuiTypography-h5", "Person Bulk Insert: Upload File");
      //
      ////////////////////////////////
      //// complete the upload form //
      ////////////////////////////////
      //qSeleniumLib.waitForSelector("input[type=file]").sendKeys(path);
      //qSeleniumLib.waitForSelectorContaining("button", "next").click();
      //
      ///////////////////////////////////////////
      //// proceed through file-mapping screen //
      ///////////////////////////////////////////
      //qSeleniumLib.waitForSelectorContaining("button", "next").click();
      //
      ////////////////////////////////////////////////////
      //// confirm data on preview screen, then proceed //
      ////////////////////////////////////////////////////
      //qSeleniumLib.waitForSelectorContaining("form#review .MuiTypography-body2 div", email);
      //qSeleniumLib.waitForSelectorContaining("form#review .MuiTypography-body2 div", "Preview 1 of 1");
      //qSeleniumLib.waitForSelectorContaining("button", "arrow_forward").click(); // to avoid the record-preview 'next' button
      //
      /////////////////////////////////////////
      //// proceed through validation screen //
      /////////////////////////////////////////
      //qSeleniumLib.waitForSelectorContaining("button", "submit").click();
      //
      //////////////////////////////////////////
      //// confirm result screen and close it //
      //////////////////////////////////////////
      //qSeleniumLib.waitForSelectorContaining(".MuiListItemText-root", "1 Person record was inserted");
      //qSeleniumLib.waitForSelectorContaining("button", "close").click();
      //
      //////////////////////////////////////////////
      //// go to the order that was just inserted //
      //// bonus - also test record-view-by-key   //
      //////////////////////////////////////////////
      //goToPathAndWaitForSelectorContaining(tablePath + "/key?email=" + email, "h5", "Viewing Person");
   }

}
