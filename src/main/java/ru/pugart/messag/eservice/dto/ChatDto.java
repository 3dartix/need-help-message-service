package ru.pugart.messag.eservice.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ChatDto {
    private String chatId;
    private String ownerId;
    private Boolean read;
    private String chatPartnerId;
    private List<Message> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Message {
        private String ownerId;
        private Instant time;
        private String message;

        public Message(String text, String separator) {
            String[] splitText = text.split(separator);
            this.ownerId = splitText[0];
            this.time = Instant.parse(splitText[1]);
            this.message = splitText[2];
        }
    }
}
