package com.inventory.financeservice.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SequenceGeneratorService {

    private final ConcurrentHashMap<String, AtomicLong> sequences = new ConcurrentHashMap<>();

    public String generateSequence(String sequenceName) {
        AtomicLong sequence = sequences.computeIfAbsent(sequenceName, k -> new AtomicLong(0));
        long nextValue = sequence.incrementAndGet();
        return String.format("%06d", nextValue);
    }

    public String generateNumber(String prefix) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = generateSequence(prefix);
        return prefix + dateStr + sequence;
    }
}
