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


import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractWidgetOutput
{
   protected QFilterCriteria condition;
   protected String          type;



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract String render(Map<String, Object> context) throws QException;



   /*******************************************************************************
    ** Getter for condition
    *******************************************************************************/
   public QFilterCriteria getCondition()
   {
      return (this.condition);
   }



   /*******************************************************************************
    ** Setter for condition
    *******************************************************************************/
   public void setCondition(QFilterCriteria condition)
   {
      this.condition = condition;
   }



   /*******************************************************************************
    ** Fluent setter for condition
    *******************************************************************************/
   public AbstractWidgetOutput withCondition(QFilterCriteria condition)
   {
      this.condition = condition;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   public String getType()
   {
      return (this.type);
   }



   /*******************************************************************************
    ** Setter for type
    *******************************************************************************/
   public void setType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    *******************************************************************************/
   public AbstractWidgetOutput withType(String type)
   {
      this.type = type;
      return (this);
   }

}
