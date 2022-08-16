package ru.pugart.messag.eservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.config.AppConfig;
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

    private final AppConfig appConfig;
    private final MessageRepository messageRepository;

    @Override
    public Mono<ChatDto> createOrUpdateChat(String ownerId, String chatPartnerId, String message) {
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

                    if(chat.getMessageCount() >= appConfig.getLimitMessage()){
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
                                    .build())
                                // convert to dto
                                .flatMap(ch -> Mono.just(convertToDto(ch)))
                                .switchIfEmpty(Mono.error(new RuntimeException("oops!")));
                    }
                    return Mono.just(convertToDto(chat));
                });
    }

    @Override
    public Mono<Void> removeChat(String ownerId, String chatPartnerId) {
        return Flux.merge(
                    messageRepository.findAllByOwnerIdAndChatPartnerId(ownerId, chatPartnerId),
                    messageRepository.findAllByOwnerIdAndChatPartnerId(chatPartnerId, ownerId)
                )
                .log()
                .flatMap(chat -> messageRepository.deleteById(chat.getChatId()))
                .then();
    }

    @Override
    public Mono<ChatDto> getMessages(String ownerId, String chatPartnerId) {
        return Flux.merge(
                    messageRepository.findAllByOwnerIdAndChatPartnerId(ownerId, chatPartnerId),
                    messageRepository.findAllByOwnerIdAndChatPartnerId(chatPartnerId, ownerId)
                )
                .log()
                .switchIfEmpty(Flux.empty())
                .collectList()
                .flatMap(chats -> {
                    List<String> messageTextList = new ArrayList<>();
                    chats.forEach(c -> {
                        if(c.getMessages() != null) {
                            messageTextList.addAll(c.getMessages());
                        }
                    });

                    if(messageTextList.isEmpty()){
                        return Mono.empty();
                    }

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
                .chatId(chat.getChatId())
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
