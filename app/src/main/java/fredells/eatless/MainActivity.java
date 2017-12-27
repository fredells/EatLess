package fredells.eatless;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.VideoView;


public class MainActivity extends Activity {

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private ImageView mImageView;
    private Bitmap mImageBitmap;
    private ImageButton accessCameraButton;
    private Button settingsButton, calendarButton;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout;
    private MyRecyclerViewAdapter adapter;
    private LinearLayoutManager horizontalLayoutManager;

    private String mCurrentPhotoPath;
    private File mFile;

    private static final String JPEG_FILE_PREFIX = "EATLESS_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private Camera mCamera = null;
    private CameraView mCameraView = null;

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        this.sendBroadcast(mediaScanIntent);
    }

    public File[] getImagePaths() {
        File imageFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EatLess");
        imageFolder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String date = dateFormat.format(new Date());
        File[] listImages = imageFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
                return (name.contains(date) && !name.contains("GALLERY"));
            }
        });

        Arrays.sort(listImages);

        return listImages;
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        mCameraView.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void resetCameraView() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "resetCameraView") {
                resetCameraView();
                File picture = (File)intent.getExtras().get("file");
                adapter.addImage(picture);
                Log.v("DEBUGGING", "RESET CAMERA VIEW");
            }
        }
    };

    Button.OnClickListener accessCameraOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("DEBUGGING", "PICTURE TAKEN");
                    mCamera.startPreview();
                    mCamera.takePicture(null, null,
                            new PhotoHandler(getApplicationContext()));
                    //dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
                    //mCamera.startPreview();
                }
            };

    Button.OnClickListener settingsClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Settings.class);
                    startActivity(intent);
                }
            };

    Button.OnClickListener galleryClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Gallery.class);
                    startActivity(intent);
                }
            };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = new CameraView(this);//create a SurfaceView to show camera data
        safeCameraOpen(0);
        mCameraView.setCamera(mCamera);

        if(mCamera != null) {
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.cameraView);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        mImageBitmap = null;

        //setup button
        accessCameraButton = findViewById(R.id.cameraButton);
        accessCameraButton.setOnClickListener(accessCameraOnClickListener);

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(settingsClickListener);

        calendarButton = findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(galleryClickListener);

        //getImages();

        // set up the RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        adapter = new MyRecyclerViewAdapter(this, getImagePaths());
        horizontalLayoutManager.scrollToPosition(0);
        recyclerView.setAdapter(adapter);

       // LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("resetCameraView"));

    }

    @Override
    protected void onPause() {
        releaseCameraAndPreview();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        safeCameraOpen(0);
        mCameraView.setCamera(mCamera);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("resetCameraView"));
        super.onResume();
    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(
                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
    }

    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


}