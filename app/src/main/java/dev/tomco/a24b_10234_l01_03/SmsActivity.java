package dev.tomco.a24b_10234_l01_03;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

public class SmsActivity extends AppCompatActivity {


    private MaterialButton sms_BTN_check_permissions;
    private MaterialButton sms_BTN_read_sms;
    private MaterialTextView sms_LBL_info;

    private ActivityResultLauncher<String> permissionLauncher
            = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    readSMS();
                } else {
                    boolean showDialog = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS);
                    if (showDialog) {
                        openPermissionDialog();
                    } else {
                        openAppInfo();
                    }
                }
            });

    private ActivityResultLauncher<Intent> manualPermissionLauncher
            = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                public void onActivityResult(ActivityResult result) {
                    readSMS();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        findViews();
        initViews();

    }

    private void initViews() {
        sms_BTN_check_permissions.setOnClickListener(v -> checkPermissions());
        sms_BTN_read_sms.setOnClickListener(v -> readSMS());

    }

    private void readSMS() {
        boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        if (isGranted) {
            sms_LBL_info.setText("My SMS List:");
        } else {
            boolean showDialog = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS);
            if (showDialog) {
                openPermissionDialog();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_SMS);
            }
        }

    }

    private void openPermissionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle("SMS Permission Required")
                .setMessage("This app requires SMS permission to read your SMS.\n " +
                        "Please grant the permission in app settings.\n" +
                        "App Info -> Permissions -> SMS -> Allow all the time")
                .setPositiveButton("To App Info", (dialog, which) -> openAppInfo())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAppInfo() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        manualPermissionLauncher.launch(intent);
    }

    private void checkPermissions() {
        boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean showDialog = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS);
        sms_LBL_info.setText("Granted = " + isGranted + "\nShow Dialog = " + showDialog);
    }

    private void findViews() {
        sms_BTN_check_permissions = findViewById(R.id.sms_BTN_check_permissions);
        sms_BTN_read_sms = findViewById(R.id.sms_BTN_read_sms);
        sms_LBL_info = findViewById(R.id.sms_LBL_info);
    }
}