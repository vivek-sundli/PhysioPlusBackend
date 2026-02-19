package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);

    List<ChatMessage> findByConversationIdAndStatus(String conversationId, ChatMessage.MessageStatus status);

    List<ChatMessage> findBySenderIdAndStatus(String senderId, ChatMessage.MessageStatus status);

    long countByConversationIdAndRecipientIdAndStatusNot(
            String conversationId, String recipientId, ChatMessage.MessageStatus status);
}
