package com.keykiosk.Observer;

//public class OderObserver {
//}
public interface OderObserver extends Observer{
    void updateOder();

    @Override
    default void update() {
        updateOder();
    }
}