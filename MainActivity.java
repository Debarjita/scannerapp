package com.example.apptest4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    // UI Views
    private MaterialButton inputImageBtn;
    private MaterialButton recognizeTextBtn;
    private ShapeableImageView imageIv;
    private EditText recognisedTextEt;

    // TAG
    private static final String TAG ="MAIN_TAG";
    private Uri imageUri =null;

    //To handle the result of Camera/Gallery permission
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE  =101;

    //arrays of permission requires to pick image from camera , gallery
    private String[] cameraPermission;
    private String[] storagePermission;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init UI Views
        inputImageBtn = findViewById(R.id.inputImageBtn);
        recognizeTextBtn=findViewById(R.id.recognizeTextBtn);
        imageIv = findViewById(R.id.imageIv);
        recognisedTextEt=findViewById(R.id.recognisedTextEt);

        // init arrays of permission required for camera, gallery
        cameraPermission = new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{  Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // handle click , show input image dialogue
        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputImageDialogue();
            }
        });
    }

    private void showInputImageDialogue() {
        PopupMenu popupMenu = new PopupMenu( this,inputImageBtn);

        popupMenu.getMenu().add(Menu.NONE,1, 1, "CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2, 2, "GALLERY");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==1)
                {
                    if (checkCameraPermissions()){
                        pickImageCamera();
                    }
                    else{
                        requestCameraPermissions();
                    }
                } else if (id ==2) {
                    if (checkStoragePermission()){
                        pickImageGallery();
                    }
                    else {
                        requestStoragePermission();
                    }

                }
                return false;
            }
        });

    }
    private void pickImageGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        // set type of file we want to pick ie image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent>galleryActivityResultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //here we will recieve the image if picked
                    if (result.getResultCode()== Activity.RESULT_OK)
                    {
                        //image picked
                        Intent data = result.getData();
                        imageUri =data.getData();

                        // set to image view
                        imageIv.setImageURI(imageUri);

                    }
                    else {
                        //cancelled
                        Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
    private void pickImageCamera()
    {
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample Title");
        values.put((MediaStore.Images.Media.DESCRIPTION, " Sample description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // here we will recieve the image if taken from camera
                    if (result.getResultCode()== Activity.RESULT_OK){

                        imageIv.setImageURI(imageUri);
                    }
                    else{
                        //cancelled
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermissions()
    {
        // check if camera permissions are allowed or not
        boolean cameraResult = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return cameraResult && storageResult;
    }

    private void requestCameraPermissions()
    {
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
    }
    // handle permission results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Camera ans storage permissions are required ", Toast.LENGTH_SHORT).show();
                    }
                }
                case STORAGE_REQUEST_CODE: {
                    if (grantResults.length>0)
                    {
                        boolean storageAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                        if (storageAccepted){
                            pickImageGallery();
                        }
                        else{
                            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        }

    }
}
