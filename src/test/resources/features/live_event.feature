Feature: Live event scheduling

  Scenario: Marking an event LIVE triggers periodic external calls and message publication
    Given the service is running
    And the external API returns a score for event "999"
    When I mark event "999" as LIVE
    Then a message should eventually be published for event "999"

  Scenario: Marking an event NOT_LIVE stops publishing
    Given the service is running
    And the external API returns a score for event "888"
    When I mark event "888" as LIVE
    Then a message should eventually be published for event "888"
    When I mark event "888" as NOT_LIVE
    Then no new messages should be published for event "888" within 1 second
