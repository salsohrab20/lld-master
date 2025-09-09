package com.practice.airlinemanagementsystem;

public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notifyBooking(Booking booking, String message) {
        System.out.println("[NOTIFY] Booking "+ booking.getId() + "->"+ message);
    }
}
