package io.heynow.bufferoperator.dao;

import io.heynow.bufferoperator.domain.BufferedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BufferedEventDao extends JpaRepository<BufferedEvent, Long> {
    List<BufferedEvent> findAllByOperatorId(Long operatorId);

    void deleteAllByOperatorId(Long operatorId);
}
