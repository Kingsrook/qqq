package com.kingsrook.qqq.backend.core.exceptions;


import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class QInstanceValidationException extends QException
{
   private List<String> reasons;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QInstanceValidationException(String message)
   {
      super(message);
   }



   /*******************************************************************************
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
