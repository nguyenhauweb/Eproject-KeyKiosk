package com.keykiosk.Observer;

public interface UserObserver extends Observer{
    void updateUser();

    @Override
    default void update() {
        updateUser();
    }
}
