package crc.DataDefender.DefenderDemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class ScreenReceiver extends BroadcastReceiver{

    public static boolean wasScreenOn = true;
    Context context;
   public ScreenReceiver(Context cont)
   {
       context = cont;
       final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
       filter.addAction(Intent.ACTION_SCREEN_OFF);
       filter.addAction(Intent.ACTION_USER_PRESENT);
       filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
       filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
       filter.addDataScheme("package");
       context.registerReceiver(this, filter);
   }
    public void OnRemove()
    {
        context.unregisterReceiver(this);
    }
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("test","onReceive");
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

            String name = intent.getDataString();
             name = name.replaceFirst("package:","");

            Toast.makeText(context, "Package removed = " + name,
                    Toast.LENGTH_LONG).show();
            Log.e("test","wasScreenOn"+wasScreenOn);
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            wasScreenOn = false;
            Toast.makeText(context, "wasScreenOn"+wasScreenOn,
                    Toast.LENGTH_LONG).show();
            Log.e("test","wasScreenOn"+wasScreenOn);
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            wasScreenOn = true;
            Toast.makeText(context, "wasScreenOn"+wasScreenOn,
                    Toast.LENGTH_LONG).show();
            Log.e("test","wasScreenOn"+wasScreenOn);
        }
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            Log.e("test","userpresent");
            Toast.makeText(context, "userpresent",
                    Toast.LENGTH_LONG).show();
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Toast.makeText(context, "Booted",
                    Toast.LENGTH_LONG).show();
        }
    }


}
