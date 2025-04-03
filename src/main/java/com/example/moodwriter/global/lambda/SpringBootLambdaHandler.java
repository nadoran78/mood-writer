package com.example.moodwriter.global.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.moodwriter.MoodWriterApplication;
import com.example.moodwriter.domain.notification.service.NotificationScheduler;
import com.example.moodwriter.global.exception.LambdaException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpringBootLambdaHandler implements RequestStreamHandler, RequestHandler<Map<String, Object>, String> {

  private final NotificationScheduler notificationScheduler;
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

  @Override
  public String handleRequest(Map<String, Object> event, Context context) {

    log.info("Received event: {}", event);

    String eventType = (String) event.get("eventType");

    if ("KeepAlive".equals(eventType)) {
      log.info("Lambda keep-alive trigger: {}", event.get("message"));

      return "Keep-alive trigger processed successfully.";

    } else if ("ScheduledNotification".equals(eventType)) {
      log.info("Scheduled Notification trigger: {}", event.get("message"));

      notificationScheduler.processNotifications();
      return "Scheduled notification processed successfully.";

    } else {

      log.error("Invalid request received. Event type: {}", eventType);
      throw new LambdaException(ErrorCode.INVALID_REQUEST_RECEIVED);
    }
  }
}
