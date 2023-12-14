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

package com.kingsrook.qqq.backend.core.model.metadata.help;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/*******************************************************************************
 ** meta-data defintion of "Help Content" to show to a user - for use in
 ** a specific "role" (e.g., insert screens but not view screens), and in a
 ** particular "format" (e.g., plain text, html, markdown).
 **
 ** Meant to be assigned to several different pieces of QQQ meta data (fields,
 ** tables, processes, etc), and used as-needed by various frontends.
 **
 ** May evolve something like a "Presentation" attribute in the future - e.g.,
 ** to say "present this one as a tooltip" vs. "present this one as inline text"
 **
 ** May be dynamically added to meta-data via (non-meta-) data - see
 ** HelpContentMetaDataProvider and QInstanceHelpContentManager
 *******************************************************************************/
public class QHelpContent
{
   private String        content;
   private HelpFormat    format;
   private Set<HelpRole> roles;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QHelpContent()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QHelpContent(String content)
   {
      setContent(content);
   }



   /*******************************************************************************
    ** Getter for content
    *******************************************************************************/
   public String getContent()
   {
      return (this.content);
   }



   /*******************************************************************************
    ** Setter for content
    *******************************************************************************/
   public void setContent(String content)
   {
      this.content = content;
   }



   /*******************************************************************************
    ** Fluent setter for content
    *******************************************************************************/
   public QHelpContent withContent(String content)
   {
      this.content = content;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for content that also sets format as HTML
    *******************************************************************************/
   public QHelpContent withContentAsHTML(String content)
   {
      this.content = content;
      this.format = HelpFormat.HTML;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for content that also sets format as TEXT
    *******************************************************************************/
   public QHelpContent withContentAsText(String content)
   {
      this.content = content;
      this.format = HelpFormat.TEXT;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for content that also sets format as Markdown
    *******************************************************************************/
   public QHelpContent withContentAsMarkdown(String content)
   {
      this.content = content;
      this.format = HelpFormat.MARKDOWN;
      return (this);
   }



   /*******************************************************************************
    ** Getter for format
    *******************************************************************************/
   public HelpFormat getFormat()
   {
      return (this.format);
   }



   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(HelpFormat format)
   {
      this.format = format;
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public QHelpContent withFormat(HelpFormat format)
   {
      this.format = format;
      return (this);
   }



   /*******************************************************************************
    ** Getter for roles
    *******************************************************************************/
   public Set<HelpRole> getRoles()
   {
      return (this.roles);
   }



   /*******************************************************************************
    ** Setter for roles
    *******************************************************************************/
   public void setRoles(Set<HelpRole> roles)
   {
      this.roles = roles;
   }



   /*******************************************************************************
    ** Fluent setter for roles
    *******************************************************************************/
   public QHelpContent withRoles(Set<HelpRole> roles)
   {
      this.roles = roles;
      return (this);
   }



   /*******************************************************************************
    ** Fluent method to add a role
    *******************************************************************************/
   public QHelpContent withRole(HelpRole role)
   {
      return (withRoles(role));
   }



   /*******************************************************************************
    ** Fluent method to add a role
    *******************************************************************************/
   public QHelpContent withRoles(HelpRole... roles)
   {
      if(roles == null || roles.length == 0)
      {
         return (this);
      }

      if(this.roles == null)
      {
         this.roles = new HashSet<>();
      }

      Collections.addAll(this.roles, roles);

      return (this);
   }

}
