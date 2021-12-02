package com.kingsrook.qqq.backend.core.utils;


import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*******************************************************************************
 ** Utility methods for working with Strings
 **
 *******************************************************************************/
public class StringUtils
{
   /*******************************************************************************
    ** test if string is not null and is not empty (after being trimmed).
    **
    ** @param input the string to test
    ** @return Boolean
    *******************************************************************************/
   public static Boolean hasContent(String input)
   {
      if(input != null && !input.trim().equals(""))
      {
         return true;
      }
      return false;
   }



   /*******************************************************************************
    ** returns input.toString() if not null, or nullOutput if input == null (as in SQL NVL)
    **
    *******************************************************************************/
   public static String nvl(Object input, String nullOutput)
   {
      if(input == null)
      {
         return nullOutput;
      }
      return input.toString();
   }



   /*******************************************************************************
    ** returns input if not null, or nullOutput if input == null (as in SQL NVL)
    **
    *******************************************************************************/
   public static String nvl(String input, String nullOutput)
   {
      if(input == null)
      {
         return nullOutput;
      }
      return input;
   }



   /*******************************************************************************
    ** allCapsToMixedCase - ie, UNIT CODE -> Unit Code
    **
    ** @param input
    ** @return
    *******************************************************************************/
   public static String allCapsToMixedCase(String input)
   {
      if(input == null)
      {
         return (null);
      }

      StringBuilder rs = new StringBuilder();

      ///////////////////////////////////////////////////////////////////////
      // match for 0 or more non-capitals (which will pass through as-is), //
      //    then one capital (which will be kept uppercase),               //
      //    then 0 or more capitals (which will be lowercased),            //
      //    then 0 or more non-capitals (which will pass through as-is)    //
      //                                                                   //
      // Example matches are:                                              //
      // "UNIT CODE" -> ()(U)(NIT)( ), then ()(C)(ODE)() -> (Unit )(Code)  //
      // "UNITCODE"  -> ()(U)(NITCODE)(),                -> (Unitcode)     //
      // "UnitCode"  -> ()(U)()(nit), then ()(C)()(ode)  -> (Unit)(Code)   //
      // "UNIT0CODE" -> ()(U)(NIT)(0), then ()(C)(ODE)() -> (Unit0)(Code)  //
      // "0UNITCODE" -> (0)(U)(NITCODE)()                -> (0Unitcode)    //
      ///////////////////////////////////////////////////////////////////////
      Pattern pattern = Pattern.compile("([^A-Z]*)([A-Z])([A-Z]*)([^A-Z]*)");
      Matcher matcher = pattern.matcher(input);
      while(matcher.find())
      {
         rs.append(matcher.group(1) != null ? matcher.group(1) : "");
         rs.append(matcher.group(2) != null ? matcher.group(2) : "");
         rs.append(matcher.group(3) != null ? matcher.group(3).toLowerCase() : "");
         rs.append(matcher.group(4) != null ? matcher.group(4) : "");
      }

      ///////////////////////////////////////////////////////////////////////
      // just in case no match was found, return the original input string //
      ///////////////////////////////////////////////////////////////////////
      if(rs.length() == 0)
      {
         return (input);
      }

      return (rs.toString());
   }



   /*******************************************************************************
    ** truncate a string (null- and index-bounds- safely) at a max length.
    **
    *******************************************************************************/
   public static String safeTruncate(String input, int maxLength)
   {
      if(input == null)
      {
         return (null);
      }

      if(input.length() <= maxLength)
      {
         return (input);
      }

      return (input.substring(0, maxLength));
   }



   /*******************************************************************************
    ** null- and index-bounds- safely truncate a string to a max length, appending
    ** a suffix (like "...") if it did get truncated.  Note that the returned string,
    ** with the suffix added, will be at most maxLength.
    **
    *******************************************************************************/
   public static String safeTruncate(String input, int maxLength, String suffix)
   {
      if(input == null)
      {
         return (null);
      }

      if(input.length() <= maxLength)
      {
         return (input);
      }

      return (input.substring(0, (maxLength - suffix.length())) + suffix);
   }



   /*******************************************************************************
    ** returns input if not null, or nullOutput if input == null (as in SQL NVL)
    **
    *******************************************************************************/
   public static String safeTrim(String input)
   {
      if(input == null)
      {
         return null;
      }
      return input.trim();
   }



   /*******************************************************************************
    ** Join a collection of objects into 1 string
    **
    ** @param   glue - String to insert between entries
    ** @param   collection - The collection of objects to join.
    ** @return String
    *******************************************************************************/
   public static String join(String glue, Collection<? extends Object> collection)
   {
      if(collection == null)
      {
         return (null);
      }

      StringBuffer rs = new StringBuffer();

      int i = 0;
      for(Object s : collection)
      {
         if(i++ > 0)
         {
            rs.append(glue);
         }
         rs.append(String.valueOf(s));
      }

      return (rs.toString());
   }



   /*******************************************************************************
    ** joinWithCommasAndAnd
    **
    ** [one] => [one]
    ** [one, two] => [one and two]
    ** [one, two, three] => [one, two, and three]
    ** [one, two, three, four] => [one, two, three, and four]
    ** etc.
    **
    ** @param input
    ** @return
    *******************************************************************************/
   public static String joinWithCommasAndAnd(List<String> input)
   {
      if(input == null)
      {
         return (null);
      }

      StringBuilder rs = new StringBuilder();
      int size = input.size();

      for(int i = 0; i < size; i++)
      {
         if(i > 0 && size == 2)
         {
            rs.append(" and ");
         }
         else if(i > 0 && i < size - 1)
         {
            rs.append(", ");
         }
         else if(i > 0 && i == size - 1)
         {
            rs.append(", and ");
         }
         rs.append(input.get(i));
      }

      return (rs.toString());

   }



   /*******************************************************************************
    ** Trims leading white spaces from a String. Returns a blank ("") value if NULL
    **
    ** @param  input
    ** @return String
    *******************************************************************************/
   public static String ltrim(String input)
   {
      if(!hasContent(input))
      {
         return "";
      }

      int i = 0;
      while(i < input.length() && Character.isWhitespace(input.charAt(i)))
      {
         i++;
      }
      return (input.substring(i));
   }



   /*******************************************************************************
    ** Trims trailing white spaces from a String. Returns a blank ("") value if NULL
    **
    ** @param  input
    ** @return String
    *******************************************************************************/
   public static String rtrim(String input)
   {
      if(!hasContent(input))
      {
         return "";
      }

      int i = input.length() - 1;
      while(i > 0 && Character.isWhitespace(input.charAt(i)))
      {
         i--;
      }
      return (input.substring(0, i + 1));
   }



   /*******************************************************************************
    ** Switch between strings based on if the size of the parameter collection.  If
    ** it is 1 (the singular) or not-1 (0 or 2+, the plural). Get back "" or "s"
    **
    *******************************************************************************/
   public static String plural(Collection<?> collection)
   {
      return (plural(collection == null ? 0 : collection.size()));
   }



   /*******************************************************************************
    ** Switch between strings based on if the 'size' parameter is 1 (the singular)
    ** or not-1 (0 or 2+, the plural).  Get back "" or "s"
    **
    *******************************************************************************/
   public static String plural(Integer size)
   {
      return (plural(size, "", "s"));
   }



   /*******************************************************************************
    ** Switch between strings based on if the size of the parameter collection.  If
    ** it is 1 (the singular) or not-1 (0 or 2+, the plural).  Specify/customize the
    ** values that you get back (e.g., "y", "ies")
    **
    *******************************************************************************/
   public static String plural(Collection<?> collection, String ifOne, String ifNotOne)
   {
      return (plural(collection.size(), ifOne, ifNotOne));
   }



   /*******************************************************************************
    ** Switch between strings based on if the 'size' parameter is 1 (the singular)
    ** or not-1 (0 or 2+, the plural).  Specify/customize the values that you get back
    ** (e.g., "y", "ies")
    **
    *******************************************************************************/
   public static String plural(Integer size, String ifOne, String ifNotOne)
   {
      return (size != null && size.equals(1) ? ifOne : ifNotOne);
   }

}
