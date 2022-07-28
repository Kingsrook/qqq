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

package com.kingsrook.qqq.backend.module.rdbms.jdbc;


import java.util.HashMap;


/*******************************************************************************
 **
 *******************************************************************************/
public class SimpleEntity extends HashMap<String, Object>
{
   // private String tableName;



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public SimpleEntity()
   // {
   //    super();
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public SimpleEntity with(String key, Object value)
   // {
   //    put(key, value);
   //    return (this);
   // }



   // /*******************************************************************************
   //  ** Return the current value of tableName
   //  **
   //  ** @return tableName
   //  *******************************************************************************/
   // public String getTableName()
   // {
   //    return (tableName);
   // }



   // /*******************************************************************************
   //  ** Set the current value of tableName
   //  **
   //  ** @param tableName
   //  *******************************************************************************/
   // public void setTableName(String tableName)
   // {
   //    this.tableName = tableName;
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public SimpleEntity withTableName(String tableName)
   // {
   //    setTableName(tableName);
   //    return (this);
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public Boolean getBoolean(String columnName)
   // {
   //    Object o = get(columnName);
   //    if(o == null)
   //    {
   //       return (null);
   //    }

   //    if(o instanceof Boolean)
   //    {
   //       return ((Boolean) o);
   //    }
   //    else if(o instanceof Number)
   //    {
   //       int i = ((Number) o).intValue();
   //       return (i != 0);
   //    }
   //    else if(o instanceof String)
   //    {
   //       String s = (String) o;
   //       return (s.equalsIgnoreCase("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t"));
   //    }
   //    else
   //    {
   //       throw new IllegalArgumentException("Could not get value of object of type [" + o.getClass() + "] as Boolean.");
   //    }

   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public String getString(String columnName)
   // {
   //    Object o = get(columnName);
   //    if(o == null)
   //    {
   //       return (null);
   //    }
   //    if(o instanceof String)
   //    {
   //       return ((String) o);
   //    }
   //    else if(o instanceof byte[])
   //    {
   //       return (new String((byte[]) o));
   //    }

   //    return String.valueOf(o);
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public Integer getInteger(String columnName)
   // {
   //    Object o = get(columnName);
   //    if(o instanceof Long)
   //    {
   //       return ((Long) o).intValue();
   //    }
   //    else if(o instanceof Short)
   //    {
   //       return ((Short) o).intValue();
   //    }
   //    else if(o instanceof String)
   //    {
   //       return (Integer.parseInt((String) o));
   //    }

   //    return ((Integer) o);
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public BigDecimal getBigDecimal(String columnName)
   // {
   //    Object o = get(columnName);
   //    if(o == null)
   //    {
   //       return (null);
   //    }

   //    if(o instanceof BigDecimal)
   //    {
   //       return ((BigDecimal) o);
   //    }
   //    else
   //    {
   //       return new BigDecimal(String.valueOf(o));
   //    }
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public Long getLong(String columnName)
   // {
   //    Object o = get(columnName);
   //    if(o instanceof Integer)
   //    {
   //       return ((Integer) o).longValue();
   //    }

   //    return ((Long) o);
   // }



   // /*******************************************************************************
   //  **
   //  *******************************************************************************/
   // public void trimStrings()
   // {
   //    for(String key : keySet())
   //    {
   //       Object value = get(key);
   //       if(value != null && value instanceof String)
   //       {
   //          put(key, ((String) value).trim());
   //       }
   //    }
   // }
}
