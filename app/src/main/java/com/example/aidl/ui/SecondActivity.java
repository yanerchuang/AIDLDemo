package com.example.aidl.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.aidl.MessageReceiver;
import com.example.aidl.MessageSender;
import com.example.aidl.R;
import com.example.aidl.data.MessageModel;
import com.example.aidl.service.MessageService;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";
    private MessageSender messageSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        setupService();
    }

    public void sendEvent(View view) throws RemoteException {
        if (messageSender != null) {
//            messageSender.sendEvent(222);
            messageSender.sendStringEvent("SecondActivity's Event");
        }
    }
    /**
     * 1.unregisterListener
     * 2.unbindService
     */
    @Override
    protected void onDestroy() {
        //解除消息监听接口
        if (messageSender != null && messageSender.asBinder().isBinderAlive()) {
            try {
                messageSender.unregisterReceiveListener(messageReceiver);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(serviceConnection);
        super.onDestroy();
    }

    /**
     * bindService & startService：
     * 使用bindService方式，多个Client可以同时bind一个Service，但是当所有Client unbind后，Service会退出
     * 通常情况下，如果希望和Service交互，一般使用bindService方法，获取到onServiceConnected中的IBinder对象，和Service进行交互，
     * 不需要和Service交互的情况下，使用startService方法即可，Service主线程执行完成后会自动关闭；
     * unbind后Service仍保持运行，可以同时调用bindService和startService（比如像聊天软件，退出UI进程，Service仍能接收消息）
     */
    private void setupService() {
        Intent intent = new Intent(this, MessageService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * Binder可能会意外死忙（比如Service Crash），Client监听到Binder死忙后可以进行重连服务等操作
     */
    IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied");
            if (messageSender != null) {
                messageSender.asBinder().unlinkToDeath(this, 0);
                messageSender = null;
            }
            //// TODO: 2017/2/28 重连服务或其他操作
            setupService();
        }
    };

    //消息监听回调接口
    private MessageReceiver messageReceiver = new MessageReceiver.Stub() {

        @Override
        public void onMessageReceived(MessageModel receivedMessage) throws RemoteException {
            Log.d(TAG, "onMessageReceived: " + receivedMessage.toString());
        }

        @Override
        public void receiveEvent(int event) throws RemoteException {
            Log.d(TAG, "receiveEvent: " +event);

        }

        @Override
        public void receiveStringEvent(String event) throws RemoteException {
            Log.d(TAG, "receiveStringEvent: " +event);
        }
    };

    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //使用asInterface方法取得AIDL对应的操作接口
            messageSender = MessageSender.Stub.asInterface(service);

            //生成消息实体对象
            MessageModel messageModel = new MessageModel();
            messageModel.setFrom("client user id");
            messageModel.setTo("receiver user id");
            messageModel.setContent("This is message content");

            try {
                //设置Binder死亡监听
                messageSender.asBinder().linkToDeath(deathRecipient, 0);
                //把接收消息的回调接口注册到服务端
                messageSender.registerReceiveListener(messageReceiver);
                //调用远程Service的sendMessage方法，并传递消息实体对象
                messageSender.sendMessage(messageModel);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

}
