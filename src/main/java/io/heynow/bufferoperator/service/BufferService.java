package io.heynow.bufferoperator.service;


import java.util.Map;
import java.util.Optional;

public interface BufferService {
    Optional<Map<String, Object>> buffer(long operatorId, Map<String, Object> input);
}
