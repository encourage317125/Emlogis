package com.emlogis.script.migration.data.legacy;

/**
 * Created by rjackson on 6/4/2015.
 */
public enum NotificationOptions {

    NOTIFICATIONS_ENABLED(1L),
    USE_CORPORATE_EMAIL_ADDRESS(32L),
    USE_PERSONAL_EMAIL_ADDRESS(64L),
    USE_SMS(128L);

    private NotificationOptions(long x) {
        this.bitMask = x;
    }

    private long bitMask = 0L;

    public long getBitMask( ) { return this.bitMask; }
}