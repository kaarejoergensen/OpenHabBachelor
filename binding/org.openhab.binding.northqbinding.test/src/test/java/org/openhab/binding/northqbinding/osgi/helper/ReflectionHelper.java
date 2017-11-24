package org.openhab.binding.northqbinding.osgi.helper;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.network.QStickBridge;

public class ReflectionHelper {
    public static void installHttpClientMockAndAuthenticate(NorthQBridgeHandler bridgeHandler, MockedHttpClient mockedHttpClient) {
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

            assertTrue(bridgeHandler.onAuthenticationError());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail("Reflection error: " + e.getMessage());
        }
    }
}
