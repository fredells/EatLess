package fredells.eatless;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Gallery extends Activity {

    private ImageView imageView;
    private Button shareButton;

    private String imagePath;
    private Bitmap imageBitmap;

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

    private void createImage() {
        File[] paths = getImagePaths();

        int length = paths.length;
        int width = 200;
        int height = 200;
        int imagesPerRow = 2;

        switch (length) {
            case 5:
            case 6:
                imagesPerRow = 2;
                height = 300;
                break;
            case 7:
            case 8:
            case 9:
                width = 300;
                height = 300;
                imagesPerRow = 3;
                break;
            case 10:
            case 11:
            case 12:
                imagesPerRow = 3;
                width = 300;
                height = 400;
                break;
        }


        Bitmap bmOverlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);

        int row = 1;
        int x = 0;
        int y = 0;

        for (int i = 0; i < length; i++) {

            File image = paths[i];

            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image.getAbsolutePath()), 100, 100);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bmp = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);

            //for new row
            if (i == imagesPerRow * row) {
                x -= imagesPerRow * 100;
                y += 100;
                row++;
            }
            Log.v("DEBUGGING", "IMAGE NUMBER: " + i + " ROW: " + row + " X: " + x + " Y: " + y);

            canvas.drawBitmap(bmp, x, y, null);

            x += 100;

            try {
                //append bitmap
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String date = dateFormat.format(new Date());
                String photoFile = "EATLESS_GALLERY_" + date + ".jpg";

                imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Eatless/" + photoFile;

                FileOutputStream out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Eatless/" + photoFile);
                bmOverlay.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.close();

            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "Failed to draw bitmap");
                e.printStackTrace();
            }

        }
    }

    private void checkForImage() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String date = dateFormat.format(new Date());
    }

    Button.OnClickListener shareClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("*/*");

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "title");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


                    OutputStream outstream;
                    try {
                        outstream = getContentResolver().openOutputStream(uri);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                        outstream.close();
                    } catch (Exception e) {
                        System.err.println(e.toString());
                    }
                    share.putExtra(Intent.EXTRA_TEXT, "I am logging meals. This is everything I ate today!");
                    share.putExtra(Intent.EXTRA_STREAM, uri);

                    startActivity(Intent.createChooser(share, "Share Image"));
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //setup views
        imageView = findViewById(R.id.imageView);
        shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(shareClickListener);

        //create gallery image
        createImage();
        try {
            Bitmap thumbnail = BitmapFactory.decodeFile(imagePath);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(thumbnail);

            imageBitmap = thumbnail;

        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "Failed to create gallery image");
            e.printStackTrace();
        }



    }
}
