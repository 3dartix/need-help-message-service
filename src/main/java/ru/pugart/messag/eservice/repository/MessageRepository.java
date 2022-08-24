package ru.pugart.messag.eservice.repository;

import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.entity.Chat;

import java.util.List;

public interface MessageRepository extends ReactiveElasticsearchRepository<Chat, String> {
    Mono<Chat> findChatByArchivedAndTaskIdAndMembersIs(Boolean archived, String taskId, List<String> members);
    Mono<Chat> findChatByTaskIdAndMembersIs(String taskId, List<String> members);
    Mono<Void> deleteChatByChatCodeAndMembersIs(String chatCode, List<String> members);
    Flux<Chat> findChatByChatCode(String chatCode);
    Flux<Chat> findChatByArchivedAndMembersIn(Boolean archived, List<String> members);
    Mono<Chat> findByChatCodeAndArchived(String chatCode, Boolean archived);
}
