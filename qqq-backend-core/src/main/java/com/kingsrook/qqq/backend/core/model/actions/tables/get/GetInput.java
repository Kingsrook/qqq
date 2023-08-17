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

package com.kingsrook.qqq.backend.core.model.actions.tables.get;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrGetInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;


/*******************************************************************************
 ** Input data for the Get action
 **
 *******************************************************************************/
public class GetInput extends AbstractTableActionInput implements QueryOrGetInputInterface
{
   private QBackendTransaction transaction;

   private Serializable              primaryKey;
   private Map<String, Serializable> uniqueKey;

   private boolean shouldTranslatePossibleValues = false;
   private boolean shouldGenerateDisplayValues   = false;
   private boolean shouldFetchHeavyFields        = true;
   private boolean shouldOmitHiddenFields        = true;
   private boolean shouldMaskPasswords           = true;

   private List<QueryJoin> queryJoins = null;

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // if you say you want to includeAssociations, you can limit which ones by passing them in associationNamesToInclude. //
   // if you leave it null, you get all associations defined on the table.  if you pass it as empty, you get none.       //
   // to go to a recursive level of associations, you need to dot-qualify the names.  e.g., A, B, A.C, A.D, A.C.E        //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private boolean            includeAssociations       = false;
   private Collection<String> associationNamesToInclude = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetInput()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public GetInput(String tableName)
   {
      setTableName(tableName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public AbstractTableActionInput withTableName(String tableName)
   {
      super.withTableName(tableName);
      return (this);
   }



   /*******************************************************************************
    ** Getter for primaryKey
    **
    *******************************************************************************/
   public Serializable getPrimaryKey()
   {
      return primaryKey;
   }



   /*******************************************************************************
    ** Setter for primaryKey
    **
    *******************************************************************************/
   public void setPrimaryKey(Serializable primaryKey)
   {
      this.primaryKey = primaryKey;
   }



   /*******************************************************************************
    ** Fluent setter for primaryKey
    **
    *******************************************************************************/
   public GetInput withPrimaryKey(Serializable primaryKey)
   {
      this.primaryKey = primaryKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for uniqueKey
    **
    *******************************************************************************/
   public Map<String, Serializable> getUniqueKey()
   {
      return uniqueKey;
   }



   /*******************************************************************************
    ** Setter for uniqueKey
    **
    *******************************************************************************/
   public void setUniqueKey(Map<String, Serializable> uniqueKey)
   {
      this.uniqueKey = uniqueKey;
   }



   /*******************************************************************************
    ** Fluent setter for uniqueKey
    **
    *******************************************************************************/
   public GetInput withUniqueKey(Map<String, Serializable> uniqueKey)
   {
      this.uniqueKey = uniqueKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shouldTranslatePossibleValues
    **
    *******************************************************************************/
   public boolean getShouldTranslatePossibleValues()
   {
      return shouldTranslatePossibleValues;
   }



   /*******************************************************************************
    ** Setter for shouldTranslatePossibleValues
    **
    *******************************************************************************/
   public void setShouldTranslatePossibleValues(boolean shouldTranslatePossibleValues)
   {
      this.shouldTranslatePossibleValues = shouldTranslatePossibleValues;
   }



   /*******************************************************************************
    ** Getter for shouldGenerateDisplayValues
    **
    *******************************************************************************/
   public boolean getShouldGenerateDisplayValues()
   {
      return shouldGenerateDisplayValues;
   }



   /*******************************************************************************
    ** Setter for shouldGenerateDisplayValues
    **
    *******************************************************************************/
   public void setShouldGenerateDisplayValues(boolean shouldGenerateDisplayValues)
   {
      this.shouldGenerateDisplayValues = shouldGenerateDisplayValues;
   }



   /*******************************************************************************
    ** Getter for transaction
    **
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return transaction;
   }



   /*******************************************************************************
    ** Setter for transaction
    **
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Fluent setter for transaction
    **
    *******************************************************************************/
   public GetInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shouldFetchHeavyFields
    *******************************************************************************/
   public boolean getShouldFetchHeavyFields()
   {
      return (this.shouldFetchHeavyFields);
   }



   /*******************************************************************************
    ** Setter for shouldFetchHeavyFields
    *******************************************************************************/
   public void setShouldFetchHeavyFields(boolean shouldFetchHeavyFields)
   {
      this.shouldFetchHeavyFields = shouldFetchHeavyFields;
   }



   /*******************************************************************************
    ** Fluent setter for shouldFetchHeavyFields
    *******************************************************************************/
   public GetInput withShouldFetchHeavyFields(boolean shouldFetchHeavyFields)
   {
      this.shouldFetchHeavyFields = shouldFetchHeavyFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for includeAssociations
    *******************************************************************************/
   public boolean getIncludeAssociations()
   {
      return (this.includeAssociations);
   }



   /*******************************************************************************
    ** Setter for includeAssociations
    *******************************************************************************/
   public void setIncludeAssociations(boolean includeAssociations)
   {
      this.includeAssociations = includeAssociations;
   }



   /*******************************************************************************
    ** Fluent setter for includeAssociations
    *******************************************************************************/
   public GetInput withIncludeAssociations(boolean includeAssociations)
   {
      this.includeAssociations = includeAssociations;
      return (this);
   }



   /*******************************************************************************
    ** Getter for associationNamesToInclude
    *******************************************************************************/
   public Collection<String> getAssociationNamesToInclude()
   {
      return (this.associationNamesToInclude);
   }



   /*******************************************************************************
    ** Setter for associationNamesToInclude
    *******************************************************************************/
   public void setAssociationNamesToInclude(Collection<String> associationNamesToInclude)
   {
      this.associationNamesToInclude = associationNamesToInclude;
   }



   /*******************************************************************************
    ** Fluent setter for associationNamesToInclude
    *******************************************************************************/
   public GetInput withAssociationNamesToInclude(Collection<String> associationNamesToInclude)
   {
      this.associationNamesToInclude = associationNamesToInclude;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for shouldTranslatePossibleValues
    *******************************************************************************/
   public GetInput withShouldTranslatePossibleValues(boolean shouldTranslatePossibleValues)
   {
      this.shouldTranslatePossibleValues = shouldTranslatePossibleValues;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for shouldGenerateDisplayValues
    *******************************************************************************/
   public GetInput withShouldGenerateDisplayValues(boolean shouldGenerateDisplayValues)
   {
      this.shouldGenerateDisplayValues = shouldGenerateDisplayValues;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shouldMaskPasswords
    *******************************************************************************/
   public boolean getShouldMaskPasswords()
   {
      return (this.shouldMaskPasswords);
   }



   /*******************************************************************************
    ** Setter for shouldMaskPasswords
    *******************************************************************************/
   public void setShouldMaskPasswords(boolean shouldMaskPasswords)
   {
      this.shouldMaskPasswords = shouldMaskPasswords;
   }



   /*******************************************************************************
    ** Fluent setter for shouldMaskPasswords
    *******************************************************************************/
   public GetInput withShouldMaskPasswords(boolean shouldMaskPasswords)
   {
      this.shouldMaskPasswords = shouldMaskPasswords;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shouldOmitHiddenFields
    *******************************************************************************/
   public boolean getShouldOmitHiddenFields()
   {
      return (this.shouldOmitHiddenFields);
   }



   /*******************************************************************************
    ** Setter for shouldOmitHiddenFields
    *******************************************************************************/
   public void setShouldOmitHiddenFields(boolean shouldOmitHiddenFields)
   {
      this.shouldOmitHiddenFields = shouldOmitHiddenFields;
   }



   /*******************************************************************************
    ** Fluent setter for shouldOmitHiddenFields
    *******************************************************************************/
   public GetInput withShouldOmitHiddenFields(boolean shouldOmitHiddenFields)
   {
      this.shouldOmitHiddenFields = shouldOmitHiddenFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryJoins
    *******************************************************************************/
   public List<QueryJoin> getQueryJoins()
   {
      return (this.queryJoins);
   }



   /*******************************************************************************
    ** Setter for queryJoins
    *******************************************************************************/
   public void setQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    *******************************************************************************/
   public GetInput withQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    **
    *******************************************************************************/
   public GetInput withQueryJoin(QueryJoin queryJoin)
   {
      if(this.queryJoins == null)
      {
         this.queryJoins = new ArrayList<>();
      }
      this.queryJoins.add(queryJoin);
      return (this);
   }

}
