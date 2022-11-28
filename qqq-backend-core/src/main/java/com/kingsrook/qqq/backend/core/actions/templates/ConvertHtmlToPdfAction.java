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


import java.nio.file.Path;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.templates.ConvertHtmlToPdfInput;
import com.kingsrook.qqq.backend.core.model.templates.ConvertHtmlToPdfOutput;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;


/*******************************************************************************
 ** Action to convert a string of HTML to a PDF!
 **
 ** Much credit to https://www.baeldung.com/java-html-to-pdf
 *******************************************************************************/
public class ConvertHtmlToPdfAction extends AbstractQActionFunction<ConvertHtmlToPdfInput, ConvertHtmlToPdfOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ConvertHtmlToPdfOutput execute(ConvertHtmlToPdfInput input) throws QException
   {
      try
      {
         ConvertHtmlToPdfOutput output = new ConvertHtmlToPdfOutput();

         //////////////////////////////////////////////////////////////////
         // convert the input HTML to XHTML, as needed for ITextRenderer //
         //////////////////////////////////////////////////////////////////
         Document document = Jsoup.parse(input.getHtml());
         document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

         //////////////////////////////
         // convert the XHTML to PDF //
         //////////////////////////////
         ITextRenderer renderer      = new ITextRenderer();
         SharedContext sharedContext = renderer.getSharedContext();
         sharedContext.setPrint(true);
         sharedContext.setInteractive(false);

         if(input.getBasePath() != null)
         {
            String baseUrl = input.getBasePath().toUri().toURL().toString();
            renderer.setDocumentFromString(document.html(), baseUrl);
         }
         else
         {
            renderer.setDocumentFromString(document.html());
         }

         //////////////////////////////////////////////////
         // register any custom fonts the input supplied //
         //////////////////////////////////////////////////
         for(Map.Entry<String, Path> entry : CollectionUtils.nonNullMap(input.getCustomFonts()).entrySet())
         {
            renderer.getFontResolver().addFont(entry.getValue().toAbsolutePath().toString(), entry.getKey(), "UTF-8", true, null);
         }

         renderer.layout();
         renderer.createPDF(input.getOutputStream());

         return (output);
      }
      catch(Exception e)
      {
         throw (new QException("Error converting html to pdf", e));
      }
   }

}
