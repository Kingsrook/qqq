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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
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



   public record JoinConnection(String joinTable, String viaJoinName) implements Comparable<JoinConnection>
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public int compareTo(JoinConnection that)
      {
         Comparator<JoinConnection> comparator = Comparator.comparing((JoinConnection jc) -> jc.joinTable())
            .thenComparing((JoinConnection jc) -> jc.viaJoinName());
         return (comparator.compare(this, that));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public record JoinConnectionList(List<JoinConnection> list) implements Comparable<JoinConnectionList>
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      public JoinConnectionList copy()
      {
         return new JoinConnectionList(new ArrayList<>(list));
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public int compareTo(JoinConnectionList that)
      {
         if(this.equals(that))
         {
            return (0);
         }

         for(int i = 0; i < Math.min(this.list.size(), that.list.size()); i++)
         {
            int comp = this.list.get(i).compareTo(that.list.get(i));
            if(comp != 0)
            {
               return (comp);
            }
         }

         return (this.list.size() - that.list.size());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<JoinConnectionList> getJoinConnections(String tableName)
   {
      Set<JoinConnectionList> rs = new TreeSet<>();
      doGetJoinConnections(rs, tableName, new ArrayList<>(), new JoinConnectionList(new ArrayList<>()));
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doGetJoinConnections(Set<JoinConnectionList> joinConnections, String tableName, List<String> path, JoinConnectionList connectionList)
   {
      for(Edge edge : edges)
      {
         if(edge.leftTable.equals(tableName) || edge.rightTable.equals(tableName))
         {
            if(path.contains(edge.joinName))
            {
               continue;
            }

            List<String> newPath = new ArrayList<>(path);
            newPath.add(edge.joinName);
            if(!joinConnectionsContain(joinConnections, newPath))
            {
               String otherTableName = null;
               if(!edge.leftTable.equals(tableName))
               {
                  otherTableName = edge.leftTable;
               }
               else if(!edge.rightTable.equals(tableName))
               {
                  otherTableName = edge.rightTable;
               }

               if(otherTableName != null)
               {

                  JoinConnectionList newConnectionList = connectionList.copy();
                  JoinConnection     joinConnection    = new JoinConnection(otherTableName, edge.joinName);
                  newConnectionList.list.add(joinConnection);
                  joinConnections.add(newConnectionList);
                  doGetJoinConnections(joinConnections, otherTableName, new ArrayList<>(newPath), newConnectionList);
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean joinConnectionsContain(Set<JoinConnectionList> joinPaths, List<String> newPath)
   {
      for(JoinConnectionList joinConnections : joinPaths)
      {
         List<String> joinConnectionJoins = joinConnections.list.stream().map(jc -> jc.viaJoinName).toList();
         if(joinConnectionJoins.equals(newPath))
         {
            return (true);
         }
      }
      return (false);
   }



   public record JoinPath(String joinTable, List<String> joinNames)
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<JoinPath> getJoinPaths(String tableName)
   {
      Set<JoinPath> rs = new HashSet<>();
      doGetJoinPaths(rs, tableName, new ArrayList<>());
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doGetJoinPaths(Set<JoinPath> joinPaths, String tableName, List<String> path)
   {
      for(Edge edge : edges)
      {
         if(edge.leftTable.equals(tableName) || edge.rightTable.equals(tableName))
         {
            if(path.contains(edge.joinName))
            {
               continue;
            }

            List<String> newPath = new ArrayList<>(path);
            newPath.add(edge.joinName);
            if(!joinPathsContain(joinPaths, newPath))
            {
               String otherTableName = null;
               if(!edge.leftTable.equals(tableName))
               {
                  otherTableName = edge.leftTable;
               }
               else if(!edge.rightTable.equals(tableName))
               {
                  otherTableName = edge.rightTable;
               }

               if(otherTableName != null)
               {
                  joinPaths.add(new JoinPath(otherTableName, newPath));
                  doGetJoinPaths(joinPaths, otherTableName, new ArrayList<>(newPath));
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean joinPathsContain(Set<JoinPath> joinPaths, List<String> newPath)
   {
      for(JoinPath joinPath : joinPaths)
      {
         if(joinPath.joinNames().equals(newPath))
         {
            return (true);
         }
      }
      return (false);
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
   public Set<List<String>> getJoinPaths(String tableName)
   {
      Set<List<String>> rs = new HashSet<>();
      doGetJoinPaths(rs, tableName, new ArrayList<>());
      return (rs);
   }
   */

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
   private void doGetJoinPaths(Set<List<String>> joinPaths, String tableName, List<String> path)
   {
      for(Edge edge : edges)
      {
         if(edge.leftTable.equals(tableName) || edge.rightTable.equals(tableName))
         {
            if(path.contains(edge.joinName))
            {
               continue;
            }

            List<String> newPath = new ArrayList<>(path);
            newPath.add(edge.joinName);
            if(!joinPaths.contains(newPath))
            {
               joinPaths.add(newPath);

               String otherTableName = null;
               if(!edge.leftTable.equals(tableName))
               {
                  otherTableName = edge.leftTable;
               }
               else if(!edge.rightTable.equals(tableName))
               {
                  otherTableName = edge.rightTable;
               }

               if(otherTableName != null)
               {
                  doGetJoinPaths(joinPaths, otherTableName, new ArrayList<>(newPath));
               }
            }
         }
      }
   }
   */



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
