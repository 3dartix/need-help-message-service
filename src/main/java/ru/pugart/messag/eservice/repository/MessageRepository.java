package ru.pugart.messag.eservice.repository;

import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pugart.messag.eservice.entity.Chat;

public interface MessageRepository extends ReactiveElasticsearchRepository<Chat, String> {
    Mono<Chat> findAllByOwnerIdAndChatPartnerIdAndArchived(String ownerId, String chatPartnerId, Boolean archived);
    Flux<Chat> findAllByOwnerIdAndChatPartnerId(String ownerId, String chatPartnerId);
    Flux<Chat> findByOwnerIdOrChatPartnerId(String ownerId, String chatPartnerId);
}
