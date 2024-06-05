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
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Object to represent the graph of joins in a QQQ Instance.  e.g., all of the
 ** connections among tables through joins.
 *******************************************************************************/
public class JoinGraph
{
   private Set<Edge> edges = new HashSet<>();

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // as an instance grows, with the number of joins (say, more than 50?), especially as they may have a lot of connections, //
   // it can become very very slow to process a full join graph (e.g., 10 seconds, maybe much worse, per Big-O...)           //
   // also, it's not frequently useful to look at a join path that's more than a handful of tables long.                     //
   // thus - this property exists - to limit the max length of a join path.  Keeping it small keeps instance enrichment      //
   // and validation reasonably performant, at the possible cost of, some join-path that's longer than this limit may not    //
   // be found - but - chances are, you don't want some 12-element join path to be used anyway, thus, this makes sense.      //
   // but - it can be adjusted, per system property or ENV var.                                                              //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private int maxPathLength = new QMetaDataVariableInterpreter().getIntegerFromPropertyOrEnvironment("qqq.instance.joinGraph.maxPathLength", "QQQ_INSTANCE_JOIN_GRAPH_MAX_PATH_LENGTH", 3);



   /*******************************************************************************
    ** Graph edge (no graph nodes needed in here)
    *******************************************************************************/
   private record Edge(String joinName, String leftTable, String rightTable)
   {
   }



   /*******************************************************************************
    ** In this class, we are treating joins as non-directional graph edges - so -
    ** use this class to "normalize" what may otherwise be duplicated joins in the
    ** qInstance (e.g., A -> B and B -> A -- in the instance, those are valid, but
    ** in our graph here, we want to consider those the same).
    *******************************************************************************/
   private static class NormalizedJoin
   {
      private String tableA;
      private String tableB;
      private String joinFieldA;
      private String joinFieldB;



      /*******************************************************************************
       **
       *******************************************************************************/
      public NormalizedJoin(QJoinMetaData joinMetaData)
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
         NormalizedJoin that = (NormalizedJoin) o;
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



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public JoinGraph(QInstance qInstance)
   {
      Set<NormalizedJoin> usedJoins = new HashSet<>();
      for(QJoinMetaData join : CollectionUtils.nonNullMap(qInstance.getJoins()).values())
      {
         NormalizedJoin normalizedJoin = new NormalizedJoin(join);
         if(usedJoins.contains(normalizedJoin))
         {
            continue;
         }

         usedJoins.add(normalizedJoin);
         edges.add(new Edge(join.getName(), join.getLeftTable(), join.getRightTable()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
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



      /*******************************************************************************
       **
       *******************************************************************************/
      public boolean matchesJoinPath(List<String> joinPath)
      {
         if(list.size() != joinPath.size())
         {
            return (false);
         }

         for(int i = 0; i < list.size(); i++)
         {
            if(!list.get(i).viaJoinName().equals(joinPath.get(i)))
            {
               return (false);
            }
         }

         return (true);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public String getJoinNamesAsString()
      {
         return (StringUtils.join(", ", list().stream().map(jc -> jc.viaJoinName()).toList()));
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public List<String> getJoinNamesAsList()
      {
         return (list().stream().map(jc -> jc.viaJoinName()).toList());
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
                  if(newPath.size() > maxPathLength)
                  {
                     ////////////////////////////////////////////////////////////////
                     // performance hack.  see comment at maxPathLength definition //
                     ////////////////////////////////////////////////////////////////
                     continue;
                  }

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

}
