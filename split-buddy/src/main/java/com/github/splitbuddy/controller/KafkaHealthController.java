package com.github.splitbuddy.controller;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class KafkaHealthController {

    private final AdminClient adminClient;

    @GetMapping("/kafka/health")
    public String checkKafkaConnection() {
        try {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get();
            return "Connected to Kafka. Topics: " + topicNames;
        } catch (InterruptedException | ExecutionException e) {
            return "Failed to connect to Kafka: " + e.getMessage();
        }
    }
}