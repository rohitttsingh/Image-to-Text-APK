package com.module.pu_vehicle_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    private static final int REQUEST_CALL = 1;

    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;
    Button searchBtn ;
    EditText mResultEt;
    ImageView mPreviewIv;
    Button callOne,callTwo;
    TextView tvname , tvlocation , tvemail , tvphone1,tvphone2;



    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mResultEt = findViewById(R.id.resultET);
        searchBtn= findViewById(R.id.searchBtn);
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        mPreviewIv = findViewById(R.id.imageV);
        callOne=findViewById(R.id.callOne);
        callTwo=findViewById(R.id.callTwo);
        tvname=findViewById(R.id.name);
        tvemail=findViewById(R.id.email);
        tvlocation=findViewById(R.id.location);
        tvphone1=findViewById(R.id.phone1);
        tvphone2=findViewById(R.id.phone2);
        String vehicleNo= mResultEt.getText().toString();

            searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseDatabase=FirebaseDatabase.getInstance();
                databaseReference=firebaseDatabase.getReference("Vehicle").child(vehicleNo);

                if (mResultEt.length()==0){
                    mResultEt.setError("Enter Vehicle Number ");
                    mResultEt.requestFocus();
                    return;
                }
                else{
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Vehicle vehicleData= snapshot.child(mResultEt.getText().toString()).getValue(Vehicle.class);

                        String emailtv = vehicleData.getEmail();
                        String nametv ="Name: "+ vehicleData.getName();
                        String phone1tv = vehicleData.getPhone1();
                        String phone2tv = vehicleData.getPhone2();
                        String locationtv = "Location: "+vehicleData.getLocation();
                        tvname.setText(nametv);
                        tvemail.setText(emailtv);
                        tvphone1.setText(phone1tv);
                        tvphone2.setText(phone2tv);
                        tvlocation.setText(locationtv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UserListActivity", "Error occured");

                        Toast.makeText(SearchActivity.this,"DATA NOT FOUND...",Toast.LENGTH_LONG).show();

                    }
                });
                }

            }
        });


        callOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callButton(tvphone1.getText().toString());
            }
        });

        callTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callButton(tvphone2.getText().toString());
            }
        });
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF6200EE"));
        actionBar.setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle("Search the information");


    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setLanguage(Activity a,String language){
        Locale locale= new Locale(language);
        locale.setDefault(locale);
        Resources resources= a.getResources();
        Configuration configuration=resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        SharedPreferences.Editor editor=getSharedPreferences("Setting",MODE_PRIVATE).edit();
        editor.putString("My_Lang",language);
        editor.apply();
    }

    public void onClick(View view){
        switch (view.getId()){

            case R.id.engbtn:{
                setLanguage(this,"en");
                recreate();
                break;
            }
            case R.id.hinbtn:{
                setLanguage(this,"hi");
                recreate();
                break;
            }
            case R.id.gujbtn:{
                setLanguage(this,"gu");
                recreate();
                break;
            }
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void callButton(String number) {
        if (ContextCompat.checkSelfPermission(SearchActivity.this,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SearchActivity.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);
        }
        else{
            String dial= "tel:"+number;
            startActivity(new Intent(Intent.ACTION_CALL,Uri.parse(dial)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add) {
            showImageImportDialog();
        }
        if (id == R.id.search) {
            startActivity(new Intent(SearchActivity.this,SearchActivity.class));
        }
        if (id == R.id.exit) {
            Toast.makeText(SearchActivity.this, "Exit Successfully", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
    private void showImageImportDialog() {
        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                if (which == 0) {
                    if (!checkCameraPermission()){

                        requestCameraPermission();
                    } else {
                        pickCamera();
                    }
                }
                if (which == 1){
                    if (!checkStoragePermission()) {

                        requestStoragePermission();
                    } else{
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }
    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraInent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraInent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraInent, IMAGE_PICK_CAMERA_CODE);
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);

    }
    private boolean checkStoragePermission() {
        boolean results = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return results;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean results = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean results1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return results && results1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccpeted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccpeted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccpeted && writeStorageAccpeted) {
                        pickCamera();
                    } else {
                        Toast.makeText(SearchActivity.this, "Permission failed", Toast.LENGTH_SHORT).show();

                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccpeted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccpeted) {
                        pickGallery();
                    } else {
                        Toast.makeText(SearchActivity.this, "Permission Failed", Toast.LENGTH_SHORT).show();

                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(data.getData()).
                        setGuidelines(CropImageView.Guidelines.ON).
                        start(this);

            }
            if (requestCode==IMAGE_PICK_CAMERA_CODE){
                CropImage.activity(image_uri).
                        setGuidelines(CropImageView.Guidelines.ON).
                        start(this);

            }
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resultUri =result.getUri();
                mPreviewIv.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable=(BitmapDrawable) mPreviewIv.getDrawable();
                TextRecognizer recognizer= new TextRecognizer.Builder(getApplicationContext()).build();

                if (!recognizer.isOperational()){
                    Toast.makeText(SearchActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
                else{
                    Frame frame= new Frame.Builder().setBitmap(bitmapDrawable.getBitmap()).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb =  new StringBuilder();

                    for (int i = 0;i<items.size();i++){
                        TextBlock myitems=items.valueAt(i);
                        sb.append(myitems.getValue());
                        sb.append("\n");
                    }
                    mResultEt.setText(sb.toString());
                }

            }
            else if (resultCode== CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(SearchActivity.this,""+error,Toast.LENGTH_SHORT).show();

            }
        }
    }
}