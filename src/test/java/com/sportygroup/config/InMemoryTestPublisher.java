package com.sportygroup.config;

import com.sportygroup.model.LiveEventMessage;
import com.sportygroup.publish.MessagePublisher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test publisher that captures messages in-memory for assertions.
 */
public class InMemoryTestPublisher implements MessagePublisher {

  private final CopyOnWriteArrayList<LiveEventMessage> messages = new CopyOnWriteArrayList<>();

  @Override
  public void publish(LiveEventMessage message) {
    messages.add(message);
  }

  public List<LiveEventMessage> getMessages() {
    return messages;
  }

  public void clear() {
    messages.clear();
  }
}
