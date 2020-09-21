package com.example.aidl;

/**
 * @author: YOULU  ywj
 * date: 2020/9/21 16:06
 * desc:
 */
public class EventBus {

    private static volatile EventBus mInstance;

    private EventBus() {
    }

    public static EventBus getInstance() {
        if (mInstance == null) {
            synchronized (EventBus.class) {
                if (mInstance == null) {
                    mInstance = new EventBus();
                }
            }
        }
        return mInstance;
    }



}