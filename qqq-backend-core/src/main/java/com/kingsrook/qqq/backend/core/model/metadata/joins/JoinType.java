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

package com.kingsrook.qqq.backend.core.model.metadata.joins;


/*******************************************************************************
 ** Type for a QJoin.
 **
 ** - One to One - or zero, i guess...
 ** - One to Many - e.g., where the parent record really "owns" all of the child
 **      records.  Like Order -> OrderLine.
 ** - Many to One - e.g., where a child references a parent, but we'd never really
 **      view or manage all of the children under the parent.
 ** - Many to Many - e.g., through an intersection table... ? Needs more thought.
 *******************************************************************************/
public enum JoinType
{
   ONE_TO_ONE,
   ONE_TO_MANY,
   MANY_TO_ONE,
   MANY_TO_MANY;



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   public JoinType flip()
   {
      return switch(this)
         {
            case ONE_TO_MANY -> MANY_TO_ONE;
            case MANY_TO_ONE -> ONE_TO_MANY;
            case MANY_TO_MANY, ONE_TO_ONE -> this;
         };
   }
}
