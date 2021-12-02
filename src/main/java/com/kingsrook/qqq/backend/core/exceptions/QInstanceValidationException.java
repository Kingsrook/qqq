package com.kingsrook.qqq.backend.core.exceptions;


import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Exception thrown during qqq-starup, if a QInstance is found to have validation
 ** issues.  Contains a list of reasons (to avoid spoon-feeding as much as possible).
 **
 *******************************************************************************/
public class QInstanceValidationException extends QException
{
   private List<String> reasons;



   /*******************************************************************************
    ** Constructor of message - does not populate reasons!
    **
    *******************************************************************************/
   public QInstanceValidationException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    ** Constructor of a list of reasons.  They feed into the core exception message.
    **
    *******************************************************************************/
   public QInstanceValidationException(List<String> reasons)
   {
      super(
         (reasons != null && reasons.size() > 0)
            ? "Instance validation failed for the following reasons:  " + StringUtils.joinWithCommasAndAnd(reasons)
            : "Validation failed, but no reasons were provided");

      if(reasons != null && reasons.size() > 0)
      {
         this.reasons = reasons;
      }
   }



   /*******************************************************************************
    ** Constructor of an array/varargs of reasons.  They feed into the core exception message.
    **
    *******************************************************************************/
   public QInstanceValidationException(String... reasons)
   {
      super(
         (reasons != null && reasons.length > 0)
            ? "Instance validation failed for the following reasons:  " + StringUtils.joinWithCommasAndAnd(Arrays.stream(reasons).toList())
            : "Validation failed, but no reasons were provided");

      if(reasons != null && reasons.length > 0)
      {
         this.reasons = Arrays.stream(reasons).toList();
      }
   }



   /*******************************************************************************
    ** Constructor of message & cause - does not populate reasons!
    **
    *******************************************************************************/
   public QInstanceValidationException(String message, Throwable cause)
   {
      super(message, cause);
   }



   /*******************************************************************************
    ** Getter for reasons
    **
    *******************************************************************************/
   public List<String> getReasons()
   {
      return reasons;
   }
}
