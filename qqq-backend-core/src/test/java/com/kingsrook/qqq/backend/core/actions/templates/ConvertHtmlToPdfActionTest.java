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

package com.kingsrook.qqq.backend.core.actions.templates;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.templates.ConvertHtmlToPdfInput;
import com.kingsrook.qqq.backend.core.model.actions.templates.RenderTemplateInput;
import com.kingsrook.qqq.backend.core.model.actions.templates.RenderTemplateOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.templates.TemplateType;
import com.kingsrook.qqq.backend.core.utils.LocalMacDevUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for ConvertHtmlToPdfAction
 *******************************************************************************/
class ConvertHtmlToPdfActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException, IOException
   {
      QInstance instance = QContext.getQInstance();

      RenderTemplateInput renderTemplateInput = new RenderTemplateInput();
      renderTemplateInput.setCode("""
         <html>
             <head>
                 <style>
                     .center_div {
                         border: 1px solid gray;
                         margin-left: auto;
                         margin-right: auto;
                         width: 90%;
                         background-color: #d0f0f6;
                         text-align: left;
                         padding: 8px;
                         font-family: Helvetica;
                     }
                 </style>
                 <link href="styles/styles.css" rel="stylesheet">
             </head>
             <body>
                 <div class="center_div">
                     <h1>
                        <img src="images/qqq-logo-2.png" width=50>
                        Hello, $name
                     </h1>
                     <div class="myclass">
                         <p>This is a test of converting HTML to PDF!!</p>
                         <p>This is &nbsp; a line with &bull; some entities &lt;</p>
                         <p style="font-family: SF-Pro; font-size: 24px;">(btw, is this in SF-Pro???)</p>
                     </div>
                 </div>
             </body>
         </html>
         """);
      renderTemplateInput.setTemplateType(TemplateType.VELOCITY);
      renderTemplateInput.setContext(Map.of("name", "Darin"));
      RenderTemplateOutput renderTemplateOutput = new RenderTemplateAction().execute(renderTemplateInput);

      ConvertHtmlToPdfInput input = new ConvertHtmlToPdfInput();
      input.setHtml(renderTemplateOutput.getResult());

      OutputStream outputStream = new FileOutputStream("/tmp/file.pdf");
      input.setOutputStream(outputStream);

      String resourceDir = "src/test/resources/actions/templates";
      input.withCustomFont("SF-Pro", Path.of(resourceDir + "/fonts/SF-Pro-Rounded-Regular.otf"));
      input.withCustomFont("Helvetica", Path.of(resourceDir + "/fonts/Helvetica.ttc"));
      input.withBasePath(Path.of(resourceDir));

      new ConvertHtmlToPdfAction().execute(input);
      System.out.println("Wrote /tmp/file.pdf");

      outputStream.close();

      /////////////////////////////////////////////////////////////////////////
      // for local dev on a mac, turn this on to auto-open the generated PDF //
      /////////////////////////////////////////////////////////////////////////
      // LocalMacDevUtils.mayOpenFiles = true;
      LocalMacDevUtils.openFile("/tmp/file.pdf");
   }

}