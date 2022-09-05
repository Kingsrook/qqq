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

package com.kingsrook.qqq.backend.core.model.metadata.tables.automation;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** Details about how this table's record automations are set up.
 *******************************************************************************/
public class QTableAutomationDetails
{
   private AutomationStatusTracking    statusTracking;
   private String                      providerName;
   private List<TableAutomationAction> actions;



   /*******************************************************************************
    ** Getter for statusTracking
    **
    *******************************************************************************/
   public AutomationStatusTracking getStatusTracking()
   {
      return statusTracking;
   }



   /*******************************************************************************
    ** Setter for statusTracking
    **
    *******************************************************************************/
   public void setStatusTracking(AutomationStatusTracking statusTracking)
   {
      this.statusTracking = statusTracking;
   }



   /*******************************************************************************
    ** Fluent setter for statusTracking
    **
    *******************************************************************************/
   public QTableAutomationDetails withStatusTracking(AutomationStatusTracking statusTracking)
   {
      this.statusTracking = statusTracking;
      return (this);
   }



   /*******************************************************************************
    ** Getter for providerName
    **
    *******************************************************************************/
   public String getProviderName()
   {
      return providerName;
   }



   /*******************************************************************************
    ** Setter for providerName
    **
    *******************************************************************************/
   public void setProviderName(String providerName)
   {
      this.providerName = providerName;
   }



   /*******************************************************************************
    ** Fluent setter for providerName
    **
    *******************************************************************************/
   public QTableAutomationDetails withProviderName(String providerName)
   {
      this.providerName = providerName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for actions
    **
    *******************************************************************************/
   public List<TableAutomationAction> getActions()
   {
      return actions;
   }



   /*******************************************************************************
    ** Setter for actions
    **
    *******************************************************************************/
   public void setActions(List<TableAutomationAction> actions)
   {
      this.actions = actions;
   }



   /*******************************************************************************
    ** Fluent setter for actions
    **
    *******************************************************************************/
   public QTableAutomationDetails withActions(List<TableAutomationAction> actions)
   {
      this.actions = actions;
      return (this);
   }



   /*******************************************************************************
    ** Fluently add an action to this table's automations.
    *******************************************************************************/
   public QTableAutomationDetails withAction(TableAutomationAction action)
   {
      if(this.actions == null)
      {
         this.actions = new ArrayList<>();
      }
      this.actions.add(action);
      return (this);
   }
}
