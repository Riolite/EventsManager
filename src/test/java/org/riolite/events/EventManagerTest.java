package org.riolite.events;

import io.vavr.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventManagerTest {
    private EventManager<String> eventManager;

    @BeforeEach
    void before() {
        eventManager = new EventManager<>();
    }

    @AfterEach
    void after() {
        eventManager.shutdown(1, TimeUnit.MINUTES).get();
    }

    @Test
    void givenEventListener_whenEventIsEmitted_thenListenerIsCalled() {
        //Given
        SubscriptionCallback<String> listener = mock(SubscriptionCallback.class);
        doNothing().when(listener).call(any());
        eventManager.subscribe("test", listener);

        //When
        Future<Boolean> emitDone = eventManager.emitEvent("test", "payload");

        //Then
        emitDone.andThen(emit -> verify(listener, times(1)).call(any()));
    }

    @Test
    void givenEventListenerSubscribeThenUnsubscribe_whenEventIsEmitted_thenListenerIsNotCalled() {
        //Given
        SubscriptionCallback<String> listener = mock(SubscriptionCallback.class);
        doNothing().when(listener).call(any());
        Future<EventSubscription> subscription = eventManager.subscribe("test", listener);
        eventManager.unsubscribe(subscription);

        //When
        Future<Boolean> emitDone = eventManager.emitEvent("test", "payload");

        //Then
        emitDone.andThen(emit -> verify(listener, never()).call(any()));
    }

    @Test
    void givenEventListenerThatThrowsException_whenEventIsEmitted_thenListenerEmitStatusIsFalse() {
        //Given
        SubscriptionCallback<String> listener = mock(SubscriptionCallback.class);
        doThrow(new RuntimeException()).when(listener).call(any());
        eventManager.subscribe("test", listener);

        //When
        Future<Boolean> emitDone = eventManager.emitEvent("test", "payload");

        //Then
        emitDone.andThen(emit -> assertEquals(emit.get(), false));
    }
}
