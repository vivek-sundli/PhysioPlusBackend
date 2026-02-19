package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.ActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {
    List<ActivityLog> findByActionType(String actionType);

    List<ActivityLog> findByActorId(String actorId);
}
