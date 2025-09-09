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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ExposedJoin implements Cloneable
{
   private static final QLogger LOG = QLogger.getLogger(ExposedJoin.class);

   private String       label;
   private String       joinTable;
   private List<String> joinPath;

   //////////////////////////////////////////////////////////////////
   // no setter for this - derive it the first time it's requested //
   //////////////////////////////////////////////////////////////////
   private Boolean isMany = null;



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @JsonIgnore
   public boolean getIsMany()
   {
      return (getIsMany(QContext.getQInstance()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @JsonIgnore
   public Boolean getIsMany(QInstance qInstance)
   {
      if(isMany == null)
      {
         if(CollectionUtils.nullSafeHasContents(joinPath))
         {
            try
            {
               //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // loop backward through the joinPath, starting at the join table (since we don't know the table that this exposedJoin is attached to!) //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               String currentTable = joinTable;
               for(int i = joinPath.size() - 1; i >= 0; i--)
               {
                  String        joinName = joinPath.get(i);
                  QJoinMetaData join     = qInstance.getJoin(joinName);
                  if(join.getRightTable().equals(currentTable))
                  {
                     currentTable = join.getLeftTable();
                     if(join.getType().equals(JoinType.ONE_TO_MANY) || join.getType().equals(JoinType.MANY_TO_MANY))
                     {
                        isMany = true;
                        break;
                     }
                  }
                  else if(join.getLeftTable().equals(currentTable))
                  {
                     currentTable = join.getRightTable();
                     if(join.getType().equals(JoinType.MANY_TO_ONE) || join.getType().equals(JoinType.MANY_TO_MANY))
                     {
                        isMany = true;
                        break;
                     }
                  }
                  else
                  {
                     throw (new IllegalStateException("Current join table [" + currentTable + "] in path traversal was not found at element [" + joinName + "]"));
                  }
               }

               ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // if we successfully got through the loop, and never found a reason to mark this join as "many", then it must not be, so set isMany to false //
               ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               if(isMany == null)
               {
                  isMany = false;
               }
            }
            catch(Exception e)
            {
               LOG.warn("Error deriving if ExposedJoin through [" + joinPath + "] to [" + joinTable + "] isMany", e);
            }
         }
      }

      return (isMany);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public ExposedJoin withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinTable
    *******************************************************************************/
   public String getJoinTable()
   {
      return (this.joinTable);
   }



   /*******************************************************************************
    ** Setter for joinTable
    *******************************************************************************/
   public void setJoinTable(String joinTable)
   {
      this.joinTable = joinTable;
   }



   /*******************************************************************************
    ** Fluent setter for joinTable
    *******************************************************************************/
   public ExposedJoin withJoinTable(String joinTable)
   {
      this.joinTable = joinTable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinPath
    *******************************************************************************/
   public List<String> getJoinPath()
   {
      return (this.joinPath);
   }



   /*******************************************************************************
    ** Setter for joinPath
    *******************************************************************************/
   public void setJoinPath(List<String> joinPath)
   {
      this.joinPath = joinPath;
   }



   /*******************************************************************************
    ** Fluent setter for joinPath
    *******************************************************************************/
   public ExposedJoin withJoinPath(List<String> joinPath)
   {
      this.joinPath = joinPath;
      return (this);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public ExposedJoin clone()
   {
      try
      {
         ExposedJoin clone = (ExposedJoin) super.clone();

         if(joinPath != null)
         {
            clone.joinPath = new ArrayList<>(joinPath);
         }

         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }
}
