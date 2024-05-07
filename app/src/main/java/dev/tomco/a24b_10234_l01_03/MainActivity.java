package dev.tomco.a24b_10234_l01_03;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class MainActivity extends AppCompatActivity {

    public static final int CONTACT_PERMISSIONS = 345345;
    public static final int CONTACT_READ_PERMISSIONS = 123123;
    public static final int CONTACT_COUNT_PERMISSIONS = 234234;
    private MaterialButton main_BTN_grant_permissions;
    private MaterialButton main_BTN_read_contacts;
    private MaterialButton main_BTN_count_contacts;
    private MaterialTextView main_LBL_info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });

        findViews();
        initViews();
    }

    private void initViews() {
        main_BTN_grant_permissions.setOnClickListener(v -> grantPermissions());
        main_BTN_grant_permissions.setVisibility(View.GONE);
        main_BTN_count_contacts.setOnClickListener(v -> countContactsClicked());
        main_BTN_read_contacts.setOnClickListener(v -> readContactsClicked());
    }

    private void readContactsClicked() {
        boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        if (isGranted){
            readContacts();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS},CONTACT_READ_PERMISSIONS);
        }
    }

    private void countContactsClicked() {
        boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        if (isGranted) {
            countContacts();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS},CONTACT_COUNT_PERMISSIONS);
        }
    }
        private void   readContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String data= "";
        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.d(name + "(phone number)", phoneNumber);
                        data += "\n" + name + "(phone number): " + phoneNumber;
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null)
            cur.close();

        main_LBL_info.setText("Contacts:\n" + data);
    }

    private void countContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int counter = 0;
        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.d(name + "(phone number)", phoneNumber);
                        counter++;
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null)
            cur.close();

        main_LBL_info.setText("The number of phone numbers stored is: " + counter);
    }

    private void grantPermissions() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{"android.permission.READ_CONTACTS"},
                CONTACT_PERMISSIONS);
    }

    private void findViews() {
        main_BTN_grant_permissions = findViewById(R.id.main_BTN_grant_permissions);
        main_BTN_read_contacts = findViewById(R.id.main_BTN_read_contacts);
        main_BTN_count_contacts = findViewById(R.id.main_BTN_count_contacts);
        main_LBL_info = findViewById(R.id.main_LBL_info);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CONTACT_READ_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readContacts();
                }
                else {
                    Log.d("Permission Denied!", "readContacts Denied!");
                }
                break;

                case CONTACT_COUNT_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    countContacts();
                }
                else {
                    Log.d("Permission Denied!", "countContacts Denied!");
                }
                break;
        }
    }
}