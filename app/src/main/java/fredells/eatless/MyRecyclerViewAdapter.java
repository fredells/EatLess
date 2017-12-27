package fredells.eatless;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fred on 2017-12-12.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private File[] filePaths;
    private ArrayList<File> fileList;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, File[] paths) {
        this.mInflater = LayoutInflater.from(context);
        //this.filePaths = paths;
        this.fileList = new ArrayList<>(Arrays.asList(paths));
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //File image = filePaths[position];
        File image = fileList.get(position);

        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image.getAbsolutePath()), 100, 100);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);

        holder.myImageView.setImageBitmap(rotatedBitmap);
        //Picasso.with(holder.myImageView.getContext()).load(image).resize(100, 100).rotate(90).into(holder.myImageView);


    }

    // total number of rows
    @Override
    public int getItemCount() {
        return fileList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView myImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            myImageView = itemView.findViewById(R.id.recyclerImage);
        }
    }

    public void addImage(File path) {
        Log.v("DEBUGGING", "ADD IMAGE TO RECYCLER");
        this.fileList.add(0, path);
        this.notifyItemInserted(0);

    }

}