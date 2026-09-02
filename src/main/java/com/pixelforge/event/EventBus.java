package com.pixelforge.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {

    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        List<Consumer<?>> list = listeners.get(event.getClass());
        if (list != null) {
            for (Consumer<?> consumer : list) {
                ((Consumer<T>) consumer).accept(event);
            }
        }
    }

    public void clear() {
        listeners.clear();
    }
}
