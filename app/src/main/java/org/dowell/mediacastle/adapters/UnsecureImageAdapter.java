package org.dowell.mediacastle.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.dowell.mediacastle.R;
import org.dowell.mediacastle.util.ImageInfo;

import java.util.List;

/**
 * Used for putting images in the view
 */
public class UnsecureImageAdapter extends BaseAdapter {

    private static final String TAG = "ImageAdapter";
    private Context mContext;
    private List<ImageInfo> allGalleryThumbs;
    private LayoutInflater inflater = null;

    public UnsecureImageAdapter(List<ImageInfo> inThumbs, Context c) {
        mContext = c;
        allGalleryThumbs = inThumbs;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.d(TAG, "ImageAdapter count = " + getCount());
    }

    public UnsecureImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return (allGalleryThumbs != null) ? allGalleryThumbs.size() : 0;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Called repeatedly to render the View of each item in the gallery.
     *
     * @see http
     * ://developer.android.com/reference/android/provider/MediaStore.Images
     * .Thumbnails.html
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.image_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) vi.findViewById(R.id.image_button_text);
            holder.image = (ImageView) vi.findViewById(R.id.image);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        Bitmap thumb = allGalleryThumbs.get(position).getThumbImage();
        holder.text.setText("Move to MediaCastle");
        holder.image.setImageBitmap(thumb);

        return vi;
    }

    @Override
    public void notifyDataSetInvalidated() {
        // We have most likely removed one of the images
        // from the gallery, now requery
        super.notifyDataSetChanged();
    }

    public static class ViewHolder {
        public TextView text;
        public ImageView image; // button
    }

}