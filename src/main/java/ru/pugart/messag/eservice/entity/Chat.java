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
    private String id;

    @Field(type = FieldType.Text, name = "message_count")
    private Integer messageCount;

    @Field(type = FieldType.Text, name = "messages")
    private List<String> messages;

    @Field(type = FieldType.Date, name = "created")
    private Instant created;

    @Field(type = FieldType.Boolean, name = "archived")
    private Boolean archived;

    @Field(type = FieldType.Object, name = "read_mark_list")
    private List<ReadMark> readMarkList;

    @Field(type = FieldType.Text, name = "members")
    private List<String> members;

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Text, name = "task_id")
    private String taskId;

    @Field(type = FieldType.Text, name = "previous_chat_id")
    private String chatCode;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReadMark {
        private Boolean read;
        private String member;
    }
}
