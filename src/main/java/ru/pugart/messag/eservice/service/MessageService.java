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
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MessageService implements MessageApi {
    /**
     * %s - owner number (string)
     * %s - time (instant)
     * %s - message (string)
     */
    private final static String MESSAGE_FORMAT = "%s;%s;%s";

    private final AppConfig appConfig;
    private final MessageRepository messageRepository;

    public Mono<Chat> findChat(String taskId, List<String> members, Boolean archived) {
        if(archived == null) {
            return messageRepository.findChatByTaskIdAndMembersIs(taskId, members)
                    .log()
                    .switchIfEmpty(Mono.empty());
        }

        return messageRepository.findChatByArchivedAndTaskIdAndMembersIs(archived, taskId, members)
                .log()
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<ChatDto> createOrFindChat(String taskId, String name, List<String> members) {
        return findChat(taskId, members, false)
                .switchIfEmpty(storeChat(taskId, name, members, null))
                .flatMap(chat -> Mono.just(convertToDto(chat, null)))
                .log()
                .switchIfEmpty(Mono.error(new RuntimeException("oops!")));
    }

    private Mono<Chat> storeChat(String taskId, String name, List<String> members, String previousChatCode) {
        String chatCode = previousChatCode == null ? java.util.UUID.randomUUID().toString() : previousChatCode;

        List<Chat.ReadMark> readMarkList = members.stream()
                .map(member -> Chat.ReadMark.builder().member(member).read(true).build())
                .collect(Collectors.toList());

        return messageRepository.save(Chat.builder()
                .name(name)
                .members(members)
                .taskId(taskId)
                .created(Instant.now())
                .messageCount(0)
                .archived(false)
                .readMarkList(readMarkList)
                .messages(new ArrayList<>())
                .chatCode(chatCode)
                .build());
    }

    @Override
    public Mono<ChatDto> updateChat(String chatCode, String authorId, String message) {
        return messageRepository.findByChatCodeAndArchived(chatCode, false)
                .log()
                .switchIfEmpty(Mono.error(new RuntimeException(String.format("oops! chat with params=[chatCode:%s] not found", chatCode))))
                .filter(chat -> chat.getMembers().contains(authorId))
                .switchIfEmpty(Mono.error(new RuntimeException(String.format("forbidden! author=[%s] not found in members this chat", authorId))))
                .flatMap(chat -> {
                    chat.getMessages().add(String.format(MESSAGE_FORMAT, authorId, Instant.now(), message));
                    chat.setMessageCount(chat.getMessageCount() + 1);

                    chat.getReadMarkList().forEach(readMark -> {
                        // if member not equals author = false
                        if(!readMark.getMember().equals(authorId)){
                            readMark.setRead(false);
                        }
                    });

                    if(chat.getMessageCount() >= appConfig.getLimitMessage()){
                        chat.setArchived(true);
                    }

                    log.info("prepared chat: {}", chat);
                    return messageRepository.save(chat);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("oops, something is wrong!")))
                .flatMap(chat -> {
                    if(chat.getArchived()) {
                        // if limit is over
                        return storeChat(chat.getTaskId(), chat.getName(), chat.getMembers(), chat.getChatCode())
                                // convert to dto
                                .flatMap(ch -> Mono.just(convertToDto(ch, authorId)))
                                .switchIfEmpty(Mono.error(new RuntimeException("oops!")));
                    }
                    return Mono.just(convertToDto(chat, authorId));
                });
    }

    @Override
    public Mono<Void> removeChat(String chatCode, String authorId) {
        return messageRepository.deleteChatByChatCodeAndMembersIs(chatCode, Collections.singletonList(authorId))
                .then();
    }

    @Override
    public Mono<ChatDto> markAsRead(String chatCode, String authorId) {
        return messageRepository.findByChatCodeAndArchived(chatCode, false)
                .switchIfEmpty(Mono.error(new RuntimeException(String.format("oops! chatCode with id=%s not found", chatCode))))
                .filter(chat -> chat.getMembers().contains(authorId))
                .switchIfEmpty(Mono.error(new RuntimeException("oops! transaction forbidden")))
                .log()
                .flatMap(chat -> {

                    chat.getReadMarkList().forEach(readMark -> {
                        if(readMark.getMember().equals(authorId)){
                            readMark.setRead(true);
                        }
                    });

                    return messageRepository.save(chat)
                            .log()
                            .flatMap(ch -> Mono.just(convertToDto(ch, authorId)))
                            .switchIfEmpty(Mono.empty());
                })
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<ChatDto> getMessages(String chatCode, String authorId) {
        return messageRepository.findChatByChatCode(chatCode)
                .switchIfEmpty(Mono.error(new RuntimeException(String.format("oops! chatCode with id=%s not found", chatCode))))
                .filter(chat -> chat.getMembers().contains(authorId))
                .switchIfEmpty(Mono.error(new RuntimeException("oops! transaction forbidden")))
                .log()
                .flatMap(chat -> {
                    //set read = true
                    chat.getReadMarkList().forEach(readMark -> {
                        if(readMark.getMember().equals(authorId)){
                            readMark.setRead(true);
                        }
                    });

                    log.info("updated chat: {}", chat);
                    return messageRepository.save(chat);
                })
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

                    ChatDto chatDto = convertToDto(chats.get(0), authorId);
                    chatDto.setMessages(convertTextToMessage(messageTextList));

                    log.info("chatDto: {}", chatDto);

                    return Mono.just(chatDto);
                })
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Flux<ChatDto> getChats(String authorId) {
        return messageRepository.findChatByArchivedAndMembersIn(false, Collections.singletonList(authorId))
                .log()
                .flatMap(chat -> Flux.just(convertToDto(chat, authorId)))
                .switchIfEmpty(Flux.empty());
    }

    private ChatDto convertToDto(Chat chat, String authorId){
        ChatDto.ReadMarkDto readMarkDto = null;
        if(authorId != null) {
            Chat.ReadMark rm = chat.getReadMarkList().stream()
                    .filter(readMark -> readMark.getMember().equals(authorId))
                    .findFirst().get();
            readMarkDto = new ChatDto.ReadMarkDto(rm.getRead(), rm.getMember());
        }

        return ChatDto.builder()
                .name(chat.getName())
                .chatCode(chat.getChatCode())
                .readMarkList(Collections.singletonList(readMarkDto))
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
