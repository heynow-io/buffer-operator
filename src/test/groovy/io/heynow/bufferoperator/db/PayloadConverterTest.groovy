package io.heynow.bufferoperator.service

import io.heynow.bufferoperator.db.PayloadConverter
import spock.lang.Specification

class PayloadConverterTest extends Specification {


    private PayloadConverter converter;
    def exampleJson = "{\"a\":\"b\", \"c\":\"d\"}"
    def Map<String, Object> exampleMap = ['a': 'b', 'c': 'd']

    def setup() {
        converter = new PayloadConverter()
    }

    def "should convert map to string"() {
        when:
        def json = converter.convertToDatabaseColumn(exampleMap)

        then:
        json.replaceAll("\\s+", "").equalsIgnoreCase(exampleJson.replaceAll("\\s+", ""))
    }


    def "should handle null map"() {
        when:
        Map<String, Object> map = null
        def json = converter.convertToDatabaseColumn(map)

        then:
        json == "null"
    }


    def "should convert json to map"() {
        when:
        def map = converter.convertToEntityAttribute(exampleJson)

        then:
        map.equals(exampleMap)
    }

    def "should handle null json"() {
        when:
        def map = converter.convertToEntityAttribute("null")

        then:
        map == null
    }

    def "should handle uncorrect json"() {
        when:
        def map = converter.convertToEntityAttribute("asdasdnull")

        then:
        map == null
    }
}