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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard;


import java.util.Collection;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuickSightChartMetaData extends QWidgetMetaData implements QWidgetMetaDataInterface
{
   private String             label;
   private String             accessKey;
   private String             secretKey;
   private String             dashboardId;
   private String             accountId;
   private String             userArn;
   private String             region;
   private Collection<String> allowedDomains;



   /*******************************************************************************
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QuickSightChartMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for accessKey
    **
    *******************************************************************************/
   public String getAccessKey()
   {
      return accessKey;
   }



   /*******************************************************************************
    ** Setter for accessKey
    **
    *******************************************************************************/
   public void setAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
   }



   /*******************************************************************************
    ** Fluent setter for accessKey
    **
    *******************************************************************************/
   public QuickSightChartMetaData withAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {

      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public QuickSightChartMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for secretKey
    **
    *******************************************************************************/
   public String getSecretKey()
   {
      return secretKey;
   }



   /*******************************************************************************
    ** Setter for secretKey
    **
    *******************************************************************************/
   public void setSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
   }



   /*******************************************************************************
    ** Fluent setter for secretKey
    **
    *******************************************************************************/
   public QuickSightChartMetaData withSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dashboardId
    **
    *******************************************************************************/
   public String getDashboardId()
   {
      return dashboardId;
   }



   /*******************************************************************************
    ** Setter for dashboardId
    **
    *******************************************************************************/
   public void setDashboardId(String dashboardId)
   {
      this.dashboardId = dashboardId;
   }



   /*******************************************************************************
    ** Fluent setter for dashboardId
    **
    *******************************************************************************/
   public QuickSightChartMetaData withDashboardId(String dashboardId)
   {
      this.dashboardId = dashboardId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for accountId
    **
    *******************************************************************************/
   public String getAccountId()
   {
      return accountId;
   }



   /*******************************************************************************
    ** Setter for accountId
    **
    *******************************************************************************/
   public void setAccountId(String accountId)
   {
      this.accountId = accountId;
   }



   /*******************************************************************************
    ** Fluent setter for accountId
    **
    *******************************************************************************/
   public QuickSightChartMetaData withAccountId(String accountId)
   {
      this.accountId = accountId;
      return this;
   }



   /*******************************************************************************
    ** Getter for userArn
    **
    *******************************************************************************/
   public String getUserArn()
   {
      return userArn;
   }



   /*******************************************************************************
    ** Setter for userArn
    **
    *******************************************************************************/
   public void setUserArn(String userArn)
   {
      this.userArn = userArn;
   }



   /*******************************************************************************
    ** Fluent setter for userArn
    **
    *******************************************************************************/
   public QuickSightChartMetaData withUserArn(String userArn)
   {
      this.userArn = userArn;
      return this;
   }



   /*******************************************************************************
    ** Getter for region
    **
    *******************************************************************************/
   public String getRegion()
   {
      return region;
   }



   /*******************************************************************************
    ** Setter for region
    **
    *******************************************************************************/
   public void setRegion(String region)
   {
      this.region = region;
   }



   /*******************************************************************************
    ** Fluent setter for region
    **
    *******************************************************************************/
   public QuickSightChartMetaData withRegion(String region)
   {
      this.region = region;
      return this;
   }



   /*******************************************************************************
    ** Getter for allowedDomains
    **
    *******************************************************************************/
   public Collection<String> getAllowedDomains()
   {
      return allowedDomains;
   }



   /*******************************************************************************
    ** Setter for allowedDomains
    **
    *******************************************************************************/
   public void setAllowedDomains(Collection<String> allowedDomains)
   {
      this.allowedDomains = allowedDomains;
   }



   /*******************************************************************************
    ** Fluent setter for allowedDomains
    **
    *******************************************************************************/
   public QuickSightChartMetaData withAllowedDomains(Collection<String> allowedDomains)
   {
      this.allowedDomains = allowedDomains;
      return this;
   }
}
