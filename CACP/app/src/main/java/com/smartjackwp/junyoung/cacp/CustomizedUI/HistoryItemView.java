package com.smartjackwp.junyoung.cacp.CustomizedUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartjackwp.junyoung.cacp.R;

public class HistoryItemView extends LinearLayout {
    Context mContext;

    ImageView thumbImageView;
    TextView fileNameTextView;
    TextView contentNameTextView;

    public HistoryItemView(Context context) {
        super(context);
        init(context);
    }

    public HistoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.history_item, this, true);

        thumbImageView = findViewById(R.id.thumbImageView);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        contentNameTextView = findViewById(R.id.contentNameTextView);
    }

    public void setThumb(String filePath){
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        thumbImageView.setImageBitmap(bitmap);
    }

    public void setFileName(String name){
        fileNameTextView.setText(name);
    }

    public void setContentName(String name){
        contentNameTextView.setText(name);
    }
}
