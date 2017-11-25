/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.osgi.helper;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.network.QStickBridge;

/**
 * Helper class for replacing instances of HttpClient in binding.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class ReflectionHelper {
    public static void installHttpClientMock(NorthQBridgeHandler bridgeHandler, MockedHttpClient mockedHttpClient) {
        try {
            Field qStickBridgeField = NorthQBridgeHandler.class.getDeclaredField("qStickBridge");
            qStickBridgeField.setAccessible(true);
            Object qStickBridgeValue = qStickBridgeField.get(bridgeHandler);
            if (qStickBridgeValue == null) {
                qStickBridgeField.set(bridgeHandler, new QStickBridge());
            }
            assertThat(qStickBridgeValue, is(notNullValue()));

            Field httpClientField = QStickBridge.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(qStickBridgeValue, mockedHttpClient);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail("Reflection error: " + e.getMessage());
        }
    }
}
