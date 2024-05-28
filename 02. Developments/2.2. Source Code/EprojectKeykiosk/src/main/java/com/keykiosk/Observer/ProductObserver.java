package com.keykiosk.Observer;


public interface ProductObserver extends Observer {
    void updateProduct();

    @Override
    default void update() {
        updateProduct();
    }
}

