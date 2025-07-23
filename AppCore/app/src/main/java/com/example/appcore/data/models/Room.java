package com.example.appcore.data.models;

public class Room {
    private String roomId;
    private String roomName;
    private int roomType;
    private int seatCount;

    public Room() {
    }

    public Room(String roomId, String roomName, int roomType, int seatCount) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.seatCount = seatCount;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }
}
