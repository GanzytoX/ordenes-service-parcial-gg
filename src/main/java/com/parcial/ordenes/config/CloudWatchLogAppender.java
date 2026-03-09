package com.parcial.ordenes.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Custom Logback Appender for AWS CloudWatch Logs.
 * Supports LocalStack via AWS_ENDPOINT_URL environment variable.
 */
public class CloudWatchLogAppender extends AppenderBase<ILoggingEvent> {

    private String logGroupName = "ordenes-log-group";
    private String logStreamName = "ordenes-stream-" + UUID.randomUUID().toString().substring(0, 8);
    private String region = "us-east-1";
    private String endpoint;

    private CloudWatchLogsClient client;
    private String sequenceToken = null;
    private final List<InputLogEvent> buffer = new ArrayList<>();
    private static final int BATCH_SIZE = 25;

    public void setLogGroupName(String logGroupName) { this.logGroupName = logGroupName; }
    public void setLogStreamName(String logStreamName) { this.logStreamName = logStreamName; }
    public void setRegion(String region) { this.region = region; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    @Override
    public void start() {
        try {
            String accessKey = System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "test");
            String secretKey = System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "test");
            String regionStr = System.getenv().getOrDefault("AWS_DEFAULT_REGION", region);
            String endpointUrl = System.getenv().getOrDefault("AWS_ENDPOINT_URL", endpoint);

            CloudWatchLogsClientBuilder builder = CloudWatchLogsClient.builder()
                    .region(Region.of(regionStr))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ));

            if (endpointUrl != null && !endpointUrl.isEmpty()) {
                builder.endpointOverride(URI.create(endpointUrl));
            }

            client = builder.build();
            ensureLogGroupAndStream();
            super.start();
        } catch (Exception e) {
            addError("Error al inicializar CloudWatchLogAppender: " + e.getMessage(), e);
        }
    }

    private void ensureLogGroupAndStream() {
        try {
            client.createLogGroup(CreateLogGroupRequest.builder()
                    .logGroupName(logGroupName).build());
        } catch (ResourceAlreadyExistsException ignored) { }

        try {
            client.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName).build());
        } catch (ResourceAlreadyExistsException ignored) { }
    }

    @Override
    protected synchronized void append(ILoggingEvent event) {
        if (client == null) return;
        try {
            buffer.add(InputLogEvent.builder()
                    .timestamp(event.getTimeStamp())
                    .message(String.format("[%s] %s - %s",
                            event.getLevel(), event.getLoggerName(), event.getFormattedMessage()))
                    .build());

            if (buffer.size() >= BATCH_SIZE) {
                flush();
            }
        } catch (Exception e) {
            addError("Error al agregar log a CloudWatch", e);
        }
    }

    private synchronized void flush() {
        if (buffer.isEmpty() || client == null) return;
        try {
            PutLogEventsRequest.Builder requestBuilder = PutLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .logEvents(new ArrayList<>(buffer));

            if (sequenceToken != null) {
                requestBuilder.sequenceToken(sequenceToken);
            }

            PutLogEventsResponse response = client.putLogEvents(requestBuilder.build());
            sequenceToken = response.nextSequenceToken();
            buffer.clear();
        } catch (InvalidSequenceTokenException e) {
            sequenceToken = e.expectedSequenceToken();
            flush();
        } catch (Exception e) {
            addError("Error al enviar logs a CloudWatch: " + e.getMessage(), e);
            buffer.clear();
        }
    }

    @Override
    public void stop() {
        flush();
        if (client != null) {
            client.close();
        }
        super.stop();
    }
}
