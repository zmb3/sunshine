package com.zmb.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SunshineSyncService extends Service {

    private static final Object sLock = new Object();
    private static SunshineSyncAdapter sSyncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
