package com.sportygroup.service;

public interface EventLifecycleService {

    void markLive(String eventId);

    void markNotLive(String eventId);

}
