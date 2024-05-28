package dev.tomco.a24b_10234_l01_03;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

public class LocationActivity extends AppCompatActivity {

    private MaterialTextView location_LBL_title;
    private MaterialTextView location_LBL_content;
    private ShapeableImageView location_IMG_image;
    private MaterialTextView location_LBL_Progress;
    private MaterialButton location_BTN_close;
    private MaterialButton location_BTN_grant;

    private STATE state = STATE.NA;

    private enum STATE {
        NA,
        NO_REGULAR_PERMISSION,
        NO_BACKGROUND_PERMISSION,
        LOCATION_DISABLED,
        LOCATION_SETTING_PROCESS,
        LOCATION_SETTINGS_OK
    }
    ActivityResultLauncher<Intent> appSettingsResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        checkStatus();
    });


    ActivityResultLauncher<String> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestPermission(), result -> {
                        if (result) {
                            // location access granted.
                            checkStatus();
                        } else {
                            // No location access granted.
                            if (shouldShowRequestPermissionRationale(checkPermissionsStatus(this))){
                                Snackbar.make(findViewById(
                                        android.R.id.content),
                                        R.string.permission_rationale,
                                        Snackbar.LENGTH_INDEFINITE)
                                        .setDuration(Snackbar.LENGTH_LONG)
                                        .setAction(R.string.settings, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                locationPermissionRequest.launch(checkPermissionsStatus(LocationActivity.this));
                                            }
                                        })
                                        .show();
                            }
                            else {
                                buildAlertMessageManuallyBackgroundPermission(
                                        checkPermissionsStatus(this)
                                );
                            }
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        findViews();
        initViews();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            checkStatus();
    }

    private void checkStatus() {
        String permissionStatus = checkPermissionsStatus(this);
        if (!isLocationEnabled(this))
            state = STATE.LOCATION_DISABLED;
        else if (permissionStatus != null)
            if (permissionStatus.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                state = STATE.NO_BACKGROUND_PERMISSION;
            else
                state = STATE.NO_REGULAR_PERMISSION;
        else {
            state = STATE.LOCATION_SETTING_PROCESS;
            validateLocationSensorsEnabled();
        }

        updateUI();
    }

    private void updateUI() {
        switch (state) {
            case NA:
                location_LBL_title.setText("NA");
                location_LBL_content.setText("NA");
                location_LBL_Progress.setText("0/0");
                location_BTN_close.setVisibility(View.INVISIBLE);
                location_BTN_grant.setVisibility(View.INVISIBLE);
                break;
            case NO_REGULAR_PERMISSION:
                location_LBL_title.setText("Location Permission");
                location_LBL_content.setText("Location permission is needed for core functionality.\nPlease Enable the app permission to access your location data");
                location_LBL_Progress.setText("2/4");
                location_BTN_close.setVisibility(View.VISIBLE);
                location_BTN_grant.setVisibility(View.VISIBLE);
                location_BTN_grant.setText("Grant Permission");
                location_BTN_grant.setOnClickListener(v -> askForLocationPermissions(checkPermissionsStatus(this)));
                break;
            case NO_BACKGROUND_PERMISSION:
                location_LBL_title.setText("Background location permission");
                location_LBL_content.setText("This app collects location data even when the app is closed or not in use.\nTo protect your privacy, the app stores only calculated indicators, like distance from home and never exact location.\nA notification is always displayed in the notifications bar when service is running.");
                location_LBL_Progress.setText("3/4");
                location_BTN_close.setVisibility(View.VISIBLE);
                location_BTN_grant.setVisibility(View.VISIBLE);
                location_BTN_grant.setText("Grant Permission");
                location_BTN_grant.setOnClickListener(v ->
                        askForLocationPermissions(checkPermissionsStatus(this)));
                break;
            case LOCATION_DISABLED:
                location_LBL_title.setText("Enable Location Services");
                location_LBL_content.setText("The app samples your location.\nPlease enable location services (GPS).");
                location_LBL_Progress.setText("1/4");
                location_BTN_close.setVisibility(View.VISIBLE);
                location_BTN_grant.setVisibility(View.VISIBLE);
                location_BTN_grant.setText("Turn On Location");
                location_BTN_grant.setOnClickListener(v -> {
                    enableLocationServiceProgramatically();
                });
                break;
            case LOCATION_SETTING_PROCESS:
                location_LBL_title.setText("LOCATION_SETTINGS_PROCCESS");
                location_LBL_content.setText("LOCATION_SETTINGS_PROCCESS");
                location_LBL_Progress.setText("4/4");
                location_BTN_close.setVisibility(View.INVISIBLE);
                location_BTN_grant.setVisibility(View.INVISIBLE);
                break;
            case LOCATION_SETTINGS_OK:
                location_LBL_title.setText("All Good! 👍🏻");
                location_LBL_content.setText("Location services are running and all permissions have been granted.\nYou can now start recording.");
                location_LBL_Progress.setText("4/4");
                location_BTN_close.setVisibility(View.INVISIBLE);
                location_BTN_grant.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void askForLocationPermissions(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                buildAlertMessageManuallyBackgroundPermission(permission);
            else
                locationPermissionRequest.launch(permission);
        }
        else
        {
            locationPermissionRequest.launch(permission);
        }
    }

    private void buildAlertMessageManuallyBackgroundPermission(String permission) {
        if (permission == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String allow_message_type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? "Allow all the time" : "Allow";

        builder.setMessage("You need to enable background location permission manually." +
                "\nOn the page that opens - click on PERMISSIONS, then on LOCATION and then check '" + allow_message_type + "'")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> openAppSettings())
                .setNegativeButton("Exit",(dialog, which) -> finish());
        builder.create().show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(),null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appSettingsResultLauncher.launch(intent);
    }

    private void enableLocationServiceProgramatically() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void validateLocationSensorsEnabled() {
        // check whether location setting are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        LocationRequest.Builder requestBuilder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY);
        builder.addLocationRequest(requestBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY).build())
                .addLocationRequest(requestBuilder.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build());
        builder.setNeedBle(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> {
                    state = STATE.LOCATION_SETTINGS_OK;
                    updateUI();
                })
                .addOnFailureListener(e -> Log.e("GPS", "Unable to execute request."))
                .addOnCanceledListener(() -> Log.e("GPS", "checkLocationSettings -> onCanceled"));
    }

    private boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            return mode != Settings.Secure.LOCATION_MODE_OFF;
        }
    }

    private String checkPermissionsStatus(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return Manifest.permission.ACCESS_FINE_LOCATION;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return Manifest.permission.ACCESS_COARSE_LOCATION;
        if (Build.VERSION.SDK_INT >= 29 && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        return null;
    }

    private void initViews() {
        location_BTN_close.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void findViews() {

        location_LBL_title = findViewById(R.id.location_LBL_title);
        location_LBL_content = findViewById(R.id.location_LBL_content);
        location_IMG_image = findViewById(R.id.location_IMG_image);
        location_LBL_Progress = findViewById(R.id.location_LBL_Progress);
        location_BTN_close = findViewById(R.id.location_BTN_close);
        location_BTN_grant = findViewById(R.id.location_BTN_grant);
    }
}