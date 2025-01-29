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

package com.kingsrook.qqq.backend.module.rdbms.model.metadata;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QSupplementalFieldMetaData;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendModule;
import com.kingsrook.qqq.backend.module.rdbms.strategy.RDBMSActionStrategyInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSFieldMetaData extends QSupplementalFieldMetaData
{
   private QCodeReference actionStrategyCodeReference;
   private RDBMSActionStrategyInterface actionStrategy;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public RDBMSFieldMetaData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static RDBMSFieldMetaData of(QFieldMetaData field)
   {
      return ((RDBMSFieldMetaData) field.getSupplementalMetaData(RDBMSBackendModule.NAME));
   }



   /*******************************************************************************
    ** either get the object attached to a field - or create a new one and attach
    ** it to the field, and return that.
    *******************************************************************************/
   public static RDBMSFieldMetaData ofOrWithNew(QFieldMetaData field)
   {
      RDBMSFieldMetaData rdbmsFieldMetaData = of(field);
      if(rdbmsFieldMetaData == null)
      {
         rdbmsFieldMetaData = new RDBMSFieldMetaData();
         field.withSupplementalMetaData(rdbmsFieldMetaData);
      }
      return (rdbmsFieldMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getType()
   {
      return (RDBMSBackendModule.NAME);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @JsonIgnore
   public RDBMSActionStrategyInterface getActionStrategy()
   {
      if(actionStrategy == null)
      {
         if(actionStrategyCodeReference != null)
         {
            actionStrategy = QCodeLoader.getAdHoc(RDBMSActionStrategyInterface.class, actionStrategyCodeReference);
         }
         else
         {
            return (null);
         }
      }

      return (actionStrategy);
   }



   /*******************************************************************************
    ** Getter for actionStrategyCodeReference
    *******************************************************************************/
   public QCodeReference getActionStrategyCodeReference()
   {
      return (this.actionStrategyCodeReference);
   }



   /*******************************************************************************
    ** Setter for actionStrategyCodeReference
    *******************************************************************************/
   public void setActionStrategyCodeReference(QCodeReference actionStrategyCodeReference)
   {
      this.actionStrategyCodeReference = actionStrategyCodeReference;
   }



   /*******************************************************************************
    ** Fluent setter for actionStrategyCodeReference
    *******************************************************************************/
   public RDBMSFieldMetaData withActionStrategyCodeReference(QCodeReference actionStrategyCodeReference)
   {
      this.actionStrategyCodeReference = actionStrategyCodeReference;
      return (this);
   }



   /***************************************************************************
    * note - protected - meant for sub-classes to use in their implementation of
    * getActionStrategy, but not for public use.
    ***************************************************************************/
   protected RDBMSActionStrategyInterface getActionStrategyField()
   {
      return (actionStrategy);
   }



   /***************************************************************************
    * note - protected - meant for sub-classes to use in their implementation of
    * getActionStrategy, but not for public use.
    ***************************************************************************/
   protected void setActionStrategyField(RDBMSActionStrategyInterface actionStrategy)
   {
      this.actionStrategy = actionStrategy;
   }

}
