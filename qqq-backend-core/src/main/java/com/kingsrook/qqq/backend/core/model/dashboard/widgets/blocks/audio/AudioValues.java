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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.audio;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class AudioValues implements BlockValuesInterface
{
   private String  path;
   private boolean showControls = false;
   private boolean autoPlay     = true;



   /*******************************************************************************
    ** Getter for path
    *******************************************************************************/
   public String getPath()
   {
      return (this.path);
   }



   /*******************************************************************************
    ** Setter for path
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    *******************************************************************************/
   public AudioValues withPath(String path)
   {
      this.path = path;
      return (this);
   }



   /*******************************************************************************
    ** Getter for showControls
    *******************************************************************************/
   public boolean getShowControls()
   {
      return (this.showControls);
   }



   /*******************************************************************************
    ** Setter for showControls
    *******************************************************************************/
   public void setShowControls(boolean showControls)
   {
      this.showControls = showControls;
   }



   /*******************************************************************************
    ** Fluent setter for showControls
    *******************************************************************************/
   public AudioValues withShowControls(boolean showControls)
   {
      this.showControls = showControls;
      return (this);
   }



   /*******************************************************************************
    ** Getter for autoPlay
    *******************************************************************************/
   public boolean getAutoPlay()
   {
      return (this.autoPlay);
   }



   /*******************************************************************************
    ** Setter for autoPlay
    *******************************************************************************/
   public void setAutoPlay(boolean autoPlay)
   {
      this.autoPlay = autoPlay;
   }



   /*******************************************************************************
    ** Fluent setter for autoPlay
    *******************************************************************************/
   public AudioValues withAutoPlay(boolean autoPlay)
   {
      this.autoPlay = autoPlay;
      return (this);
   }

}
