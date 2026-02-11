package com.healthclub.Physioplus.Dto;

import com.healthclub.Physioplus.Model.VideoSession;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponse {

    private boolean success;
    private String message;
    private VideoSession session;

    public RoomResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public RoomResponse(boolean success, String message, VideoSession session) {
        this.success = success;
        this.message = message;
        this.session = session;
    }

    public static RoomResponse success(String message, VideoSession session) {
        return new RoomResponse(true, message, session);
    }

    public static RoomResponse error(String message) {
        return new RoomResponse(false, message);
    }
}
