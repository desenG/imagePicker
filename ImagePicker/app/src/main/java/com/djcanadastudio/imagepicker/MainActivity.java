package com.djcanadastudio.imagepicker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    //constants
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private File imageFile;
    private ImageView profileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linkToUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        if (responseCode == RESULT_OK) {
            Bitmap profileBitmap = null;
            //result of take picture
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //load bitmap
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                profileBitmap = BitmapFactory.decodeFile(imageFile.getPath(), bitmapOptions);

                //Rotate image. it seems some device such samsung only put a exif tag orientation but not actually rotate image.
                try {
                    ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotate = 0;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }

                    if (rotate != 0) {
                        int profileBitmapWidth = profileBitmap.getWidth();
                        int profileBitmapHeight = profileBitmap.getHeight();

                        Matrix matrix = new Matrix();
                        matrix.preRotate(rotate);
                        profileBitmap = Bitmap.createBitmap(profileBitmap, 0, 0, profileBitmapWidth, profileBitmapHeight, matrix, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (profileBitmap==null) {
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_profile_round);
                }


                //manipulate
                profileBitmap = createProfilePictureFromCamera(profileBitmap, 250);
                profileImg.setImageBitmap(RoundedImageView.getRoundBitmap(profileBitmap, 150, "#FFFFFF"));

            }
        }
    }

    private void linkToUI() {
        profileImg = (ImageView) findViewById(R.id.profileimg);
    }

    public void takePhoto(View view) {
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String CurrentDateAndTime = getCurrentDateAndTime();
            imageFile = new File(Environment.getExternalStorageDirectory(), CurrentDateAndTime + ".jpg");
            Uri photoPath = Uri.fromFile(imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoPath);
            if(deviceHasFrontCamera()){
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            }

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else{
            Resources resources = this.getResources();
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Camera not support")
                    .setPositiveButton(android.R.string.yes, null).create();
        }
    }

    private String getCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    private Boolean deviceHasFrontCamera(){

        Boolean hasFrontCamera=false;

        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo=new Camera.CameraInfo();

        if(cameraCount>0){
            while( --cameraCount >= 0){
                Camera.getCameraInfo(cameraCount,cameraInfo);
                if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    hasFrontCamera=true;
                    break;
                }
            }
        }
        return hasFrontCamera;
    }

    public static Bitmap createProfilePictureFromCamera(Bitmap bitmapInput, int dimension){
        int dimensionForBitmap = getSquareCropDimensionForBitmap(bitmapInput);
        Bitmap square = ThumbnailUtils.extractThumbnail(bitmapInput, dimensionForBitmap, dimensionForBitmap);
        return Bitmap.createScaledBitmap(square, dimension, dimension, true);
    }

    //to calculate the dimensions of the bitmap.
    public static int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        //If the bitmap is wider than it is tall use the height as the square crop dimension
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            return bitmap.getHeight();
        }
        //If the bitmap is taller than it is wide use the width as the square crop dimension
        else {
            return bitmap.getWidth();
        }
    }
}
