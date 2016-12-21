package io.heynow.bufferoperator.service

import io.heynow.bufferoperator.dao.BufferedEventDao
import io.heynow.bufferoperator.domain.BufferedEvent
import io.heynow.bufferoperator.service.impl.BufferServiceImpl
import io.heynow.stream.manager.client.facade.StreamManagerClient
import spock.lang.Specification

class BufferServiceImplTest extends Specification {

    private BufferServiceImpl bufferService
    private StreamManagerClient streamManagerClient
    private BufferedEventDao bufferedEventDao
    private int operatorId = 1;

    def setup() {
        bufferedEventDao = Mock()
        streamManagerClient = Mock()

        bufferService = new BufferServiceImpl(streamManagerClient, bufferedEventDao)

        bufferedEventDao.findAllByOperatorId(operatorId) >> new ArrayList<BufferedEvent>()
    }


    def "should get all events for operatorId"() {
        when:
        bufferService.buffer(1, new HashMap<String, Object>())
        then:
        1 * bufferedEventDao.findAllByOperatorId(1) >> new ArrayList<BufferedEvent>()
    }

    def "should return empty optional for empty events lists"() {
        when:
        Optional<BufferedEvent> result = bufferService.buffer(operatorId, new HashMap<String, Object>())

        then:
        !result.isPresent()
    }

    def "should persist all events"() {
        int eventsNumber = 10;


        when:
        for (int i = 0; i < eventsNumber; i++) {
            bufferService.buffer(1, new HashMap<String, Object>())
        }

        then:
        eventsNumber * bufferedEventDao.save(_)
    }


    def "should return payloads list after buffer count exceeded"() {
        streamManagerClient.getProperties(operatorId).get(_) >> 10

        when:
        Optional<BufferedEvent> result = bufferService.buffer(operatorId, new HashMap<String, Object>())


        then:
        bufferedEventDao.findAllByOperatorId(operatorId) >> getArrayListWithSize(10)
        result.isPresent()
    }

    def "should return empty payloads optional after buffer count not exceeded"() {

        streamManagerClient.getProperties(operatorId) >> createBufferPropertiesMap(10)

        when:
        Optional<BufferedEvent> result = bufferService.buffer(operatorId, new HashMap<String, Object>())

        then:
        bufferedEventDao.findAllByOperatorId(operatorId) >> getArrayListWithSize(8)
        !result.isPresent()
    }

    def "should set default buffer count- less events number should return empty optional"() {
        when:
        Optional<BufferedEvent> result = bufferService.buffer(operatorId, new HashMap<String, Object>())

        then:
        bufferedEventDao.findAllByOperatorId(operatorId) >> getArrayListWithSize(2)
        !result.isPresent()
    }

    def "should set default buffer count- equal or greater events number should return non-empty optional"() {
        when:
        Optional<BufferedEvent> result = bufferService.buffer(operatorId, new HashMap<String, Object>())

        then:
        bufferedEventDao.findAllByOperatorId(operatorId) >> getArrayListWithSize(3)
        result.isPresent()
    }

    def "should delete persisted events after returning buffer"() {
        ArrayList<BufferedEvent> bufferedEvents = getArrayListWithSize(3);


        when:
        bufferService.buffer(operatorId, new HashMap<String, Object>())


        then:
        bufferedEventDao.findAllByOperatorId(operatorId) >> bufferedEvents
        streamManagerClient.getProperties(operatorId) >> createBufferPropertiesMap(3)

        for (BufferedEvent bufferedEvent : bufferedEvents) {
            1 * bufferedEventDao.delete(bufferedEvent)
        }
    }

    private List<BufferedEvent> getArrayListWithSize(int size) {
        List<BufferedEvent> bufferedEventList = new ArrayList<>()
        for (int i = 0; i < size; i++) {
            bufferedEventList.add(new BufferedEvent())
        }
        return bufferedEventList
    }


    private Map<String, Object> createBufferPropertiesMap(int i) {
        Map<String, Object> map = new HashMap<>()
        map.put(BufferServiceImpl.COUNT_PROPERTY, i)
        return map
    }
}