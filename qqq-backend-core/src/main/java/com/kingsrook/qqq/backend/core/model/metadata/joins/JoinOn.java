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
 **
 *******************************************************************************/
public class JoinOn
{
   private String leftField;
   private String rightField;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public JoinOn()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public JoinOn(String leftField, String rightField)
   {
      this.leftField = leftField;
      this.rightField = rightField;
   }



   /*******************************************************************************
    ** Getter for leftField
    **
    *******************************************************************************/
   public String getLeftField()
   {
      return leftField;
   }



   /*******************************************************************************
    ** Setter for leftField
    **
    *******************************************************************************/
   public void setLeftField(String leftField)
   {
      this.leftField = leftField;
   }



   /*******************************************************************************
    ** Fluent setter for leftField
    **
    *******************************************************************************/
   public JoinOn withLeftField(String leftField)
   {
      this.leftField = leftField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for rightField
    **
    *******************************************************************************/
   public String getRightField()
   {
      return rightField;
   }



   /*******************************************************************************
    ** Setter for rightField
    **
    *******************************************************************************/
   public void setRightField(String rightField)
   {
      this.rightField = rightField;
   }



   /*******************************************************************************
    ** Fluent setter for rightField
    **
    *******************************************************************************/
   public JoinOn withRightField(String rightField)
   {
      this.rightField = rightField;
      return (this);
   }

}
