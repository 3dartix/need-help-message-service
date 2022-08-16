package ru.pugart.messag.eservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Document(indexName = "chat_index")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @Id
    private String chatId;

    @Field(type = FieldType.Text, name = "owner_id")
    private String ownerId;

    @Field(type = FieldType.Text, name = "chat_partner_id")
    private String chatPartnerId;

    @Field(type = FieldType.Text, name = "message_count")
    private Integer messageCount;

    @Field(type = FieldType.Text, name = "messages")
    private List<String> messages;

    @Field(type = FieldType.Date, name = "created")
    private Instant created;

    @Field(type = FieldType.Boolean, name = "archived")
    private Boolean archived;

    @Field(type = FieldType.Boolean, name = "read")
    private Boolean read;
}
