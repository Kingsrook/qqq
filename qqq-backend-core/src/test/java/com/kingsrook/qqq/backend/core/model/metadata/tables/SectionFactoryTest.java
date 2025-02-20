/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for SectionFactory 
 *******************************************************************************/
class SectionFactoryTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      QFieldSection t1section = SectionFactory.defaultT1("id", "name");
      assertEquals(SectionFactory.getDefaultT1name(), t1section.getName());
      assertEquals(SectionFactory.getDefaultT1iconName(), t1section.getIcon().getName());
      assertEquals(Tier.T1, t1section.getTier());
      assertEquals(List.of("id", "name"), t1section.getFieldNames());

      QFieldSection t2section = SectionFactory.defaultT2("size", "age");
      assertEquals(SectionFactory.getDefaultT2name(), t2section.getName());
      assertEquals(SectionFactory.getDefaultT2iconName(), t2section.getIcon().getName());
      assertEquals(Tier.T2, t2section.getTier());
      assertEquals(List.of("size", "age"), t2section.getFieldNames());

      QFieldSection t3section = SectionFactory.defaultT3("createDate", "modifyDate");
      assertEquals(SectionFactory.getDefaultT3name(), t3section.getName());
      assertEquals(SectionFactory.getDefaultT3iconName(), t3section.getIcon().getName());
      assertEquals(Tier.T3, t3section.getTier());
      assertEquals(List.of("createDate", "modifyDate"), t3section.getFieldNames());
   }

}