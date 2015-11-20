package org.dowell.mediacastle.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author mjdowell
 */
public class VideoAdapter extends BaseAdapter {
    private Context vContext;
    private int count;
    private Cursor videocursor;
    private int video_column_index;

    public VideoAdapter(Context c) {
        vContext = c;
    }

    public int getCount() {
        return count;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        System.gc();
        TextView tv = new TextView(vContext.getApplicationContext());
        String id = null;
        if (convertView == null) {
            video_column_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            videocursor.moveToPosition(position);
            id = videocursor.getString(video_column_index);
            video_column_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            videocursor.moveToPosition(position);
            id += " Size(KB):" + videocursor.getString(video_column_index);
            tv.setText(id);
        } else
            tv = (TextView) convertView;
        return tv;
    }
}
