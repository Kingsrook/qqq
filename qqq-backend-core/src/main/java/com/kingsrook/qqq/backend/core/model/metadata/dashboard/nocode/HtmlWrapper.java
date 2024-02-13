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


import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class HtmlWrapper implements Serializable
{
   private String prefix;
   private String suffix;

   public static final HtmlWrapper SUBHEADER    = new HtmlWrapper("<h4>", "</h4>");
   public static final HtmlWrapper BIG_CENTERED = new HtmlWrapper("<div style='font-size: 2rem; font-weight: 400; line-height: 1.625; text-align: center; padding-bottom: 8px;'>", "</div>");
   public static final HtmlWrapper INDENT_1     = new HtmlWrapper("<div style='padding-left: 1rem;'>", "</div>");
   public static final HtmlWrapper INDENT_2     = new HtmlWrapper("<div style='padding-left: 2rem;'>", "</div>");
   public static final HtmlWrapper FLOAT_RIGHT  = new HtmlWrapper("<div style='float: right'>", "</div>");
   public static final HtmlWrapper RULE_ABOVE   = new HtmlWrapper("""
      <hr style="opacity: 0.25; height: 0.0625rem; border-width: 0; margin-bottom: 1rem; background-image: linear-gradient(to right, rgba(52, 71, 103, 0), rgba(52, 71, 103, 0.4), rgba(52, 71, 103, 0));" />
      """, "");

   public static final String STYLE_BIG_CENTERED    = "font-size: 2rem; font-weight: 400; line-height: 1.625; text-align: center; padding-bottom: 8px; ";
   public static final String STYLE_MEDIUM_CENTERED = "font-size: 1.5rem; font-weight: 400; line-height: 1.625; text-align: center; padding-bottom: 4px; ";
   public static final String STYLE_INDENT_1        = "padding-left: 1rem; ";
   public static final String STYLE_INDENT_2        = "padding-left: 2rem; ";
   public static final String STYLE_FLOAT_RIGHT     = "float: right; ";
   public static final String STYLE_RED             = "color: red; ";
   public static final String STYLE_YELLOW = "color: #bfb743; ";



   /*******************************************************************************
    **
    *******************************************************************************/
   public HtmlWrapper(String prefix, String suffix)
   {
      this.prefix = prefix;
      this.suffix = suffix;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static HtmlWrapper divWithStyles(String... styles)
   {
      String style = StringUtils.join("", Arrays.asList(styles));
      return (new HtmlWrapper("<div style=\"" + style + "\">", "</div>"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static HtmlWrapper width(String amount)
   {
      return (new HtmlWrapper("<div style='width: " + amount + "'>", "</div>"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static HtmlWrapper paddingTop(String amount)
   {
      return (new HtmlWrapper("<div style='padding-top: " + amount + "'>", "</div>"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String styleWidth(String amount)
   {
      return ("width: " + amount);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String wrap(String content)
   {
      return (Objects.requireNonNullElse(prefix, "")
         + "\n" + Objects.requireNonNullElse(content, "") + "\n"
         + Objects.requireNonNullElse(suffix, "") + "\n");
   }



   /*******************************************************************************
    ** Getter for prefix
    **
    *******************************************************************************/
   public String getPrefix()
   {
      return prefix;
   }



   /*******************************************************************************
    ** Setter for prefix
    **
    *******************************************************************************/
   public void setPrefix(String prefix)
   {
      this.prefix = prefix;
   }



   /*******************************************************************************
    ** Getter for suffix
    **
    *******************************************************************************/
   public String getSuffix()
   {
      return suffix;
   }



   /*******************************************************************************
    ** Setter for suffix
    **
    *******************************************************************************/
   public void setSuffix(String suffix)
   {
      this.suffix = suffix;
   }
}
