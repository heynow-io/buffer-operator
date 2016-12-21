package io.heynow.bufferoperator.service.impl;

import io.heynow.bufferoperator.dao.BufferedEventDao;
import io.heynow.bufferoperator.domain.BufferedEvent;
import io.heynow.bufferoperator.service.BufferService;
import io.heynow.stream.manager.client.facade.StreamManagerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BufferServiceImpl implements BufferService {
    public static final Integer DEFAULT_BUFFER_COUNT = 3;
    public static final String COUNT_PROPERTY = "count";
    public static final String PAYLOADS_MAP_KEY = "payloads";

    private final StreamManagerClient streamManagerClient;
    private final BufferedEventDao bufferedEventDao;


    @Override
    public Optional<Map<String, Object>> buffer(long operatorId, Map<String, Object> input) {
        BufferedEvent bufferedEvent = BufferedEvent.builder().operatorId(operatorId).payload(input).build();
        bufferedEventDao.save(bufferedEvent);

        List<BufferedEvent> allEventsForOperatorId = bufferedEventDao.findAllByOperatorId(operatorId);

        Integer count = getBufferCountFromPropertiesOrDefault(operatorId);
        if (allEventsForOperatorId.size() >= count) {
            allEventsForOperatorId.forEach(bufferedEventDao::delete);

            Map<String, Object> stringObjectMap = createMapWithEventsPayloads(allEventsForOperatorId);
            return Optional.of(stringObjectMap);
        }

        return Optional.empty();
    }

    private Map<String, Object> createMapWithEventsPayloads(List<BufferedEvent> allEventsForOperatorId) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put(PAYLOADS_MAP_KEY, allEventsForOperatorId.stream().map(x -> x.getPayload()).collect(Collectors.toList()));
        return stringObjectMap;
    }

    private Integer getBufferCountFromPropertiesOrDefault(long operatorId) {
        try {
            Integer count = (Integer) streamManagerClient.getProperties(operatorId).get(COUNT_PROPERTY);
            log.debug("Read count value from properties: " + count);
            return count;
        } catch (Exception e) {
            log.warn("Could not find 'count' property in operator configuration, setting default " + DEFAULT_BUFFER_COUNT);
        }
        return DEFAULT_BUFFER_COUNT;


    }

}
