package com.example.moodwriter.global.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.moodwriter.MoodWriterApplication;
import com.example.moodwriter.domain.notification.service.NotificationScheduler;
import com.example.moodwriter.global.exception.LambdaException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.util.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
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
    ObjectMapper objectMapper = BeanUtils.getBean(ObjectMapper.class);
    Map<String, Object> map;

    byte[] inputBytes = inputStreamToByteArray(input);

    map = objectMapper.readValue(inputBytes, Map.class);

    if (map != null && map.containsKey("eventType")) {
      handleEvent(map);
    } else {
      ByteArrayInputStream copyInputStream = new ByteArrayInputStream(inputBytes);

      handler.proxyStream(copyInputStream, output, context);
    }
  }
  private byte[] inputStreamToByteArray(InputStream input) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = input.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, bytesRead);
    }
    return byteArrayOutputStream.toByteArray();
  }

  public void handleEvent(Map<String, Object> event) {

    log.info("Received event: {}", event);

    String eventType = (String) event.get("eventType");

    if ("KeepAlive".equals(eventType)) {
      log.info("Lambda keep-alive trigger: {}", event.get("message"));

    } else if ("ScheduledNotification".equals(eventType)) {
      NotificationScheduler notificationScheduler = BeanUtils.getBean(
          NotificationScheduler.class);

      log.info("NotificationScheduler DI completed");
      log.info("Scheduled Notification trigger: {}", event.get("message"));

      notificationScheduler.processNotifications();

    } else {

      log.error("Invalid request received. Event type: {}", eventType);
      throw new LambdaException(ErrorCode.INVALID_REQUEST_RECEIVED);
    }
  }
}
