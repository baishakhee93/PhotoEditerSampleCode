package com.baishakhee.photoeditersamplecode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    String ecImage="",picturePath_new="";
    Bitmap bitmap;
    File file;
    private ImageView btnRotate, btnGrayscale, btnReset,selectImage,btnGalary;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final int REQUEST_CODE_CROP_IMAGE = 2;
    private Uri mSelectedImageUri;
   // private CropImageView mCropImageView;
   ActivityResultLauncher<Intent> launchSomeActivity,launchSomeActivitycamera;
    private Bitmap originalBitmap, currentBitmap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnGalary = findViewById(R.id.btnGalary);
        btnRotate = findViewById(R.id.btnRotate);
        btnGrayscale = findViewById(R.id.btnGrayscale);
        btnReset = findViewById(R.id.btnReset);
        selectImage = findViewById(R.id.selectImage);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent();
            }
        });
        btnGalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryIntent();
            }
        });
        launchSomeActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            onSelectFromGalleryResult(data);

                        }
                    }
                });


        launchSomeActivitycamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            onCaptureImageResult(data);

                        }
                    }
                });

        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(currentBitmap);

        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        btnGrayscale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyGrayscale();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
            }
        });
    }
    private void galleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Start the Intent
        launchSomeActivity.launch(galleryIntent);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launchSomeActivitycamera.launch(intent);
    }
    private void onCaptureImageResult(Intent data) {

        try {
            bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            byte[] byteArray = bytes .toByteArray();
            imageView.setImageBitmap(bitmap);
            ecImage = Base64.encodeToString(byteArray, Base64.DEFAULT);



            //  getUpload();


            file = new File(Environment.getExternalStorageDirectory(),
                    System.currentTimeMillis() + ".jpg");

            //    picturePath_new = Environment.getExternalStorageDirectory().toString();

            FileOutputStream fo;
            try {
                file.createNewFile();
                fo = new FileOutputStream(file);
                picturePath_new = file.getAbsolutePath();
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //decodeFile(picturePath_new);

            // ConfirmDialogBox(bitmap);

            //ConfirmDialogBox(bitmap);
        } catch (Exception e) {
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        picturePath_new = cursor.getString(columnIndex);
        cursor.close();

        decodeFile (picturePath_new);




          /*  try {
                Log.e("sss", picturePath);
                Intent in = new Intent(EditProfile.this, CropActivity.class);
                in.putExtra("ImagePath", picturePath);
                startActivityForResult(in, 80);
            } catch (Exception e) {
                Log.e("Ex", e.toString());
            }*/
    }

    private void decodeFile(String filePath) {

        try {
            Bitmap original = BitmapFactory.decodeStream(getAssets().open(filePath));
            Log.e("Original   dimensions", original.getWidth() + " " + original.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);
        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);


        file = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            file.createNewFile();
            fo = new FileOutputStream(file);
            fo.write(byteArrayOutputStream.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
            //   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            imageView.setImageBitmap(bitmap);
            ecImage = Base64.encodeToString(byteArray, Base64.DEFAULT);


        } catch (Exception e) {

        }
    }


    private void rotateImage() {
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), currentBitmap.getConfig());
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                rotatedBitmap.setPixel(j, bitmap.getWidth() - i - 1, bitmap.getPixel(i, j));
            }
        }
        bitmap = rotatedBitmap;
        imageView.setImageBitmap(bitmap);
    }

    private void applyGrayscale() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }

    private void resetImage() {
        bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);
    }


}