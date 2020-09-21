// MessageReceiver.aidl
package com.example.aidl;
import com.example.aidl.data.MessageModel;


interface MessageReceiver {
    void onMessageReceived(in MessageModel receivedMessage);
    void receiveEvent(int event);
    void receiveStringEvent(String event);
}
