package io.heynow.bufferoperator.domain;

import io.heynow.bufferoperator.db.PayloadConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Map;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BufferedEvent {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    private Long operatorId;
    @Column
    @Convert(converter = PayloadConverter.class)
    private Map<String, Object> payload;

}
