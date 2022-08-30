package com.kingsrook.qqq.backend.core.actions.dashboard;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.QuickSightChart;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QuickSightChartMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.quicksight.QuickSightClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.quicksight.model.GenerateEmbedUrlForRegisteredUserRequest;
import software.amazon.awssdk.services.quicksight.model.GenerateEmbedUrlForRegisteredUserResponse;
import software.amazon.awssdk.services.quicksight.model.RegisteredUserDashboardEmbeddingConfiguration;
import software.amazon.awssdk.services.quicksight.model.RegisteredUserEmbeddingExperienceConfiguration;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuickSightChartRenderer extends AbstractWidgetRenderer
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object render(QInstance qInstance, QSession session, QWidgetMetaDataInterface metaData) throws QException
   {
      try
      {
         QuickSightChartMetaData quickSightMetaData = (QuickSightChartMetaData) metaData;
         QuickSightClient        quickSightClient   = getQuickSightClient(quickSightMetaData);

         final RegisteredUserEmbeddingExperienceConfiguration experienceConfiguration = RegisteredUserEmbeddingExperienceConfiguration.builder()
            .dashboard(
               RegisteredUserDashboardEmbeddingConfiguration.builder()
                  .initialDashboardId(quickSightMetaData.getDashboardId())
                  .build())
            .build();

         final GenerateEmbedUrlForRegisteredUserRequest generateEmbedUrlForRegisteredUserRequest = GenerateEmbedUrlForRegisteredUserRequest.builder()
            .awsAccountId(quickSightMetaData.getAccountId())
            .userArn(quickSightMetaData.getUserArn())
            .experienceConfiguration(experienceConfiguration)
            .build();

         final GenerateEmbedUrlForRegisteredUserResponse generateEmbedUrlForRegisteredUserResponse = quickSightClient.generateEmbedUrlForRegisteredUser(generateEmbedUrlForRegisteredUserRequest);

         String embedUrl = generateEmbedUrlForRegisteredUserResponse.embedUrl();
         return (new QuickSightChart(metaData.getName(), quickSightMetaData.getLabel(), embedUrl));
      }
      catch(Exception e)
      {
         throw (new QException("Error rendering widget", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QuickSightClient getQuickSightClient(QuickSightChartMetaData metaData)
   {
      AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(metaData.getAccessKey(), metaData.getSecretKey());

      QuickSightClient amazonQuickSightClient = QuickSightClient.builder()
         .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
         .region(Region.of(metaData.getRegion()))
         .build();

      return (amazonQuickSightClient);
   }

}
