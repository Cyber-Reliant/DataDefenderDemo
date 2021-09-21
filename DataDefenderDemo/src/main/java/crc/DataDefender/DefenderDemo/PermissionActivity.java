package crc.DataDefender.DefenderDemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * Created by wlady 8/20/20
 */
public class PermissionActivity extends Activity {

    public static boolean shouldRequestRuntimePermission() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] perms = null;
        boolean needsPermRequest = false;
        if (shouldRequestRuntimePermission()) {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                needsPermRequest = true;
                perms = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
            }

            if (ActivityCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                if (!needsPermRequest) {
                    perms = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
                } else {
                    perms = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
                }
                needsPermRequest = true;
            }
        }

        if (needsPermRequest) {
            int permsRequestCode = 200;
            requestPermissions(perms, permsRequestCode);
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }
    }
}
