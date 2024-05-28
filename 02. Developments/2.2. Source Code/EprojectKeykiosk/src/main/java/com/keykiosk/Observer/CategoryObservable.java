package com.keykiosk.Observer;

import org.springframework.stereotype.Component;

@Component
public class CategoryObservable extends Observable<CategoryObserver> {
    public void notifyObservers() {
        notifyObservers(CategoryObserver::updateCategories);
    }
}
