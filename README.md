# Notification subscription and initial data replay handling for the MLS ecosystem

This project contains the modules for subscribing to notifications and receiving replayed initial data on subscription.

**CAVE: This is still somewhere between experimental and testing.**
It works well so far, but it needs thorough testing until production readiness.

TODO: Extend this documentation

## Modules

The project contains four modules:

* `eventbus` contains a lightweight event bus implementation for an event bus using worker queues.
  It is used for publishing notification messages to the message broker and handling replay requests, but can also be used for any other
  events.
* `replay-common` includes some common declarations shared by publishers and subscribers.
* `replay-service` includes the base implementation for publishers.
  Services need to provide implementations of `AbstractReplayListener` and `AbstractReplayHandler` and can send notifications by publishing
  `NotificationMessage` events to the event bus.
* `stomp-endpoint` is a microservice that provides a STOMP endpoint which then forwards subscriptions to the message broker and requests the
  initial data from the replaying services.
  It can be (built and) started using `docker-compose build` and `docker-compose up` (requires the services from the orchestration project).
