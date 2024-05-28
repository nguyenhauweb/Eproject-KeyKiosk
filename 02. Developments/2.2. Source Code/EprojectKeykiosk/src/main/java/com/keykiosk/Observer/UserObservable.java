package com.keykiosk.Observer;

import org.springframework.stereotype.Component;

@Component
public class UserObservable  extends Observable<UserObserver>{
    public void notifyUser() {
        notifyObservers(UserObserver::updateUser);
    }
}
