/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.riolite.events;

/**
 * @author riolite
 */
public class EventSubscription {
    Object eventId;
    String callbackId;

    EventSubscription(Object eventId, String callbackId) {
        this.eventId = eventId;
        this.callbackId = callbackId;
    }

}
