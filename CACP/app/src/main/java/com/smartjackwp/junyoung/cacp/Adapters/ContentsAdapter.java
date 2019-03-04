package com.smartjackwp.junyoung.cacp.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.smartjackwp.junyoung.cacp.CustomizedUI.ContentsItemView;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;

import java.util.ArrayList;

public class ContentsAdapter extends BaseAdapter {
    Context context;
    ArrayList<AccentContents> contentsList;

    public ContentsAdapter(Context context){
        this.context = context;
    }

    public ContentsAdapter(Context context, ArrayList<AccentContents> contentsList){
        this.context = context;
        this.contentsList = contentsList;
    }

    public void setContentsList(ArrayList<AccentContents> contentsList){
        this.contentsList = contentsList;
    }

    @Override
    public int getCount() {
        return contentsList.size();
    }

    @Override
    public Object getItem(int position) {
        return contentsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContentsItemView view;
        if(convertView == null)
            view = new ContentsItemView(context);
        else
            view = (ContentsItemView)convertView;

        AccentContents contents = (AccentContents)getItem(position);
        view.setTitle(contents.getTitle());
        view.setDescription(contents.getDescription());
        //view.setPlayTime();

        return view;
    }
}
