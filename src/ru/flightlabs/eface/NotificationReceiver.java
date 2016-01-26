package ru.flightlabs.eface;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class NotificationReceiver extends ResultReceiver {
    private Listener listener;

    public NotificationReceiver(Handler handler) {
        super(handler);
        // TODO Auto-generated constructor stub
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (listener != null)
            listener.onReceiveResult(resultCode, resultData);
    }

    public static interface Listener {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

}
