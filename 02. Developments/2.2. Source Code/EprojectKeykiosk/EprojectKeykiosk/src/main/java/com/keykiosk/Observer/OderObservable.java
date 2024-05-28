package com.keykiosk.Observer;

import org.springframework.stereotype.Component;

//public class OderObservable {
//}
@Component
public class OderObservable  extends Observable<OderObserver>{
    public void notifyUser() {
        notifyObservers(OderObserver::updateOder);
    }
}
