package com.example.vida.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Appointment {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String date;
    private String time;
    private String status;

    public Appointment() {
    }

    public Appointment(String id, String name, String email, String phone, String date, String time, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.status = status;
    }

}
