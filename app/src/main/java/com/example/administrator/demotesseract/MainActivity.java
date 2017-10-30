package com.example.administrator.demotesseract;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.R.attr.x;

public class MainActivity extends AppCompatActivity {

    ImageButton btnCapture, btnUpload;
    TextView textView;
    private Uri fileUri;
    private static String imageFullPath="";
    public static final String DATA_PATH= Environment.getExternalStorageDirectory().toString() + "/tesseract_languages/";
    public boolean dirExist=false;
    private static final int REQUEST_CODE = 100;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static String textOfImage=""; // this variable is for storing text of image temporary


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(LOG_TAG,"data path la gi " +DATA_PATH);
        addControls();
        addEvents();

        try {
            createDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void createDirectory() throws IOException {
//        File dir  = new File(DATA_PATH + "tessdata/");
//        if (!dir.exists()) {
//            dir.mkdir();
//            dirExist = true;
//            if (!dirExist) {
//                Log.d("msg :", "ERROR: Creation of directory " + DATA_PATH + "tessdata/" + " on sdcard failed");
//                return;
//            } else {
//                Log.d("msg :", "Created directory " + DATA_PATH + "tessdata/" + " on sdcard");
//            }
//        }
//
//
//        File file = new File(DATA_PATH + "tessdata/" + "eng.traineddata");
//        if (!(file.exists())) {
//
//            System.out.println("open file");
//
//            AssetManager assetManager = getAssets();
//            //System.out.println("File name => " + filename);
//            InputStream in = null;
//            OutputStream out = null;
//                in = assetManager.open("tessdata/" +"eng.traineddata");   // if files resides inside the "Files" directory itself
//                out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/tesseract_languages/"+"tessdata/"+"eng.traineddata");
//                copyFile(in, out);
//                in.close();
//                in = null;
//                out.flush();
//                out.close();
//                out = null;
//        }

        File dir = new File(DATA_PATH + "tessdata/");
        if (!dir.exists()) {
            dir.mkdirs();
            dirExist = true;
            if (!dirExist) {
                Log.d("msg :", "ERROR: Creation of directory " + DATA_PATH + "tessdata/" + " on sdcard failed");
                return;
            } else {
                Log.d("msg :", "Created directory " + DATA_PATH + "tessdata/" + " on sdcard");
            }
        }

        File file = new File(DATA_PATH + "tessdata/" + "jpn.traineddata");
        if (!(file.exists())) {

            System.out.println("open file");

            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list("tessdata");
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }


            //System.out.println("File name => " + filename);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("tessdata/" + "jpn.traineddata");   // if files resides inside the "Files" directory itself
                out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/tesseract_languages/" + "tessdata/" + "jpn.traineddata");
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.d("tag error :", e.getMessage());
            }

        }
    }
//    private void copyFile(InputStream in, OutputStream out) throws IOException {
//
//        byte[] buffer = new byte[1024];
//        int read;
//        Log.v(LOG_TAG,"co copy khong");
//            while ((read = in.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//                Log.v(LOG_TAG,"read la gi " +read);
//
//            }
//
//
//
//    }
    private void copyFile(InputStream in, OutputStream out) {

        byte[] buffer = new byte[1024];
        int read;
        try {
            System.out.println("File read");
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void addControls() {
        btnCapture = (ImageButton) findViewById(R.id.btnCapture);
        textView = (TextView) findViewById(R.id.text);
    }

    private void addEvents() {
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,REQUEST_CODE);
                }
                else {
                    Toast.makeText(getApplication(), "Camera cannot support", Toast.LENGTH_LONG).show();
                }
            }

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && data!=null) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            Log.v(LOG_TAG,"file path coloumn " +filePathColumn[0]);
            imageFullPath = cursor.getString(columnIndex);
            Log.v(LOG_TAG,"image full path " + imageFullPath);
            cursor.close();
            new LoadImage(imageFullPath).execute();

        }
    }

    public  class LoadImage extends AsyncTask<String,Boolean,Void>{
        ProgressDialog progressDialog;
        private String imageUrl ;
        LoadImage(String imageFullPath) {
            imageUrl = imageFullPath;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait..");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {

            performOcr(imageFullPath);
            return null;
        }

        private void performOcr(String imagePath) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFullPath);

            try {
                ExifInterface exif = new ExifInterface(imagePath);
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                Log.v("Image :", "Orient: " + exifOrientation);

                int rotate = 0;
                switch (exifOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                }

                Log.v("Pic Rotate:", "Rotation: " + rotate);

                if (rotate != 0) {

                    // Getting width & height of the given image.
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    // Setting pre rotate
                    Matrix mtx = new Matrix();
                    mtx.preRotate(rotate);

                    // Rotating Bitmap
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                    // tesseract req. ARGB_8888
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.setDebug(true);
            Log.v(LOG_TAG,"wtf duong dan " +DATA_PATH);
            tessBaseAPI.init(DATA_PATH,"jpn");
            tessBaseAPI.setImage(bitmap);
            String text = tessBaseAPI.getUTF8Text();
            Log.v(LOG_TAG,"text la gi " +text);
            tessBaseAPI.end();
            textOfImage = text;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            textView.setText(textOfImage);
            progressDialog.dismiss();
        }
    }



}
