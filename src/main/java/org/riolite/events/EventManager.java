package org.riolite.events;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Eslam El-Shinawy
 */

public class EventManager<T> {

    private final HashMap<Object, List<SubscriptionCallback<T>>> eventsSubscriptions = new HashMap<>();
    private final ScheduledThreadPoolExecutor eventsQueue = new ScheduledThreadPoolExecutor(1);

    /**
     * Calls listeners that have subscribed and haven't unsubscribed yet.
     * @param eventId - The eventId to emit.
     * @param payload - the payload that will be passed to subscribed listeners.
     * @return a Future containing the status of the execution of all listeners. Any listener that fails to execute will cause a failure (false).
     */
    public Future<Boolean> emitEvent(Object eventId, T payload) {
        return Future.of(eventsQueue, () ->
                Option.of(eventsSubscriptions.get(eventId))
                        .peek(subs -> subs.forEach(i -> i.call(payload)))
                        .map(subs -> true)
                        .getOrElse(false));
    }


    /**
     * Attaches a listener to an event.
     * @param eventId - The eventId to subscribe to.
     * @param subscriptionCallback - the listener function.
     * @return A Future containing object used for unsubscribing the attached listener from the event.
     */
    public Future<EventSubscription> subscribe(Object eventId, SubscriptionCallback<T> subscriptionCallback) {
        return Future.of(eventsQueue, () ->
                Option.of(eventsSubscriptions.get(eventId))
                        .onEmpty(() -> eventsSubscriptions.put(eventId, new ArrayList<>(Collections.singletonList(subscriptionCallback))))
                        .peek(subs -> subs.add(subscriptionCallback))
                        .transform(option -> new EventSubscription(eventId, subscriptionCallback.toString()))
        );
    }

    /**
     * Detaches a listener to an event.
     * @param eventSubscription - A Future containing subscription metadata.
     * @return A Future for un-subscription operation.
     */
    public Future<Boolean> unsubscribe(Future<EventSubscription> eventSubscription) {
        return Future.of(eventsQueue, () -> Option.of(eventSubscription.get())
                .peek(sub ->
                        Option.of(eventsSubscriptions.get(sub.eventId))
                                .peek(subs -> subs.removeIf(element -> element.toString().equals(sub.callbackId)))
                                .filter(List::isEmpty)
                                .peek(subs -> eventsSubscriptions.remove(sub.eventId))
                )
                .map(sub -> true)
                .getOrElse(false));
    }

    /**
     * Shuts down this EventsHandler without waiting.
     */
    public void shutdown() {
        eventsQueue.shutdown();
    }
    /**
     * Shuts down this EventsHandler and waits for it to shut down.
     * @param timeout - the time to wait for shutdown to complete.
     * @param timeUnit - the time unit for timeout parameter.
     * @return The result of the waiting operation.
     */
    public Try<Boolean> shutdown(long timeout, TimeUnit timeUnit) {
        eventsQueue.shutdown();
        return Try.of(() -> eventsQueue.awaitTermination(timeout, timeUnit));
    }
}
