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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.button;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.AbstractBlockWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.base.BaseSlots;


/*******************************************************************************
 ** a button (for a process - not sure yet what this could do in a standalone
 ** widget?) to submit the process screen to run a specific action (e.g., not just
 ** 'next'), or do other control-ish things
 *******************************************************************************/
public class ButtonBlockData extends AbstractBlockWidgetData<ButtonBlockData, ButtonValues, BaseSlots, ButtonStyles>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getBlockTypeName()
   {
      return "BUTTON";
   }

}
