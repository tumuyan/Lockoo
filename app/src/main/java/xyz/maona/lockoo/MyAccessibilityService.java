package xyz.maona.lockoo;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {

    private static MyAccessibilityService service;

    public static void lock() {
        if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service = null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
 /*       int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:

                //界面点击
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                //界面文字改动
                break;
        }*/
    }

    @Override
    public void onInterrupt() {
    }
}
