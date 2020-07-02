package xyz.maona.lockoo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private EditText text_min;
    private SeekBar seekBar_min;
    private ToggleButton toggle;
    private int set_min=1;
    private boolean modeP=false;
    private PendingIntent alarmIntent;
    private Intent intent;
    private  ScreenListener l;
//    private AlarmManager alarmManager;

    protected void onCreate(Bundle state) {
        super.onCreate(state);
        boolean advanced = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
        boolean meizu = android.os.Build.BRAND.toLowerCase().equals("meizu") &&
                android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
        modeP=(advanced && !meizu);

        setContentView(R.layout.setting_layout);

        text_min=(EditText)findViewById(R.id.editText4) ;
        seekBar_min=(SeekBar)findViewById(R.id.seekBar4);

        seekBar_min.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                set_min=progress;
                if(set_min==0)
                    set_min=1;
                text_min.setText(""+set_min);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.Test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startService(intent);
                if (modeP)
                    lockScreenOverP();
                else lockScreen();
                finish();
            }
        });

        findViewById(R.id.Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_min=Integer.parseInt(text_min.getText().toString());
                if(set_min>seekBar_min.getMax()){
                    Log.i("save","setting error, input = "+set_min+", max="+seekBar_min.getMax());
                    set_min=seekBar_min.getMax();
                }
                if(set_min<1){
                    set_min=1;
                    text_min.setText(set_min);
                }

                seekBar_min.setProgress(set_min);

                SharedPreferences.Editor editor = getSharedPreferences("lock",MODE_PRIVATE).edit();
                editor.putInt("min",set_min);
                editor.commit();
                Log.i("save","setting min = "+set_min);
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_min =(set_min+5)/5*5;
                if(set_min>seekBar_min.getMax())
                    set_min=1;
                seekBar_min.setProgress(set_min);
            }
        });


        set_min = getSharedPreferences("lock",MODE_PRIVATE).getInt("min",36) ;
        seekBar_min.setProgress(set_min);



        l = new ScreenListener(this);

        toggle=(ToggleButton)findViewById(R.id.toggleButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggle.isChecked()){
                    user_setting(modeP);
                    set_alarm();
                    l.begin(new ScreenListener.ScreenStateListener() {
                        @Override
                        public void onUserPresent() {
                            Log.e("onUserPresent", "onUserPresent");
                            set_alarm();
                        }

                        @Override
                        public void onScreenOn() {
                            Log.e("onScreenOn", "onScreenOn");
                        }

                        @Override
                        public void onScreenOff() {
                            Log.e("onScreenOff", "onScreenOff");
                            release_alarm();
                        }
                    });
                }else {
                    l.unregisterListener();
                }

            }
        });



        intent = new Intent(this, ScreenOffService.class);
            alarmIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


    }
    private void release_alarm(){
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
    }
    private void set_alarm(){
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.cancel(alarmIntent);

        int time = 60000 * set_min;//30分钟
        if (Build.VERSION.SDK_INT < 19) {
            alarmManager.set(AlarmManager.RTC_WAKEUP , System.currentTimeMillis() + time, alarmIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP , System.currentTimeMillis() + time, alarmIntent);
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(l!=null)
            toggle.setChecked(l.isWorking());
    }



    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
/*        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            System.out.println("您按了菜单键");
            CreatAlertDialog("您按了菜单键");
            Toast.makeText(this, "您按了菜单键", Toast.LENGTH_SHORT).show();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            // 由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            System.out.println("您按了HOME键");
            CreatAlertDialog("您按了HOME键");
            Toast.makeText(this, "您按了HOME键", Toast.LENGTH_SHORT).show();
            return true;
        }*/
        return super.onKeyDown(keyCode, event);
    }


    // 引导用户进入系统设置
    private void user_setting(boolean p){
        if(p){
            boolean settingOn = false;
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(1000))
                if (service.service.getClassName().equals(MyAccessibilityService.class.getName()))
                    settingOn = true;
            if (!settingOn) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }else{
            ComponentName componentName = new ComponentName(this, android.app.admin.DeviceAdminReceiver.class);
            DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (policyManager == null) return;
            if (!policyManager.isAdminActive(componentName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.app_name));
                startActivity(intent);
            }
        }
    }

    private void lockScreen() {
        ComponentName componentName = new ComponentName(this, android.app.admin.DeviceAdminReceiver.class);
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (policyManager == null) return;
        if (!policyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.app_name));
            startActivity(intent);
        } else policyManager.lockNow();
    }

    private void lockScreenOverP() {
        boolean settingOn = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(1000))
            if (service.service.getClassName().equals(MyAccessibilityService.class.getName()))
                settingOn = true;
        if (settingOn) {
            MyAccessibilityService.lock();
        } else startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }
}
