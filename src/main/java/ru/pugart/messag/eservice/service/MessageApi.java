package ru.pugart.messag.eservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.dto.ChatDto;

import java.util.List;

public interface MessageApi {
    Mono<ChatDto> createOrFindChat(String taskId, String name, List<String> members);
    Mono<ChatDto> updateChat(String chatCode, String authorId, String message);
    Mono<Void> removeChat(String chatCode, String authorId);
    Mono<ChatDto> markAsRead(String chatCode, String authorId);
    Mono<ChatDto> getMessages(String chatCode, String authorId);
    Flux<ChatDto> getChats(String authorId);
}
