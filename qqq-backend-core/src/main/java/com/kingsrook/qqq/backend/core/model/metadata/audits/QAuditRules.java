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

package com.kingsrook.qqq.backend.core.model.metadata.audits;


import java.util.List;


/*******************************************************************************
 **
 *******************************************************************************/
public class QAuditRules
{
   private AuditLevel auditLevel;

   private boolean      isAuditTreeRoot           = false;
   private List<String> auditTreeParentTableNames = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QAuditRules defaultInstanceLevelNone()
   {
      return (new QAuditRules()
         .withAuditLevel(AuditLevel.NONE));
   }



   /*******************************************************************************
    ** Getter for auditLevel
    *******************************************************************************/
   public AuditLevel getAuditLevel()
   {
      return (this.auditLevel);
   }



   /*******************************************************************************
    ** Setter for auditLevel
    *******************************************************************************/
   public void setAuditLevel(AuditLevel auditLevel)
   {
      this.auditLevel = auditLevel;
   }



   /*******************************************************************************
    ** Fluent setter for auditLevel
    *******************************************************************************/
   public QAuditRules withAuditLevel(AuditLevel auditLevel)
   {
      this.auditLevel = auditLevel;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isAuditTreeRoot
    *******************************************************************************/
   public boolean getIsAuditTreeRoot()
   {
      return (this.isAuditTreeRoot);
   }



   /*******************************************************************************
    ** Setter for isAuditTreeRoot
    *******************************************************************************/
   public void setIsAuditTreeRoot(boolean isAuditTreeRoot)
   {
      this.isAuditTreeRoot = isAuditTreeRoot;
   }



   /*******************************************************************************
    ** Fluent setter for isAuditTreeRoot
    *******************************************************************************/
   public QAuditRules withIsAuditTreeRoot(boolean isAuditTreeRoot)
   {
      this.isAuditTreeRoot = isAuditTreeRoot;
      return (this);
   }



   /*******************************************************************************
    ** Getter for auditTreeParentTableNames
    *******************************************************************************/
   public List<String> getAuditTreeParentTableNames()
   {
      return (this.auditTreeParentTableNames);
   }



   /*******************************************************************************
    ** Setter for auditTreeParentTableNames
    *******************************************************************************/
   public void setAuditTreeParentTableNames(List<String> auditTreeParentTableNames)
   {
      this.auditTreeParentTableNames = auditTreeParentTableNames;
   }



   /*******************************************************************************
    ** Fluent setter for auditTreeParentTableNames
    *******************************************************************************/
   public QAuditRules withAuditTreeParentTableNames(List<String> auditTreeParentTableNames)
   {
      this.auditTreeParentTableNames = auditTreeParentTableNames;
      return (this);
   }

}
