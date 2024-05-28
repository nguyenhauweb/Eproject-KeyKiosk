package com.keykiosk.Observer;


public interface PermissionObserver extends Observer {
    void updatePermissions();

    @Override
    default void update() {
        updatePermissions();
    }
}
