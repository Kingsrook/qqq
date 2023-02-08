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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class Auth0PermissionsHelper
{
   private String baseUrl;
   private String apiName;
   private String token;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public Auth0PermissionsHelper(String baseUrl, String apiName, String token)
   {
      this.baseUrl = baseUrl;
      this.apiName = apiName;
      this.token = token;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getCurrentAuth0Permissions() throws QException
   {
      Set<String> rs = new LinkedHashSet<>();
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         HttpGet request = new HttpGet(baseUrl + "/resource-servers/" + URLEncoder.encode(apiName, StandardCharsets.UTF_8));
         request.addHeader("Authorization", "Bearer " + token);
         request.addHeader("Content-Type", "application/json");

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            logResponseStatus(response);

            String     body   = EntityUtils.toString(response.getEntity());
            JSONObject json   = new JSONObject(body);
            JSONArray  scopes = json.getJSONArray("scopes");
            for(int i = 0; i < scopes.length(); i++)
            {
               JSONObject scope = scopes.getJSONObject(i);
               rs.add(scope.getString("value"));
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QException("Error getting current auth0 permissions", e));
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getRoles() throws QException
   {
      Set<String> rs = new LinkedHashSet<>();
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         HttpGet request = new HttpGet(baseUrl + "roles");
         request.addHeader("Authorization", "Bearer " + token);
         request.addHeader("Content-Type", "application/json");

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            logResponseStatus(response);

            String    body = EntityUtils.toString(response.getEntity());
            JSONArray json = new JSONArray(body);
            for(int i = 0; i < json.length(); i++)
            {
               JSONObject role = json.getJSONObject(i);
               rs.add(role.getString("id"));
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QException("Error getting auth0 roles", e));
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getPermissionsForRole(String roleId) throws QException
   {
      Set<String> rs = new LinkedHashSet<>();
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         for(int page = 0; ; page++)
         {
            HttpGet request = new HttpGet(baseUrl + "roles/" + roleId + "/permissions?page=" + page);
            request.addHeader("Authorization", "Bearer " + token);
            request.addHeader("Content-Type", "application/json");

            try(CloseableHttpResponse response = httpClient.execute(request))
            {
               logResponseStatus(response);

               String    body = EntityUtils.toString(response.getEntity());
               JSONArray json = new JSONArray(body);
               if(json.isEmpty())
               {
                  break;
               }

               for(int i = 0; i < json.length(); i++)
               {
                  JSONObject permission = json.getJSONObject(i);
                  rs.add(permission.getString("permission_name"));
               }
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QException("Error getting auth0 permissions for role", e));
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addPermissionsToRole(String roleId, Set<String> permissionsToAdd) throws QException
   {
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         HttpPost request = new HttpPost(baseUrl + "/roles/" + roleId + "/permissions");
         request.addHeader("Authorization", "Bearer " + token);
         request.addHeader("Content-Type", "application/json");

         JSONArray permissions = new JSONArray();
         for(String permissionName : permissionsToAdd)
         {
            JSONObject permission = new JSONObject();
            permissions.put(permission);
            permission.put("resource_server_identifier", apiName);
            permission.put("permission_name", permissionName);
         }
         JSONObject body = new JSONObject();
         body.put("permissions", permissions);

         request.setEntity(new StringEntity(body.toString()));

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            logResponseStatus(response);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QException("Error storing permissions for role in auth0", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void removePermissionsFromRole(String roleId, Set<String> permissionsToRemove) throws QException
   {
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         HttpDeleteWithBody request = new HttpDeleteWithBody(baseUrl + "/roles/" + roleId + "/permissions");
         request.addHeader("Authorization", "Bearer " + token);
         request.addHeader("Content-Type", "application/json");

         JSONArray permissions = new JSONArray();
         for(String permissionName : permissionsToRemove)
         {
            JSONObject permission = new JSONObject();
            permissions.put(permission);
            permission.put("resource_server_identifier", apiName);
            permission.put("permission_name", permissionName);
         }
         JSONObject body = new JSONObject();
         body.put("permissions", permissions);

         request.setEntity(new StringEntity(body.toString()));

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            logResponseStatus(response);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QException("Error storing permissions for role in auth0", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getPermissionsInInstanceButNotInAuth0(QInstance qInstance, Collection<String> permissionsCurrentlyInAuth0)
   {
      List<String> allInstancePermissions = new ArrayList<>(PermissionsHelper.getAllAvailablePermissionNames(qInstance).stream().filter(p -> !p.contains(".bulk")).toList());
      allInstancePermissions.removeAll(permissionsCurrentlyInAuth0);
      return new TreeSet<>(allInstancePermissions);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<String> getPermissionsInAuth0ButNotInInstance(QInstance qInstance, Set<String> currentAuth0Permissions)
   {
      List<String> allInstancePermissions = new ArrayList<>(PermissionsHelper.getAllAvailablePermissionNames(qInstance).stream().filter(p -> !p.contains(".bulk")).toList());
      Set<String>  rs                     = new TreeSet<>(currentAuth0Permissions);
      allInstancePermissions.forEach(rs::remove);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addPermissionsToAuth0(QInstance qInstance, Collection<String> permissionsCurrentlyInAuth0, Collection<String> permissionsToAddToAuth0) throws QException
   {
      Set<String> permissions = new TreeSet<>();
      permissions.addAll(permissionsCurrentlyInAuth0);
      permissions.addAll(permissionsToAddToAuth0);
      storePermissionsInAuth0(qInstance, permissions);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void removePermissionsToAuth0(QInstance qInstance, Collection<String> permissionsCurrentlyInAuth0, Collection<String> permissionsToRemoveFromAuth0) throws QException
   {
      Set<String> permissions = new TreeSet<>(permissionsCurrentlyInAuth0);
      permissions.removeAll(permissionsToRemoveFromAuth0);
      storePermissionsInAuth0(qInstance, permissions);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void replaceAuth0PermissionsWithAllFromInstance(QInstance qInstance) throws QException
   {
      List<String> permissions = PermissionsHelper.getAllAvailablePermissionNames(qInstance).stream().filter(p -> !p.contains(".bulk")).toList();
      storePermissionsInAuth0(qInstance, permissions);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void storePermissionsInAuth0(QInstance qInstance, Collection<String> permissions) throws QException
   {
      try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
      {
         HttpPatch request = new HttpPatch(baseUrl + "/resource-servers/" + URLEncoder.encode(apiName, StandardCharsets.UTF_8));
         request.addHeader("Authorization", "Bearer " + token);
         request.addHeader("Content-Type", "application/json");

         buildRequestBodyWithScopesFromPermissions(qInstance, permissions, request);

         try(CloseableHttpResponse response = httpClient.execute(request))
         {
            logResponseStatus(response);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw (new QException("Error storing permissions in auth0", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void logResponseStatus(CloseableHttpResponse response) throws IOException
   {
      if(response.getStatusLine() != null)
      {
         Integer statusCode         = response.getStatusLine().getStatusCode();
         String  statusReasonPhrase = response.getStatusLine().getReasonPhrase();
         System.out.println("Result: " + statusCode + " : " + statusReasonPhrase);

         if(statusCode > 299)
         {
            System.out.println(EntityUtils.toString(response.getEntity()));
            throw (new IllegalStateException("Bad status code: " + statusCode));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void buildRequestBodyWithScopesFromPermissions(QInstance qInstance, Collection<String> permissions, HttpPatch request) throws UnsupportedEncodingException
   {
      JSONArray scopes = new JSONArray();
      for(String permissionName : permissions)
      {
         String[] parts      = permissionName.split("\\.", 2);
         String   name       = parts[0];
         String   permission = parts[1];

         String object = name;
         if(qInstance.getTable(name) != null)
         {
            object = qInstance.getTable(name).getLabel() + " table";
         }
         else if(qInstance.getProcess(name) != null)
         {
            object = qInstance.getProcess(name).getLabel() + " process";
         }
         else if(qInstance.getReport(name) != null)
         {
            object = qInstance.getReport(name).getLabel() + " report";
         }
         else if(qInstance.getWidget(name) != null)
         {
            object = qInstance.getWidget(name).getLabel() + " widget";
         }
         else if(qInstance.getApp(name) != null)
         {
            object = qInstance.getApp(name).getLabel() + " app";
         }
         String verb = permission.equals("hasAccess") ? "access" : permission;

         JSONObject scopeObject = new JSONObject();
         scopeObject.put("value", permissionName);
         scopeObject.put("description", "Permission to " + verb + " the " + object);
         scopes.put(scopeObject);
      }

      JSONObject body = new JSONObject();
      body.put("scopes", scopes);

      request.setEntity(new StringEntity(body.toString()));
   }



   /*******************************************************************************
    ** Getter for baseUrl
    *******************************************************************************/
   public String getBaseUrl()
   {
      return (this.baseUrl);
   }



   /*******************************************************************************
    ** Setter for baseUrl
    *******************************************************************************/
   public void setBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
   }



   /*******************************************************************************
    ** Fluent setter for baseUrl
    *******************************************************************************/
   public Auth0PermissionsHelper withUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for token
    *******************************************************************************/
   public String getToken()
   {
      return (this.token);
   }



   /*******************************************************************************
    ** Setter for token
    *******************************************************************************/
   public void setToken(String token)
   {
      this.token = token;
   }



   /*******************************************************************************
    ** Fluent setter for token
    *******************************************************************************/
   public Auth0PermissionsHelper withToken(String token)
   {
      this.token = token;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      public String getMethod()
      {
         return "DELETE";
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public HttpDeleteWithBody(final String uri)
      {
         super();
         setURI(URI.create(uri));
      }

   }

}
