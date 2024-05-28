package com.keykiosk.Observer;


import org.springframework.stereotype.Component;

@Component
public class PermissionObservable extends Observable<PermissionObserver> {
    public void notifyObservers() {
        notifyObservers(PermissionObserver::updatePermissions);
    }
}
