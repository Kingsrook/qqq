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

package com.kingsrook.qqq.middleware.javalin.specs.v1.responses.components;


import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockStylesInterface;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.base.BaseStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.button.ButtonStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.image.ImageStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text.TextStyles;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/*******************************************************************************
 **
 *******************************************************************************/
public sealed interface WidgetBlockStyles extends ToSchema permits
   WidgetBlockBaseStyles,
   WidgetBlockButtonStyles,
   WidgetBlockImageStyles,
   WidgetBlockTextStyles
{
   @OpenAPIExclude
   QLogger LOG = QLogger.getLogger(WidgetBlockStyles.class);


   /***************************************************************************
    **
    ***************************************************************************/
   static WidgetBlockStyles of(BlockStylesInterface blockStyles)
   {
      if(blockStyles == null)
      {
         return (null);
      }

      if(blockStyles instanceof ButtonStyles s)
      {
         return (new WidgetBlockButtonStyles(s));
      }
      else if(blockStyles instanceof ImageStyles s)
      {
         return (new WidgetBlockImageStyles(s));
      }
      else if(blockStyles instanceof TextStyles s)
      {
         return (new WidgetBlockTextStyles(s));
      }
      //////////////////////////////////////////////////////////////////////////////////////////////
      // note - important for this one to be last, since it's a base class to some of the above!! //
      //////////////////////////////////////////////////////////////////////////////////////////////
      else if(blockStyles instanceof BaseStyles s)
      {
         return (new WidgetBlockBaseStyles(s));
      }

      LOG.warn("Unrecognized block value type: " + blockStyles.getClass().getName());
      return (null);
   }

}
