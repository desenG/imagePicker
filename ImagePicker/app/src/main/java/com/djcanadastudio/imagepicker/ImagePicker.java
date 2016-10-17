package com.djcanadastudio.imagepicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by giatec on 2016-10-17.
 */

public class ImagePicker {
    //constants
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int REQUEST_IMAGE_LIBRARY = 2;
    private static File imageFile;
    private static ImagePicker instance;

    /**
     * Returns a singleton instance of this class.
     *
     * @return An instance of {@link ImagePicker}
     */
    public static ImagePicker getInstance() {
        synchronized (ImagePicker.class) {
            if (instance == null) {
                instance = new ImagePicker();
            }
        }
        return instance;
    }

    public void showDialogFromActivity(final Activity activity)
    {
        final CharSequence[] items = {"Take Photo", "Use Existing"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    pickPhotoFromCameraInActivity(activity);
                } else if (items[item].equals("Use Existing")) {
                    pickPhotoFromLibraryInActivity(activity);
                }
            }
        }).show().setCanceledOnTouchOutside(true);
    }

    public Bitmap getImageFromActivityResult(Activity activity, int requestCode, Intent data)
    {
        Bitmap imageFromActivityResult = null;
        //result of take picture
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            //load bitmap
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            imageFromActivityResult = BitmapFactory.decodeFile(imageFile.getPath(), bitmapOptions);

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
                    int bitmapWidth = imageFromActivityResult.getWidth();
                    int bitmapHeight = imageFromActivityResult.getHeight();

                    Matrix matrix = new Matrix();
                    matrix.preRotate(rotate);
                    imageFromActivityResult = Bitmap.createBitmap(imageFromActivityResult, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_IMAGE_LIBRARY) {
            Bundle extras = data.getExtras();
            if(extras != null){
                imageFromActivityResult=(Bitmap) extras.get("data");
            }
        }

        if (imageFromActivityResult==null) {
            imageFromActivityResult = BitmapFactory.decodeResource(activity.getResources(), R.drawable.img_profile_round);
        }


        //manipulate
        imageFromActivityResult = createImageWithDimension(imageFromActivityResult, 250);
        if(imageFile!=null)
        imageFile.delete();
        return imageFromActivityResult;
    }

    private static Bitmap createImageWithDimension(Bitmap bitmapInput, int dimension){
        int dimensionForBitmap = getSquareCropDimensionForBitmap(bitmapInput);
        Bitmap square = ThumbnailUtils.extractThumbnail(bitmapInput, dimensionForBitmap, dimensionForBitmap);
        return Bitmap.createScaledBitmap(square, dimension, dimension, true);
    }

    //to calculate the dimensions of the bitmap.
    private static int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        //If the bitmap is wider than it is tall use the height as the square crop dimension
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            return bitmap.getHeight();
        }
        //If the bitmap is taller than it is wide use the width as the square crop dimension
        else {
            return bitmap.getWidth();
        }
    }

    private void pickPhotoFromCameraInActivity(Activity activity)
    {
        if(activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String CurrentDateAndTime = getCurrentDateAndTime();
            imageFile = new File(Environment.getExternalStorageDirectory(), CurrentDateAndTime + ".jpg");
            Uri photoPath = Uri.fromFile(imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoPath);

            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else{
            Resources resources = activity.getResources();
            new AlertDialog.Builder(activity)
                    .setTitle("Error")
                    .setMessage("Camera not support")
                    .setPositiveButton(android.R.string.yes, null).create();
        }
    }

    private void pickPhotoFromLibraryInActivity(Activity activity)
    {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        // ******** code for crop image
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        activity.startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_IMAGE_LIBRARY);
    }

    private static String getCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
}
