package ru.pugart.messag.eservice.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.dto.ChatDto;
import ru.pugart.messag.eservice.entity.Chat;
import ru.pugart.messag.eservice.service.MessageApi;
import ru.pugart.messag.eservice.service.MessageService;

@RestController
@RequestMapping("/chat")
@AllArgsConstructor
@Slf4j
public class Controller implements MessageApi {
    private final MessageService messageService;

    @Override
    @PostMapping(value = "create-or-update")
    public Mono<ChatDto> createOrUpdateChat(@RequestParam String ownerId, @RequestParam String chatPartnerId, @RequestParam String message) {
        return messageService.createOrUpdateChat(ownerId, chatPartnerId, message);
    }

    @Override
    @GetMapping(value = "remove")
    public Mono<Void> removeChat(@RequestParam String ownerId, @RequestParam String chatPartnerId) {
        return messageService.removeChat(ownerId, chatPartnerId);
    }

    @Override
    @GetMapping(value = "messages")
    public Mono<ChatDto> getMessages(@RequestParam String ownerId, @RequestParam String chatPartnerId) {
        return messageService.getMessages(ownerId, chatPartnerId);
    }

    @Override
    @GetMapping(value = "find-all")
    public Flux<ChatDto> getChats(@RequestParam String ownerId) {
        return messageService.getChats(ownerId);
    }
}
