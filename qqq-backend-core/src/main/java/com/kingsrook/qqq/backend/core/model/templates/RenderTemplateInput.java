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

package com.kingsrook.qqq.backend.core.model.templates;


import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;


/*******************************************************************************
 **
 *******************************************************************************/
public class RenderTemplateInput extends AbstractActionInput
{
   private String       code; // todo - TemplateReference, like CodeReference??
   private TemplateType templateType;

   private Map<String, Object> context;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RenderTemplateInput()
   {
   }



   /*******************************************************************************
    ** Getter for code
    **
    *******************************************************************************/
   public String getCode()
   {
      return code;
   }



   /*******************************************************************************
    ** Setter for code
    **
    *******************************************************************************/
   public void setCode(String code)
   {
      this.code = code;
   }



   /*******************************************************************************
    ** Fluent setter for code
    **
    *******************************************************************************/
   public RenderTemplateInput withCode(String code)
   {
      this.code = code;
      return (this);
   }



   /*******************************************************************************
    ** Getter for templateType
    **
    *******************************************************************************/
   public TemplateType getTemplateType()
   {
      return templateType;
   }



   /*******************************************************************************
    ** Setter for templateType
    **
    *******************************************************************************/
   public void setTemplateType(TemplateType templateType)
   {
      this.templateType = templateType;
   }



   /*******************************************************************************
    ** Fluent setter for templateType
    **
    *******************************************************************************/
   public RenderTemplateInput withTemplateType(TemplateType templateType)
   {
      this.templateType = templateType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for context
    **
    *******************************************************************************/
   public Map<String, Object> getContext()
   {
      return context;
   }



   /*******************************************************************************
    ** Setter for context
    **
    *******************************************************************************/
   public void setContext(Map<String, Object> context)
   {
      this.context = context;
   }



   /*******************************************************************************
    ** Fluent setter for context
    **
    *******************************************************************************/
   public RenderTemplateInput withContext(Map<String, Object> context)
   {
      this.context = context;
      return (this);
   }

}
