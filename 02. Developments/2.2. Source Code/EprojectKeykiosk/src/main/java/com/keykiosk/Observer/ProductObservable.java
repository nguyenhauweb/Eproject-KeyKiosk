package com.keykiosk.Observer;

import org.springframework.stereotype.Component;

@Component
public class ProductObservable extends Observable<ProductObserver>{
    public void notifyProductObservers() {
        notifyObservers(ProductObserver::updateProduct);
    }
}
