package com.keykiosk.Observer;


public interface CategoryObserver extends Observer {
    void updateCategories();

    @Override
    default void update() {
        updateCategories();
    }
}
