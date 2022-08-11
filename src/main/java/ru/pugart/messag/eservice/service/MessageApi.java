package ru.pugart.messag.eservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.dto.ChatDto;
import ru.pugart.messag.eservice.entity.Chat;

public interface MessageApi {
    Mono<Chat> createOrUpdateChat(String ownerId, String chatPartnerId, String message);
    void removeChat(String ownerId, String chatId);
    Mono<ChatDto> getMessages(String ownerId, String chatPartnerId);
    Flux<ChatDto> getChats(String ownerId);
}
