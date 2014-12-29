package com.zmb.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A service that allows the SyncAdapter framework to interact with our
 * {@link com.zmb.sunshine.sync.DummyAuthenticator}.
 */
public class DummyAuthenticatorService extends Service {

    private DummyAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new DummyAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
