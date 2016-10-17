package com.djcanadastudio.imagepicker;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    //constants
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int REQUEST_IMAGE_LIBRARY = 2;
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
            if(requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_LIBRARY)
            {
                Bitmap profileBitmap = null;
                profileBitmap = ImagePicker.getInstance().getImageFromActivityResult(this, requestCode, data);
                profileImg.setImageBitmap(RoundedImageView.getRoundBitmap(profileBitmap, 150, "#FFFFFF"));
            }
        }

    }

    private void linkToUI() {
        profileImg = (ImageView) findViewById(R.id.profileimg);
    }

    public void takePhoto(View view) {
        ImagePicker.getInstance().showDialogFromActivity(this);
    }
}
