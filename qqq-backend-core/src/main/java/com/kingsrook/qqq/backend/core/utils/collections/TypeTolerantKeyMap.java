/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.utils.collections;


import java.io.Serializable;
import java.util.Map;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Version of map where string keys are handled case-insensitively.  e.g.,
 ** map.put("One", 1); map.get("ONE") == 1.
 *******************************************************************************/
public class TypeTolerantKeyMap<V> extends TransformedKeyMap<Serializable, Serializable, V>
{

   /***************************************************************************
    *
    ***************************************************************************/
   public TypeTolerantKeyMap(QFieldType qFieldType)
   {
      super(key -> ValueUtils.getValueAsFieldType(qFieldType, key));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public TypeTolerantKeyMap(QFieldType qFieldType, Supplier<Map<Serializable, V>> supplier)
   {
      super(key -> ValueUtils.getValueAsFieldType(qFieldType, key), supplier);
   }


   /***************************************************************************
    *
    ***************************************************************************/
   public TypeTolerantKeyMap(Class<? extends Serializable> c)
   {
      super(key -> ValueUtils.getValueAsType(c, key));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public TypeTolerantKeyMap(Class<? extends Serializable> c, Supplier<Map<Serializable, V>> supplier)
   {
      super(key -> ValueUtils.getValueAsType(c, key), supplier);
   }

}
