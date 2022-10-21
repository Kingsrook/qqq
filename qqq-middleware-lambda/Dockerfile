FROM public.ecr.aws/aleph0io/lambda/java:17.0.4-al2

COPY target/qqq-middleware-lambda-0.6.0-SNAPSHOT.jar "${LAMBDA_TASK_ROOT}/lib/"

CMD [ "com.kingsrook.qqq.lambda.examples.ExampleLambdaHandler::handleRequest" ]

