package com.example.walter.nursecaller_final;


public class Notification {
    //  @SerializedName("status")
    private String room_num;
    private String incomming;

    public Notification() {
    }

    public Notification(String room_numb) {
        this.room_num = room_numb;

    }

    public String getRoom_num() {
        return room_num;
    }

    public void setRoom_num(String room_num) {
        this.room_num = room_num;
    }
}
