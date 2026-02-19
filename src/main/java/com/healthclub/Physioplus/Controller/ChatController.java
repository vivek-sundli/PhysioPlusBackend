package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.PageResponse;
import com.healthclub.Physioplus.Model.ChatMessage;
import com.healthclub.Physioplus.Model.Conversation;
import com.healthclub.Physioplus.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Real-time messaging APIs")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ==================== Conversations ====================

    @PostMapping("/conversations")
    @Operation(summary = "Create or get existing conversation")
    public ResponseEntity<Conversation> createConversation(@RequestBody Map<String, String> request) {
        Conversation conversation = chatService.createConversation(
                request.get("doctorId"),
                request.get("patientId"),
                request.get("bookingId")
        );
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Get conversation by ID")
    public ResponseEntity<Conversation> getConversation(@PathVariable String conversationId) {
        return chatService.getConversation(conversationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/conversations/user/{userId}")
    @Operation(summary = "Get all conversations for a user")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable String userId) {
        return ResponseEntity.ok(chatService.getConversationsForUser(userId));
    }

    @GetMapping("/conversations/between")
    @Operation(summary = "Get conversation between two participants")
    public ResponseEntity<Conversation> getConversationByParticipants(
            @RequestParam String participant1,
            @RequestParam String participant2) {
        return chatService.getConversationByParticipants(participant1, participant2)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/conversations/{conversationId}/archive")
    @Operation(summary = "Archive a conversation")
    public ResponseEntity<Conversation> archiveConversation(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatService.archiveConversation(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/mute")
    @Operation(summary = "Mute/unmute a conversation for a participant")
    public ResponseEntity<Conversation> muteConversation(
            @PathVariable String conversationId,
            @RequestParam String participantId,
            @RequestParam boolean mute) {
        return ResponseEntity.ok(chatService.muteConversation(conversationId, participantId, mute));
    }

    // ==================== Messages ====================

    @PostMapping("/messages")
    @Operation(summary = "Send a new message")
    public ResponseEntity<ChatMessage> sendMessage(@Valid @RequestBody ChatMessage message) {
        return ResponseEntity.ok(chatService.sendMessage(message));
    }

    @GetMapping("/messages/{conversationId}")
    @Operation(summary = "Get messages in a conversation with pagination")
    public ResponseEntity<PageResponse<ChatMessage>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, page, size));
    }

    @PostMapping("/messages/{messageId}/delivered")
    @Operation(summary = "Mark message as delivered")
    public ResponseEntity<ChatMessage> markAsDelivered(@PathVariable String messageId) {
        return ResponseEntity.ok(chatService.markAsDelivered(messageId));
    }

    @PostMapping("/messages/{messageId}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ChatMessage> markAsRead(@PathVariable String messageId) {
        return ResponseEntity.ok(chatService.markAsRead(messageId));
    }

    @PostMapping("/conversations/{conversationId}/read-all")
    @Operation(summary = "Mark all messages as read for a participant")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable String conversationId,
            @RequestParam String participantId) {
        chatService.markAllAsRead(conversationId, participantId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete a message (sender only)")
    public ResponseEntity<ChatMessage> deleteMessage(
            @PathVariable String messageId,
            @RequestParam String userId) {
        return chatService.deleteMessage(messageId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/messages/{messageId}")
    @Operation(summary = "Edit a message (sender only)")
    public ResponseEntity<ChatMessage> editMessage(
            @PathVariable String messageId,
            @RequestParam String userId,
            @RequestBody Map<String, String> request) {
        return chatService.editMessage(messageId, userId, request.get("content"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // ==================== Unread Count ====================

    @GetMapping("/conversations/{conversationId}/unread")
    @Operation(summary = "Get unread message count for a participant")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @PathVariable String conversationId,
            @RequestParam String participantId) {
        long count = chatService.getUnreadCount(conversationId, participantId);
        return ResponseEntity.ok(Map.of(
                "conversationId", conversationId,
                "participantId", participantId,
                "unreadCount", count
        ));
    }
}
