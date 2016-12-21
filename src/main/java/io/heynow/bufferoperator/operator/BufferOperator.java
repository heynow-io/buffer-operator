package io.heynow.bufferoperator.operator;

import io.heynow.bufferoperator.service.BufferService;
import io.heynow.stream.manager.client.facade.StreamManagerClient;
import io.heynow.stream.manager.client.model.Note;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.Router;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BufferOperator {

    private static final String INTERNAL = "internal";
    private final StreamManagerClient streamManagerClient;
    private final BufferService bufferService;

    @Filter(inputChannel = Sink.INPUT, outputChannel = INTERNAL)
    public boolean filter(Note note) {
        log.debug("filter " + note);
        Optional<Map<String, Object>> buffer = bufferService.buffer(note.getProcessingModel().getCurrent().getId(), note.getPayload());
        if (buffer.isPresent()) {
            note.setPayload(buffer.get());
            return true;
        }

        return false;
    }

    @Router(inputChannel = INTERNAL)
    public String routeOutput(Note note) {
        log.debug("router " + note);
        return note.proceed().getName();
    }
}
