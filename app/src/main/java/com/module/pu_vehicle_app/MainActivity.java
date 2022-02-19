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
import android.util.Patterns;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;
    ImageButton copy ;
    EditText mResultEt;
    ImageView mPreviewIv;
    EditText etname,etphone1,etphone2,etemail;
    AutoCompleteTextView etlocation;
    String[] listitem;
    Button submit;

    DatabaseReference databaseReference;
    public static final String CHANNEL_ID="channel1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mResultEt = findViewById(R.id.resultET);
        copy= findViewById(R.id.copy_button);
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        mPreviewIv = findViewById(R.id.imageV);
        etname=findViewById(R.id.name);
        etphone1=findViewById(R.id.phone1);
        etphone2=findViewById(R.id.phone2);
        etemail=findViewById(R.id.email);
        etlocation=findViewById(R.id.location);
        submit=findViewById(R.id.submit);
        listitem=getResources().getStringArray(R.array.city);

         String name=  etname.getText().toString();
        String phone1=  etphone1.getText().toString();
        String phone2=  etphone2.getText().toString();
        String email=   etemail.getText().toString();
        String location=  etlocation.getText().toString();

        databaseReference= FirebaseDatabase.getInstance().getReference("Vehicle").child(mResultEt.getText().toString());

        //STRING ADAPTER
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,listitem);
        etlocation.setAdapter(adapter);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mResultEt.length()==0) {
                    mResultEt.setError("Enter Vehicle Number ");
                    mResultEt.requestFocus();
                    return;

                }
                if (etname.length()==0) {
                    etname.setError("Enter The Name");
                    etname.requestFocus();
                    return;
                }


                if (etphone1.length()==0) {
                    etphone1.setError("Enter The Phone Number");
                    etphone1.requestFocus();
                    return;
                }
                if (etphone2.length()==0) {
                    etphone2.setError("Enter The Phone Number");
                    etphone2.requestFocus();
                    return;

                }
                if (etemail.length()==0) {
                    etemail.setError("Enter Your Email");
                    etemail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(etemail.getText().toString()).matches()){
                    etemail.setError("Please Provide A Valid Email Address");
                    etemail.requestFocus();
                    return;
                }
                if (etlocation.length()==0) {
                    etlocation.setError("Enter The location");
                    etlocation.requestFocus();
                    return;

                }

                else {


                    InsertData();

                }

            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String getstring = mResultEt.getText().toString();
                clipboard.setText(getstring);
                Toast.makeText(MainActivity.this,"Copied to your Clipboard",Toast.LENGTH_LONG).show();

            }
        });
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF6200EE"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setSubtitle("Upload the information");



        }
    public void setLanguage(Activity a, String language){
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
                // startActivity(new Intent(SearchActivity.this,SearchActivity.class));
                break;
            }
            case R.id.hinbtn:{
                setLanguage(this,"hi");
                recreate();
                // startActivity(new Intent(SearchActivity.this,SearchActivity.class));

                break;
            }
            case R.id.gujbtn:{
                setLanguage(this,"gu");
                recreate();
                //startActivity(new Intent(SearchActivity.this,SearchActivity.class));


                break;
            }
        }
    }

    private void InsertData() {
        String name=  etname.getText().toString();
        String phone1=  etphone1.getText().toString();
        String phone2=  etphone2.getText().toString();
        String email=   etemail.getText().toString();
        String location=  etlocation.getText().toString();
        String vehicleNo= mResultEt.getText().toString();

        Vehicle vehicle= new Vehicle(vehicleNo,name,phone1,phone2,email,location);

       // databaseReference.push().child(vehicleNo).setValue(vehicle);
        FirebaseDatabase.getInstance().getReference("Vehicle").child(vehicleNo).setValue(vehicle);
        Toast.makeText(MainActivity.this,"Data Inserted Successfully",Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(MainActivity.this,SearchActivity.class));
            Toast.makeText(MainActivity.this,"Search",Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.exit) {
            Toast.makeText(MainActivity.this, "Exit Successfully", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(1);
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
                        Toast.makeText(MainActivity.this, "Permission failed", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(MainActivity.this, "Permission Failed", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this,""+error,Toast.LENGTH_SHORT).show();

            }
        }
    }
    }

