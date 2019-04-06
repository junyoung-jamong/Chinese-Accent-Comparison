package com.smartjackwp.junyoung.cacp.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.smartjackwp.junyoung.cacp.CustomizedUI.HistoryItemView;
import com.smartjackwp.junyoung.cacp.Entity.History;

import java.util.ArrayList;

public class HistoryAdapter extends BaseAdapter {
    Context context;
    ArrayList<History>  historyList;

    public HistoryAdapter(Context context, ArrayList<History> historyList){
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryItemView view;

        if(convertView == null)
            view = new HistoryItemView(context);
        else
            view = (HistoryItemView)convertView;

        History history = (History) getItem(position);

        view.setThumb(history.getImagePath());
        view.setFileName(history.getFileName());
        view.setContentName(history.getContentName());

        return view;
    }
}
