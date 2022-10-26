package com.krickert.search.download.dump;
import com.krickert.search.model.DownloadedFile;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient(id = "downloaded-file-producer")
public interface DownloadedFileProcessingProducer {
    @Topic("wiki-dump-file")
    void sendFileProcessingRequest(@KafkaKey UUID key, DownloadedFile request);
}