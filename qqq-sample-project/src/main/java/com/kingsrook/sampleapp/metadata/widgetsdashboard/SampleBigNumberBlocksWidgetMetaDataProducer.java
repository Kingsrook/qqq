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

package com.kingsrook.sampleapp.metadata.widgetsdashboard;


import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.CompositeWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.bignumberblock.BigNumberBlockData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.bignumberblock.BigNumberStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.bignumberblock.BigNumberValues;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.upordownnumber.UpOrDownNumberBlockData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.upordownnumber.UpOrDownNumberSlots;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.upordownnumber.UpOrDownNumberStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.upordownnumber.UpOrDownNumberValues;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardIconRoleNames;


/*******************************************************************************
 ** Meta Data Producer for SampleStatisticsWidget
 *******************************************************************************/
public class SampleBigNumberBlocksWidgetMetaDataProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "SampleBigNumberBlocksWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.COMPOSITE.getType())
         .withGridColumns(12)
         .withIsCard(true)
         .withLabel("Big Number Blocks")
         .withTooltip("This is a sample of a widget using Big Number Blocks")
         .withShowReloadButton(false)
         .withIcon(MaterialDashboardIconRoleNames.TOP_RIGHT_INSIDE_CARD, new QIcon("blocks").withColor("skyblue"))
         .withCodeReference(new QCodeReference(SampleBigNumberBlocksWidgetRenderer.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleBigNumberBlocksWidgetRenderer extends AbstractWidgetRenderer
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public RenderWidgetOutput render(RenderWidgetInput input) throws QException
      {
         CompositeWidgetData data = new CompositeWidgetData();
         data.setLayout(CompositeWidgetData.Layout.FLEX_ROW_WRAPPED);

         data.addBlock(new BigNumberBlockData()
            .withLink("/same-link-for-all-parts")
            .withTooltip("You can have the same tooltip for all parts")
            .withStyles(new BigNumberStyles().withWidth("300px"))
            .withValues(new BigNumberValues().withNumber("123").withHeading("Big Number with Simple Context").withContext("context")));

         data.addBlock(new CompositeWidgetData()
            .withLayout(CompositeWidgetData.Layout.FLEX_ROW_SPACE_BETWEEN)
            .withBlock(new BigNumberBlockData()
               .withLink("/default-link")
               .withTooltip("You can have a default tooltip...")
               .withStyles(new BigNumberStyles().withWidth("300px"))
               .withValues(new BigNumberValues().withNumber("1,234").withHeading("Number with Up/Down Context")))
            .withBlock(new UpOrDownNumberBlockData()
               .withTooltip(UpOrDownNumberSlots.CONTEXT, "You can do a custom tooltip for each slot")
               .withTooltip(UpOrDownNumberSlots.NUMBER, "This number has a customized color")
               .withLink(UpOrDownNumberSlots.NUMBER, "/custom-link-per-slot")
               .withStyles(new UpOrDownNumberStyles().withColorOverride("blue"))
               .withValues(new UpOrDownNumberValues().withIsUp(false).withIsGood(false).withNumber("12,345").withContext("context")))
         );

         data.addBlock(new CompositeWidgetData()
            .withLayout(CompositeWidgetData.Layout.FLEX_ROW_SPACE_BETWEEN)
            .withBlock(new BigNumberBlockData()
               .withStyles(new BigNumberStyles().withWidth("300px"))
               .withValues(new BigNumberValues().withNumber("1,234").withHeading("Number with Stacked Up/Down Context")))
            .withBlock(new UpOrDownNumberBlockData()
               .withStyles(new UpOrDownNumberStyles().withIsStacked(true))
               .withValues(new UpOrDownNumberValues().withIsUp(true).withIsGood(true).withNumber("123").withContext("context")))
         );

         return (new RenderWidgetOutput(data));
      }
   }

}
