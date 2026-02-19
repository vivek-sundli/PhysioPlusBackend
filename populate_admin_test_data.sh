#!/bin/bash

BASE_URL="http://localhost:8080/api/admin"

echo "Populating Admin Dashboard Test Data..."

# 1. Update App Settings
echo "1. Updating App Settings..."
curl -X PUT -H "Content-Type: application/json" -d '{
    "consultationFeeBase": 600.0,
    "platformFeePercentage": 12.5,
    "maintenanceMode": false,
    "allowedPaymentMethods": ["UPI", "CARD", "NET_BANKING"]
}' "$BASE_URL/settings"
echo -e "\nSettings updated."

# 2. Generate Activity Logs (Simulating various actions via logActivity implicitly if exposed, or trigger via other endpoints)
# Since logActivity is internal, we will trigger actions that cause logs.

# Trigger 2.1: Send Broadcast Notification (Logs: NOTIFICATION_SENT)
echo "2.1 Sending Broadcast Notification..."
curl -X POST -H "Content-Type: application/json" -d '{
    "title": "System Maintenance",
    "message": "Scheduled maintenance on Sunday at 10 PM.",
    "type": "BROADCAST",
    "targetGroup": "ALL"
}' "$BASE_URL/notifications/send-alert"
echo -e "\nBroadcast notification sent."

# Trigger 2.2: Send Group Notification to Doctors (Logs: NOTIFICATION_SENT)
echo "2.2 Sending Group Notification to Doctors..."
curl -X POST -H "Content-Type: application/json" -d '{
    "title": "New Policy Update",
    "message": "Please review the new consultation guidelines.",
    "type": "GROUP",
    "targetGroup": "DOCTORS"
}' "$BASE_URL/notifications/send-alert"
echo -e "\nGroup notification sent."

# Trigger 2.3: Send User Notification (Logs: NOTIFICATION_SENT)
echo "2.3 Sending Specific User Notification..."
curl -X POST -H "Content-Type: application/json" -d '{
    "title": "Welcome",
    "message": "Welcome to PhysioPlus Admin Panel testing.",
    "type": "USER",
    "targetUserId": "TEST_USER_001"
}' "$BASE_URL/notifications/send-alert"
echo -e "\nUser notification sent."

# Trigger 2.4: Block a Patient (Logs: USER_BLOCK)
# Note: This requires a valid ID. Usage: /api/admin/patients/{id}/block
# We will use a dummy ID 'PATIENT_TEST_BLOCK'. If the patient doesn't exist, it might fail or just log.
# Based on code, check if it throws exception if not found.
# The code does: patientRepository.findById(patientId).orElseThrow(...)
# So we need a real patient ID or we can't test this easily without creating a patient first.
# For now, let's skip blocking/unblocking/cancelling if we don't have known IDs, or we create one.

# Let's try to verify what we have so far.
echo "3. Verifying Activity Feed..."
curl "$BASE_URL/activity-feed"
echo -e "\nActivity Feed retrieved."

echo "Done. Please check the 'ActivityLogs', 'AppSettings', and 'NotificationLogs' collections in MongoDB."
