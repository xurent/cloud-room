package com.shelgon.nopage.config;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.expression.ParseException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@JsonComponent
public class DateFormatConfig {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期格式化
     */
    public static class DateJsonSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(dateFormat.format(date));
        }

    }

    /**
     * 解析日期字符串
     */
    public static class DateJsonDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            try {
                return dateFormat.parse(jsonParser.getText());
            } catch (ParseException | java.text.ParseException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
