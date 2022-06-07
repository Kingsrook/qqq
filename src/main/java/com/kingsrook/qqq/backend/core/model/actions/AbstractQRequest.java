/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Base request class for all Q actions.
 **
 *******************************************************************************/
public abstract class AbstractQRequest
{
   protected QInstance instance;
   protected QSession session;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQRequest(QInstance instance)
   {
      this.instance = instance;

      ////////////////////////////////////////////////////////////
      // if this instance hasn't been validated yet, do so now  //
      // noting that this will also enrich any missing metaData //
      ////////////////////////////////////////////////////////////
      if(!instance.getHasBeenValidated())
      {
         try
         {
            new QInstanceValidator().validate(instance);
         }
         catch(QInstanceValidationException e)
         {
            System.err.println(e.getMessage());
            throw (new IllegalArgumentException("QInstance failed validation" + e.getMessage()));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationMetaData getAuthenticationMetaData()
   {
      return (instance.getAuthentication());
   }



   /*******************************************************************************
    ** Getter for instance
    **
    *******************************************************************************/
   public QInstance getInstance()
   {
      return instance;
   }



   /*******************************************************************************
    ** Setter for instance
    **
    *******************************************************************************/
   public void setInstance(QInstance instance)
   {
      this.instance = instance;
   }



   /*******************************************************************************
    ** Getter for session
    **
    *******************************************************************************/
   public QSession getSession()
   {
      return session;
   }



   /*******************************************************************************
    ** Setter for session
    **
    *******************************************************************************/
   public void setSession(QSession session)
   {
      this.session = session;
   }
}
