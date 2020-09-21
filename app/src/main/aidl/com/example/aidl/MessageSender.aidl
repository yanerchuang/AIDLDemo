// MessageSender.aidl
package com.example.aidl;
import com.example.aidl.data.MessageModel;
import com.example.aidl.MessageReceiver;

interface MessageSender {
    void sendMessage(in MessageModel messageModel);
    void sendEvent(int event);

    void registerReceiveListener(MessageReceiver messageReceiver);

    void unregisterReceiveListener(MessageReceiver messageReceiver);
}
