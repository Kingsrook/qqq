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

package com.kingsrook.qqq.backend.core.utils;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for PrefixedDefaultThreadFactory 
 *******************************************************************************/
class PrefixedDefaultThreadFactoryTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      assertThat(new PrefixedDefaultThreadFactory("ItsMe").newThread(this::noop).getName()).startsWith("ItsMe-pool");
      assertThat(new PrefixedDefaultThreadFactory("ItsMe-").newThread(this::noop).getName()).startsWith("ItsMe-pool");
      assertThat(new PrefixedDefaultThreadFactory("ItsMe--").newThread(this::noop).getName()).startsWith("ItsMe-pool");
      assertThat(new PrefixedDefaultThreadFactory((String) null).newThread(this::noop).getName()).startsWith("pool");
      assertThat(new PrefixedDefaultThreadFactory((Class<?>) null).newThread(this::noop).getName()).startsWith("pool");
      assertThat(new PrefixedDefaultThreadFactory((Object) null).newThread(this::noop).getName()).startsWith("pool");
      assertThat(new PrefixedDefaultThreadFactory("").newThread(this::noop).getName()).startsWith("pool");
      assertThat(new PrefixedDefaultThreadFactory(" ").newThread(this::noop).getName()).startsWith("pool");
      assertThat(new PrefixedDefaultThreadFactory(this).newThread(this::noop).getName()).startsWith(getClass().getSimpleName() + "-pool");
      assertThat(new PrefixedDefaultThreadFactory(InsertAction.class).newThread(this::noop).getName()).startsWith(InsertAction.class.getSimpleName() + "-pool");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void noop()
   {
      System.out.println("noop");
   }

}