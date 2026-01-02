package com.sportygroup.service;

public interface LiveEventScheduler {

    void ensureScheduled(String eventId);

    void cancelIfScheduled(String eventId);

}
