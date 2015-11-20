package org.dowell.mediacastle.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.dowell.mediacastle.R;
import org.dowell.mediacastle.util.ImageUtil;

/**
 * WHAT DOES THIS DO?
 *
 * @author Matt
 */
public class SecureImageAdapter extends BaseAdapter {

    public static int VIEW = 1;
    public static int DELETE = 2;
    private static LayoutInflater inflater = null;
    private static String mode = "";

    // This is to determine if it is thumbnails only (the gallery) or thumbs+text
    private static boolean IMAGE_AND_TEXT = false;
    private Context mContext;
    private Uri[] securedImageUris = null;

    public SecureImageAdapter(Context c, Uri[] inImages, String inMode) {
        mContext = c;
        securedImageUris = inImages;
        mode = inMode;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        IMAGE_AND_TEXT = (mode != null && mode.length() > 0);

    }

    public void setSecureImageUris(Uri[] images) {
        this.securedImageUris = images;
    }

    public int getCount() {
        return securedImageUris.length;
    }

    public Object getItem(int position) {
        return securedImageUris[position];
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (IMAGE_AND_TEXT) {
            return getImageAndTextView(position, convertView, parent);
        } else {
            return getImageOnlyView(position, convertView, parent);
        }
    }

    /**
     * Loads a thumbnail for the given position/image
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private View getImageAndTextView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            vi = inflater.inflate(R.layout.image_item, null);
            holder.text = (TextView) vi.findViewById(R.id.image_button_text);
            holder.image = (ImageView) vi.findViewById(R.id.image);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.text.setText(mode);
        holder.image.setTag(securedImageUris[position]);
        holder.image.setImageBitmap(ImageUtil.loadObfuscatedThumbnail(securedImageUris[position]));

        return vi;
    }

    /**
     * Loads the THUMBNAIL in to the grid
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private View getImageOnlyView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ImageUtil.THUMB_WIDTH, ImageUtil.THUMB_HEIGHT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(2, 2, 2, 2);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setTag(securedImageUris[position]);
        imageView.setImageBitmap(ImageUtil.loadObfuscatedThumbnail(securedImageUris[position]));

        return imageView;
    }

    public static class ViewHolder {
        public TextView text;
        public ImageView image; // button
    }

}
