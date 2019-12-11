package xyz.maona.lockoo;

import android.app.ActivityManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class ScreenOffService extends Service {
    public ScreenOffService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        boolean advanced = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
        boolean meizu = android.os.Build.BRAND.toLowerCase().equals("meizu") &&
                android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
        if (advanced && !meizu)
            lockScreenOverP();
        else lockScreen();
        Log.w("ScreenOffService","onStartCommand");
        return super.onStartCommand( intent,  flags,  startId);
    }

    private void lockScreen() {
        ComponentName componentName = new ComponentName(this, android.app.admin.DeviceAdminReceiver.class);
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (policyManager == null) return;
        if (policyManager.isAdminActive(componentName)) {
            policyManager.lockNow();
        }
    }

    private void lockScreenOverP() {
        boolean settingOn = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(1000))
            if (service.service.getClassName().equals(MyAccessibilityService.class.getName()))
                settingOn = true;
        if (settingOn) {
            MyAccessibilityService.lock();
        }
    }
}
