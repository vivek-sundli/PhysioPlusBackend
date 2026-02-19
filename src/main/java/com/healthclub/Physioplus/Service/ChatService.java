package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.ChatMessage;
import com.healthclub.Physioplus.Model.Conversation;
import com.healthclub.Physioplus.Repository.ChatMessageRepository;
import com.healthclub.Physioplus.Repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Autowired
    public ChatService(ChatMessageRepository messageRepository,
                       ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    // ==================== Conversation Management ====================

    public Conversation createConversation(String doctorId, String patientId, String bookingId) {
        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findByDoctorIdAndPatientId(doctorId, patientId);
        if (existing.isPresent()) {
            log.debug("Conversation already exists between doctor {} and patient {}", doctorId, patientId);
            return existing.get();
        }

        Conversation conversation = new Conversation();
        conversation.setDoctorId(doctorId);
        conversation.setPatientId(patientId);
        conversation.setBookingId(bookingId);
        conversation.setParticipant1Id(patientId);
        conversation.setParticipant1Type("PATIENT");
        conversation.setParticipant2Id(doctorId);
        conversation.setParticipant2Type("DOCTOR");
        conversation.setParticipantIds(List.of(patientId, doctorId));
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());

        log.info("Creating conversation between doctor {} and patient {}", doctorId, patientId);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Optional<Conversation> getConversation(String conversationId) {
        return conversationRepository.findById(conversationId);
    }

    @Transactional(readOnly = true)
    public Optional<Conversation> getConversationByParticipants(String participantId1, String participantId2) {
        return conversationRepository.findByBothParticipants(participantId1, participantId2);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getConversationsForUser(String userId) {
        return conversationRepository.findByParticipant1IdOrParticipant2Id(userId, userId);
    }

    public Conversation archiveConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conversation -> {
                    conversation.setArchived(true);
                    conversation.setUpdatedAt(Instant.now());
                    return conversationRepository.save(conversation);
                })
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public Conversation muteConversation(String conversationId, String participantId, boolean mute) {
        return conversationRepository.findById(conversationId)
                .map(conversation -> {
                    if (participantId.equals(conversation.getParticipant1Id())) {
                        conversation.setMuted1(mute);
                    } else if (participantId.equals(conversation.getParticipant2Id())) {
                        conversation.setMuted2(mute);
                    }
                    conversation.setUpdatedAt(Instant.now());
                    return conversationRepository.save(conversation);
                })
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    // ==================== Message Management ====================

    public ChatMessage sendMessage(ChatMessage message) {
        message.setSentAt(Instant.now());
        message.setStatus(ChatMessage.MessageStatus.SENT);

        ChatMessage savedMessage = messageRepository.save(message);

        // Update conversation with last message info
        conversationRepository.findById(message.getConversationId())
                .ifPresent(conversation -> {
                    conversation.setLastMessageId(savedMessage.getId());
                    conversation.setLastMessagePreview(truncateMessage(message.getContent(), 50));
                    conversation.setLastMessageAt(Instant.now());
                    conversation.setLastMessageSenderId(message.getSenderId());

                    // Update unread count
                    if (message.getSenderId().equals(conversation.getParticipant1Id())) {
                        conversation.setUnreadCount2(conversation.getUnreadCount2() + 1);
                    } else {
                        conversation.setUnreadCount1(conversation.getUnreadCount1() + 1);
                    }

                    conversation.setUpdatedAt(Instant.now());
                    conversationRepository.save(conversation);
                });

        log.info("Message sent in conversation {} from {}", message.getConversationId(), message.getSenderId());
        return savedMessage;
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessage> getMessages(String conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<ChatMessage> messages = messageRepository.findByConversationIdOrderBySentAtDesc(conversationId, pageable);
        return PageResponse.of(messages);
    }

    public ChatMessage markAsDelivered(String messageId) {
        return messageRepository.findById(messageId)
                .map(message -> {
                    message.setStatus(ChatMessage.MessageStatus.DELIVERED);
                    message.setDeliveredAt(Instant.now());
                    return messageRepository.save(message);
                })
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public ChatMessage markAsRead(String messageId) {
        return messageRepository.findById(messageId)
                .map(message -> {
                    message.setStatus(ChatMessage.MessageStatus.READ);
                    message.setReadAt(Instant.now());
                    return messageRepository.save(message);
                })
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public void markAllAsRead(String conversationId, String participantId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Reset unread count for the participant
        if (participantId.equals(conversation.getParticipant1Id())) {
            conversation.setUnreadCount1(0);
        } else if (participantId.equals(conversation.getParticipant2Id())) {
            conversation.setUnreadCount2(0);
        }

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        log.info("Marked all messages as read for {} in conversation {}", participantId, conversationId);
    }

    public Optional<ChatMessage> deleteMessage(String messageId, String userId) {
        return messageRepository.findById(messageId)
                .filter(message -> message.getSenderId().equals(userId))
                .map(message -> {
                    message.setContent("[Message deleted]");
                    message.setStatus(ChatMessage.MessageStatus.FAILED); // Mark as failed/deleted
                    return messageRepository.save(message);
                });
    }

    public Optional<ChatMessage> editMessage(String messageId, String userId, String newContent) {
        return messageRepository.findById(messageId)
                .filter(message -> message.getSenderId().equals(userId))
                .map(message -> {
                    message.setContent(newContent);
                    message.setEdited(true);
                    message.setEditedAt(Instant.now());
                    return messageRepository.save(message);
                });
    }

    // ==================== Utilities ====================

    private String truncateMessage(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength - 3) + "...";
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String conversationId, String participantId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (participantId.equals(conversation.getParticipant1Id())) {
            return conversation.getUnreadCount1();
        } else if (participantId.equals(conversation.getParticipant2Id())) {
            return conversation.getUnreadCount2();
        }
        return 0;
    }
}
