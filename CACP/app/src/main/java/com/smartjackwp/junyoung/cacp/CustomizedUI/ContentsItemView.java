package com.smartjackwp.junyoung.cacp.CustomizedUI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartjackwp.junyoung.cacp.R;

public class ContentsItemView extends LinearLayout{
    TextView titleTextView;
    TextView descriptionTextView;
    TextView playTimeTextView;

    public ContentsItemView(Context context) {
        super(context);
        init(context);
    }

    public ContentsItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.contents_item, this, true);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        playTimeTextView = findViewById(R.id.playTimeTextView);
    }

    public void setTitle(String title)
    {
        this.titleTextView.setText(title);
    }

    public void setDescription(String description)
    {
        this.descriptionTextView.setText(description);
    }

    public void setPlayTime(String playTime)
    {
        this.playTimeTextView.setText(playTime);
    }
}

