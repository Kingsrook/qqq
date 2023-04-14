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

package com.kingsrook.qqq.backend.core.actions.metadata;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class JoinGraph
{
   private record Node(String tableName)
   {
   }



   private record Edge(String joinName, String leftTable, String rightTable)
   {
   }



   private static class CanonicalJoin
   {
      private String tableA;
      private String tableB;
      private String joinFieldA;
      private String joinFieldB;



      /*******************************************************************************
       **
       *******************************************************************************/
      public CanonicalJoin(QJoinMetaData joinMetaData)
      {
         boolean needFlip     = false;
         int     tableCompare = joinMetaData.getLeftTable().compareTo(joinMetaData.getRightTable());
         if(tableCompare < 0)
         {
            needFlip = true;
         }
         else if(tableCompare == 0)
         {
            int fieldCompare = joinMetaData.getJoinOns().get(0).getLeftField().compareTo(joinMetaData.getJoinOns().get(0).getRightField());
            if(fieldCompare < 0)
            {
               needFlip = true;
            }
         }

         if(needFlip)
         {
            joinMetaData = joinMetaData.flip();
         }

         tableA = joinMetaData.getLeftTable();
         tableB = joinMetaData.getRightTable();
         joinFieldA = joinMetaData.getJoinOns().get(0).getLeftField();
         joinFieldB = joinMetaData.getJoinOns().get(0).getRightField();
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public boolean equals(Object o)
      {
         if(this == o)
         {
            return true;
         }
         if(o == null || getClass() != o.getClass())
         {
            return false;
         }
         CanonicalJoin that = (CanonicalJoin) o;
         return Objects.equals(tableA, that.tableA) && Objects.equals(tableB, that.tableB) && Objects.equals(joinFieldA, that.joinFieldA) && Objects.equals(joinFieldB, that.joinFieldB);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public int hashCode()
      {
         return Objects.hash(tableA, tableB, joinFieldA, joinFieldB);
      }
   }



   private Set<Node> nodes = new HashSet<>();
   private Set<Edge> edges = new HashSet<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public JoinGraph(QInstance qInstance)
   {
      Set<CanonicalJoin> usedJoins = new HashSet<>();
      for(QJoinMetaData join : qInstance.getJoins().values())
      {
         CanonicalJoin canonicalJoin = new CanonicalJoin(join);
         if(usedJoins.contains(canonicalJoin))
         {
            continue;
         }

         usedJoins.add(canonicalJoin);
         nodes.add(new Node(join.getLeftTable()));
         nodes.add(new Node(join.getRightTable()));
         edges.add(new Edge(join.getName(), join.getLeftTable(), join.getRightTable()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getJoins(String tableName)
   {
      Set<String> rs     = new HashSet<>();
      Set<String> tables = new HashSet<>();
      tables.add(tableName);

      boolean keepGoing = true;
      while(keepGoing)
      {
         keepGoing = false;
         for(Edge edge : edges)
         {
            if(tables.contains(edge.leftTable) || tables.contains(edge.rightTable))
            {
               if(!rs.contains(edge.joinName))
               {
                  tables.add(edge.leftTable);
                  tables.add(edge.rightTable);
                  rs.add(edge.joinName);
                  keepGoing = true;
               }
            }
         }
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public record Something(String joinTable, List<String> joinPath)
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<Something> getJoinsBetter(String tableName)
   {
      Set<Something> rs        = new HashSet<>();
      Set<String>    usedEdges = new HashSet<>();
      Set<String>    tables    = new HashSet<>();
      tables.add(tableName);
      doGetJoinsBetter(rs, tables, new ArrayList<>(), usedEdges);

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doGetJoinsBetter(Set<Something> rs, Set<String> tables, List<String> joinPath, Set<String> usedEdges)
   {
      for(Edge edge : edges)
      {
         if(usedEdges.contains(edge.joinName))
         {
            continue;
         }

         if(tables.contains(edge.leftTable) || tables.contains(edge.rightTable))
         {
            usedEdges.add(edge.joinName);
            // todo - clone list here, then recurisiv call
            rs.add(new Something(tables.contains(edge.leftTable) ? edge.rightTable : edge.leftTable, joinPath));
         }
      }
   }

}
