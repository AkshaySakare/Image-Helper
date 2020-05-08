package com.sakareps.myapplicationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

// BY APS 8/5/2020
// Function-   selection of the image & move to the vsm folder & change name according to the user
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE = 1;
    private Button mBtAdhar, mBtPancard; // Button
    private String mSelectionType;// type of the selection
    private EditText mEtName;//
    private String TAG = "MainActivity";

    private static final int STORAGE_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdhar = (Button) findViewById(R.id.iv_adharcard);
        mBtPancard = (Button) findViewById(R.id.iv_pancard);
        mEtName = (EditText) findViewById(R.id.et_username);
        mBtPancard.setOnClickListener(this);
        mBtAdhar.setOnClickListener(this);
        checkPermission(

                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);

}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_adharcard:
                mSelectionType = getResources().getString(R.string.aadharcard);//AadharCard
                if (!mEtName.getText().toString().isEmpty()) {
                    selectImage();
                } else {
                    Toast.makeText(MainActivity.this,getResources().getString(R.string.no_userName), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.iv_pancard:
                mSelectionType = getResources().getString(R.string.pancard);//pancard
                if (!mEtName.getText().toString().isEmpty()) {
                    selectImage();
                } else {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.no_userName), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // an error
                return;
            }

            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + getResources().getString(R.string.vsm));
            String destination = Environment.getExternalStorageDirectory() +
                    File.separator + getResources().getString(R.string.vsm);
            boolean success = true;
          // Check the folder is created or not
            if (!folder.exists()) {
                success = folder.mkdirs();// Make folder
            }
            if (success) {
                copyData((getImageFilePath(data.getData())), destination);//
            } else { // if folder is present add the data
                if (folder.exists()) {
                    copyData(getImageFilePath(data.getData()), destination);
                }

            }

        }
    }


// Copy path
    public void copyData(String source, String destination) {
        File sourceLocation = new File(source);
        File targetLocation = new File(destination + "/" + mEtName.getText().toString().trim() + "-" + mSelectionType + ".jpeg");// Rename the file
        try {
            int actionChoice = 2;
            if (actionChoice == 1) {
                if (sourceLocation.renameTo(targetLocation)) {//The renameTo() function is used to rename the abstract path name of a File to a given path name. The function returns true if the file is renamed else returns false
                    Log.v(TAG, "Move file successfully.");//Move file successful.
                } else {
                    Log.v(TAG, "Move file failed.");//Move file failed.
                }
            } else {
                if (sourceLocation.exists()) {
                    InputStream in = new FileInputStream(sourceLocation);
                    OutputStream out = new FileOutputStream(targetLocation);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    Log.v(TAG, "Copy file successful.");
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.save_file), Toast.LENGTH_LONG).show();
                } else {
                    Log.v(TAG, "File failed to save.Source file missing.");
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



// get the actual path from URI

    public String getImageFilePath(Uri uri) {

        File file = new File(uri.getPath());
        String[] filePath = file.getPath().split(":");
        String image_id = filePath[filePath.length - 1];

        Cursor cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            cursor.close();
            return imagePath;
        }
        return null;
    }


    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {

        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

 if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
