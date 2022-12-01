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


import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.templates.RenderTemplateInput;
import com.kingsrook.qqq.backend.core.model.templates.RenderTemplateOutput;
import com.kingsrook.qqq.backend.core.model.templates.TemplateType;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for RenderTemplateAction
 *******************************************************************************/
class RenderTemplateActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      RenderTemplateInput renderTemplateInput = new RenderTemplateInput(TestUtils.defineInstance());
      renderTemplateInput.setSession(new QSession());
      renderTemplateInput.setCode("""
         Hello, $name""");
      renderTemplateInput.setContext(Map.of("name", "Darin"));
      renderTemplateInput.setTemplateType(TemplateType.VELOCITY);
      RenderTemplateOutput output = new RenderTemplateAction().execute(renderTemplateInput);
      assertEquals("Hello, Darin", output.getResult());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testConvenientWrapper() throws QException
   {
      RenderTemplateInput parentActionInput = new RenderTemplateInput(TestUtils.defineInstance());
      parentActionInput.setSession(new QSession());

      String template = "Hello, $name";
      assertEquals("Hello, Darin", RenderTemplateAction.renderVelocity(parentActionInput, Map.of("name", "Darin"), template));
      assertEquals("Hello, Tim", RenderTemplateAction.renderVelocity(parentActionInput, Map.of("name", "Tim"), template));
      assertEquals("Hello, $name", RenderTemplateAction.renderVelocity(parentActionInput, Map.of(), template));

      template = "Hello, $!name";
      assertEquals("Hello, ", RenderTemplateAction.renderVelocity(parentActionInput, Map.of(), template));
      assertEquals("Hello, ", RenderTemplateAction.renderVelocity(parentActionInput, null, template));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMissingType()
   {
      RenderTemplateInput parentActionInput = new RenderTemplateInput(TestUtils.defineInstance());
      parentActionInput.setSession(new QSession());

      assertThatThrownBy(() -> RenderTemplateAction.render(parentActionInput, null, Map.of("name", "Darin"), "Hello, $name"))
         .isInstanceOf(QException.class)
         .hasMessageContaining("Unsupported Template Type");
   }

}