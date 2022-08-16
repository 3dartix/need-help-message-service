package ru.pugart.messag.eservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.dto.ChatDto;

public interface MessageApi {
    Mono<ChatDto> createOrUpdateChat(String ownerId, String chatPartnerId, String message);
    Mono<Void> removeChat(String ownerId, String chatPartnerId);
    Flux<ChatDto> markAsRead(String ownerId, String chatPartnerId);
    Mono<ChatDto> getMessages(String ownerId, String chatPartnerId);
    Flux<ChatDto> getChats(String ownerId);
}
