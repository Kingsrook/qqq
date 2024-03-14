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

package com.kingsrook.qqq.backend.core.model.actions.tables;


import java.util.Collection;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;


/*******************************************************************************
 ** Common getters & setters, shared by both QueryInput and GetInput.
 **
 ** Original impetus for this class is the setCommonParamsFrom() method - for cases
 ** where we need to change a Query to a Get, or vice-versa, and we want to copy over
 ** all of those input params.
 *******************************************************************************/
public interface QueryOrGetInputInterface
{
   /*******************************************************************************
    ** Set in THIS, the "common params" (e.g., common to both Query & Get inputs)
    ** from the parameter SOURCE object.
    *******************************************************************************/
   default void setCommonParamsFrom(QueryOrGetInputInterface source)
   {
      this.setTransaction(source.getTransaction());
      this.setShouldTranslatePossibleValues(source.getShouldTranslatePossibleValues());
      this.setShouldGenerateDisplayValues(source.getShouldGenerateDisplayValues());
      this.setShouldFetchHeavyFields(source.getShouldFetchHeavyFields());
      this.setShouldOmitHiddenFields(source.getShouldOmitHiddenFields());
      this.setShouldMaskPasswords(source.getShouldMaskPasswords());
      this.setIncludeAssociations(source.getIncludeAssociations());
      this.setAssociationNamesToInclude(source.getAssociationNamesToInclude());
      this.setQueryJoins(source.getQueryJoins());
   }

   /*******************************************************************************
    ** Getter for transaction
    *******************************************************************************/
   QBackendTransaction getTransaction();


   /*******************************************************************************
    **
    *******************************************************************************/
   String getTableName();

   /*******************************************************************************
    ** Setter for transaction
    *******************************************************************************/
   void setTransaction(QBackendTransaction transaction);


   /*******************************************************************************
    ** Getter for shouldTranslatePossibleValues
    *******************************************************************************/
   boolean getShouldTranslatePossibleValues();


   /*******************************************************************************
    ** Setter for shouldTranslatePossibleValues
    *******************************************************************************/
   void setShouldTranslatePossibleValues(boolean shouldTranslatePossibleValues);


   /*******************************************************************************
    ** Getter for shouldGenerateDisplayValues
    *******************************************************************************/
   boolean getShouldGenerateDisplayValues();


   /*******************************************************************************
    ** Setter for shouldGenerateDisplayValues
    *******************************************************************************/
   void setShouldGenerateDisplayValues(boolean shouldGenerateDisplayValues);


   /*******************************************************************************
    ** Getter for shouldFetchHeavyFields
    *******************************************************************************/
   boolean getShouldFetchHeavyFields();


   /*******************************************************************************
    ** Setter for shouldFetchHeavyFields
    *******************************************************************************/
   void setShouldFetchHeavyFields(boolean shouldFetchHeavyFields);


   /*******************************************************************************
    ** Getter for shouldOmitHiddenFields
    *******************************************************************************/
   boolean getShouldOmitHiddenFields();


   /*******************************************************************************
    ** Setter for shouldOmitHiddenFields
    *******************************************************************************/
   void setShouldOmitHiddenFields(boolean shouldOmitHiddenFields);


   /*******************************************************************************
    ** Getter for shouldMaskPasswords
    *******************************************************************************/
   boolean getShouldMaskPasswords();


   /*******************************************************************************
    ** Setter for shouldMaskPasswords
    *******************************************************************************/
   void setShouldMaskPasswords(boolean shouldMaskPasswords);


   /*******************************************************************************
    ** Getter for includeAssociations
    *******************************************************************************/
   boolean getIncludeAssociations();


   /*******************************************************************************
    ** Setter for includeAssociations
    *******************************************************************************/
   void setIncludeAssociations(boolean includeAssociations);


   /*******************************************************************************
    ** Getter for associationNamesToInclude
    *******************************************************************************/
   Collection<String> getAssociationNamesToInclude();


   /*******************************************************************************
    ** Setter for associationNamesToInclude
    *******************************************************************************/
   void setAssociationNamesToInclude(Collection<String> associationNamesToInclude);


   /*******************************************************************************
    ** Getter for queryJoins
    *******************************************************************************/
   List<QueryJoin> getQueryJoins();


   /*******************************************************************************
    ** Setter for queryJoins
    **
    *******************************************************************************/
   void setQueryJoins(List<QueryJoin> queryJoins);

}
