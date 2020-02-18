package com.dji.ImportSDKDemo;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private static Application app = null;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        app = this;
    }

    public static Application getInstance() {
        return MApplication.app;
    }

}
