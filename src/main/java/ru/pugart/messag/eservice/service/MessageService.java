package ru.pugart.messag.eservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.dto.ChatDto;
import ru.pugart.messag.eservice.entity.Chat;
import ru.pugart.messag.eservice.repository.MessageRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MessageService implements MessageApi {

    private final static String MESSAGE_FORMAT = "%s;%s;%s";
    private final static Integer MESSAGE_LIMIT = 10;

    private final MessageRepository messageRepository;

    @Override
    public Mono<Chat> createOrUpdateChat(String ownerId, String chatPartnerId, String message) {
        return messageRepository.findAllByOwnerIdAndChatPartnerIdAndArchived(ownerId, chatPartnerId, false)
                .log()
                .switchIfEmpty(messageRepository.findAllByOwnerIdAndChatPartnerIdAndArchived(chatPartnerId, ownerId, false))
                .switchIfEmpty(
                        Mono.just(Chat.builder()
                                .ownerId(ownerId)
                                .chatPartnerId(chatPartnerId)
                                .created(Instant.now())
                                .messageCount(0)
                                .archived(false)
                                .build())
                )
                .log()
                .flatMap(chat -> {
                    if(chat.getMessages() == null) {
                        chat.setMessages(new ArrayList<>());
                    }

                    Instant now = Instant.now();
                    chat.getMessages().add(String.format(MESSAGE_FORMAT, ownerId, now, message));
                    chat.setMessageCount(chat.getMessageCount() + 1);

                    if(chat.getMessageCount() >= MESSAGE_LIMIT){
                        chat.setArchived(true);
                    }

                    log.info("prepared chat: {}", chat);
                    return messageRepository.save(chat);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("oops!")))
                .flatMap(chat -> {
                    if(chat.getArchived()) {
                        return messageRepository.save(Chat.builder()
                                .ownerId(chat.getOwnerId())
                                .chatPartnerId(chat.getChatPartnerId())
                                .created(Instant.now())
                                .messageCount(0)
                                .archived(false)
                                .build());
                    }
                    return Mono.just(chat);
                });
    }

    @Override
    public void removeChat(String ownerId, String chatId) {
        messageRepository.deleteByChatIdAndOwnerId(chatId, ownerId);
    }

    @Override
    public Mono<ChatDto> getMessages(String ownerId, String chatPartnerId) {
        return messageRepository.findAllByOwnerIdAndChatPartnerId(ownerId, chatPartnerId)
                .log()
                .switchIfEmpty(Flux.empty())
                .collectList()
                .flatMap(chats -> {
                    List<String> messageTextList = new ArrayList<>();
                    chats.forEach(c -> messageTextList.addAll(c.getMessages()));

                    ChatDto chatDto = ChatDto.builder()
                            .ownerId(ownerId)
                            .chatPartnerId(chatPartnerId)
                            .messages(convertTextToMessage(messageTextList))
                            .build();

                    log.info("chatDto: {}", chatDto);

                    return Mono.just(chatDto);
                })
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Flux<ChatDto> getChats(String ownerId) {
        return messageRepository.findByOwnerIdOrChatPartnerId(ownerId, ownerId)
                .log()
                .flatMap(chat -> Flux.just(convertToDto(chat)))
                .switchIfEmpty(Flux.empty());
    }

    private ChatDto convertToDto(Chat chat){
        return ChatDto.builder()
                .ownerId(chat.getOwnerId())
                .chatPartnerId(chat.getChatPartnerId())
                .messages(convertTextToMessage(chat.getMessages()))
                .build();
    }

    private List<ChatDto.Message> convertTextToMessage(List<String> messageTextList){
        return messageTextList.stream()
                .map(messageText -> new ChatDto.Message(messageText, ";"))
                .sorted((Comparator.comparing(ChatDto.Message::getTime, Comparator.nullsFirst(Comparator.naturalOrder()))))
                .collect(Collectors.toList());
    }
}
