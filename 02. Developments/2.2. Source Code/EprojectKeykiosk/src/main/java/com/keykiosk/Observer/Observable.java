package com.keykiosk.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class Observable<T> {
    private final List<T> observers = new ArrayList<>();

    public void addObserver(T observer) {
        observers.add(observer);
    }

    public void removeObserver(T observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Consumer<T> notification) {
        for (T observer : observers) {
            notification.accept(observer);
        }
    }
}
