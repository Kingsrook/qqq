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

package com.kingsrook.qqq.backend.core.actions.dashboard;


import java.util.Collections;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.DenyBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for AbstractHTMLWidgetRenderer 
 *******************************************************************************/
class AbstractHTMLWidgetRendererTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      String link = AbstractHTMLWidgetRenderer.getCountLink(null, TestUtils.TABLE_NAME_PERSON, new QQueryFilter()
         .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1))
         .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1)), 2
      );

      ////////////////////////////////////////////////////
      // assert that filter de-duplication is occurring //
      ////////////////////////////////////////////////////
      assertThat(link).doesNotMatch(".*EQUALS.*EQUALS.*");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPermissionDeniedMakesNullLinks() throws QException
   {
      ///////////////////////////////////
      // put permissions on the tables //
      ///////////////////////////////////
      QInstance qInstance = TestUtils.defineInstance();
      for(QTableMetaData table : qInstance.getTables().values())
      {
         table.withPermissionRules(new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION).withDenyBehavior(DenyBehavior.HIDDEN));
      }
      QContext.setQInstance(qInstance);

      RenderWidgetInput input = new RenderWidgetInput();
      String tableName = TestUtils.TABLE_NAME_PERSON;

      ////////////////////////////////////////
      // with permissions, should get paths //
      ////////////////////////////////////////
      QContext.setQSession(new QSession().withPermission(tableName + ".hasAccess"));
      assertTrue(AbstractHTMLWidgetRenderer.doesHaveTablePermission(tableName));
      assertNotNull(AbstractHTMLWidgetRenderer.linkTableBulkLoadChildren(input, tableName));
      assertNotNull(AbstractHTMLWidgetRenderer.linkTableCreate(input, tableName));
      assertNotNull(AbstractHTMLWidgetRenderer.linkTableCreateWithDefaultValues(input, tableName, Collections.emptyMap()));
      assertThat(AbstractHTMLWidgetRenderer.getCountLink(input, tableName, new QQueryFilter(), 1)).startsWith("<a href");
      assertNotNull(AbstractHTMLWidgetRenderer.linkTableFilterUnencoded(input, tableName, new QQueryFilter()));
      assertNotNull(AbstractHTMLWidgetRenderer.linkTableFilter(tableName, new QQueryFilter()));
      assertThat(AbstractHTMLWidgetRenderer.aHrefTableFilterNoOfRecords(tableName, new QQueryFilter(), 1, "x", "sx")).startsWith("<a href");
      assertThat(AbstractHTMLWidgetRenderer.aHrefTableFilterNoOfRecords(tableName, new QQueryFilter(), 1, "x", "sx", true)).startsWith("<a href");
      assertThat(AbstractHTMLWidgetRenderer.aHrefViewRecord(tableName, 1, "link")).startsWith("<a href");
      assertNotNull(AbstractHTMLWidgetRenderer.linkRecordEdit(tableName, 1));
      assertNotNull(AbstractHTMLWidgetRenderer.linkRecordView(tableName, 1));
      assertNotNull(AbstractHTMLWidgetRenderer.aHrefTableCreateChild(input, tableName, Collections.emptyMap()));
      assertNotNull(AbstractHTMLWidgetRenderer.linkTableCreateChild(input, tableName, Collections.emptyMap()));

      /////////////////////////////////////////////////////////////
      // with no permissions, should get null (and/or non-links) //
      /////////////////////////////////////////////////////////////
      QContext.setQSession(new QSession());
      assertFalse(AbstractHTMLWidgetRenderer.doesHaveTablePermission(tableName));
      assertNull(AbstractHTMLWidgetRenderer.linkTableBulkLoadChildren(input, tableName));
      assertNull(AbstractHTMLWidgetRenderer.linkTableCreate(input, tableName));
      assertNull(AbstractHTMLWidgetRenderer.linkTableCreateWithDefaultValues(input, tableName, Collections.emptyMap()));
      assertThat(AbstractHTMLWidgetRenderer.getCountLink(input, tableName, new QQueryFilter(), 1)).doesNotStartWith("<a href");
      assertNull(AbstractHTMLWidgetRenderer.linkTableFilterUnencoded(input, tableName, new QQueryFilter()));
      assertNull(AbstractHTMLWidgetRenderer.linkTableFilter(tableName, new QQueryFilter()));
      assertThat(AbstractHTMLWidgetRenderer.aHrefTableFilterNoOfRecords(tableName, new QQueryFilter(), 1, "x", "sx")).doesNotStartWith("<a href");
      assertThat(AbstractHTMLWidgetRenderer.aHrefTableFilterNoOfRecords(tableName, new QQueryFilter(), 1, "x", "sx", true)).doesNotStartWith("<a href");
      assertThat(AbstractHTMLWidgetRenderer.aHrefViewRecord(tableName, 1, "link")).doesNotStartWith("<a href");
      assertNull(AbstractHTMLWidgetRenderer.linkRecordEdit(tableName, 1));
      assertNull(AbstractHTMLWidgetRenderer.linkRecordView(tableName, 1));
      assertNull(AbstractHTMLWidgetRenderer.aHrefTableCreateChild(input, tableName, Collections.emptyMap()));
      assertNull(AbstractHTMLWidgetRenderer.linkTableCreateChild(input, tableName, Collections.emptyMap()));
   }
}