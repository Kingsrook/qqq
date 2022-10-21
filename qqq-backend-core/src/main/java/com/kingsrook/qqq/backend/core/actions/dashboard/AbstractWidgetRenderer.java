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

package com.kingsrook.qqq.backend.core.actions.dashboard;


import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;


/*******************************************************************************
 ** Base class for rendering qqq dashboard widgets
 **
 *******************************************************************************/
public abstract class AbstractWidgetRenderer
{
   public static final QValueFormatter   valueFormatter    = new QValueFormatter();
   public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mma").withZone(ZoneId.systemDefault());
   public static final DateTimeFormatter dateFormatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract RenderWidgetOutput render(RenderWidgetInput input) throws QException;

}
