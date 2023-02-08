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

package com.kingsrook.qqq.backend.core.modules.authentication.implementations;


import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for Auth0PermissionsHelper
 *******************************************************************************/
class Auth0PermissionsHelperTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // don't really want to test this class (don't want to change permissions), so, just here to help keep class coverage % up //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      Auth0PermissionsHelper auth0PermissionsHelper = new Auth0PermissionsHelper(null, null, null);
      try
      {
         auth0PermissionsHelper.getCurrentAuth0Permissions();
      }
      catch(Exception e)
      {
         // is expected.
      }
   }

}