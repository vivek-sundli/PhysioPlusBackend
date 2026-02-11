package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findByUserId(String userId);

    List<NotificationLog> findByBookingId(String bookingId);

    Optional<NotificationLog> findByMessageId(String messageId);

    List<NotificationLog> findByStatus(NotificationLog.DeliveryStatus status);

    List<NotificationLog> findByRecipientPhone(String recipientPhone);

    List<NotificationLog> findByUserIdAndType(String userId, NotificationLog.NotificationType type);
}
