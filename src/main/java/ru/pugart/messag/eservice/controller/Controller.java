package ru.pugart.messag.eservice.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.dto.ChatDto;
import ru.pugart.messag.eservice.service.MessageApi;
import ru.pugart.messag.eservice.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/chat")
@AllArgsConstructor
@Slf4j
public class Controller implements MessageApi {
    private final MessageService messageService;

    @Override
    @PostMapping(value = "create")
    public Mono<ChatDto> createOrFindChat(@RequestParam String taskId,
                                          @RequestParam String name,
                                          @RequestParam List<String> members) {
        return messageService.createOrFindChat(taskId, name, members);
    }

    @Override
    @PostMapping(value = "update")
    public Mono<ChatDto> updateChat(@RequestParam String chatCode, @RequestParam String authorId, @RequestBody String message) {
        return messageService.updateChat(chatCode, authorId, message);
    }

    @Override
    @GetMapping(value = "remove")
    public Mono<Void> removeChat(@RequestParam String chatId, @RequestParam String authorId) {
        return messageService.removeChat(chatId, authorId);
    }

    @Override
    @GetMapping(value = "mark-as-read")
    public Mono<ChatDto> markAsRead(String chatCode, String authorId) {
        return messageService.markAsRead(chatCode, authorId);
    }

    @Override
    @GetMapping(value = "messages")
    public Mono<ChatDto> getMessages(@RequestParam String chatCode, @RequestParam String authorId) {
        return messageService.getMessages(chatCode, authorId);
    }

    @Override
    @GetMapping(value = "find-all")
    public Flux<ChatDto> getChats(@RequestParam String authorId) {
        return messageService.getChats(authorId);
    }
}
