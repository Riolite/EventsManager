/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.riolite.events;

/**
 * @author riolite
 */
@FunctionalInterface
public interface SubscriptionCallback<T> {
    /**
     * This function shouldn't have any blocking calls or any heavy computation.
     *
     * @param eventPayload - payload
     */
    void call(T eventPayload);
}
