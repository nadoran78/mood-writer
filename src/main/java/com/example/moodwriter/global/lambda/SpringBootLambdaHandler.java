package com.example.moodwriter.global.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.moodwriter.MoodWriterApplication;
import com.example.moodwriter.global.exception.LambdaException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SpringBootLambdaHandler implements RequestStreamHandler {

  private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

  static {
    try {
      handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(
          MoodWriterApplication.class);
    } catch (ContainerInitializationException e) {
      throw new LambdaException(ErrorCode.FAIL_INITIALIZE_APPLICATION);
    }
  }

  @Override
  public void handleRequest(InputStream input, OutputStream output, Context context)
      throws IOException {
    handler.proxyStream(input, output, context);
  }
}
