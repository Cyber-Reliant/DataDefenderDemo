package crc.DataDefender.DefenderDemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class ActivityReceiver implements Application.ActivityLifecycleCallbacks {

    Application app;
    public ActivityReceiver(Application application)
    {
        app = application;

        application.registerActivityLifecycleCallbacks(this);
    }
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.e("Activity test","onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.e("Activity test","onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.e("Activity test","onActivityResumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.e("Activity test","onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.e("Activity test","onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.e("Activity test","onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.e("Activity test","onActivityDestroyed");

        app.unregisterActivityLifecycleCallbacks(this);
    }
}
