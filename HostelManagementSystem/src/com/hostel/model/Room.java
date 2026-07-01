package com.hostel.model;

public class Room {
    private int roomId, planId, capacity, occupiedCount, floorNumber;
    private String roomNumber;
    private boolean active;
    private String planName; // transient

    public Room() {}

    public int getRoomId() { return roomId; }
    public void setRoomId(int r) { this.roomId = r; }
    public int getPlanId() { return planId; }
    public void setPlanId(int p) { this.planId = p; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { this.capacity = c; }
    public int getOccupiedCount() { return occupiedCount; }
    public void setOccupiedCount(int o) { this.occupiedCount = o; }
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int f) { this.floorNumber = f; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String r) { this.roomNumber = r; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public String getPlanName() { return planName; }
    public void setPlanName(String p) { this.planName = p; }

    @Override public String toString() { return roomNumber + " [" + occupiedCount + "/" + capacity + "]"; }
}
