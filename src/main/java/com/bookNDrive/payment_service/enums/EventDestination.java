package com.bookNDrive.payment_service.enums;

public enum EventDestination {

    SEND_COMMUNICATION("sendCommunication-out-0");

    private final String destination;

    EventDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }
}

