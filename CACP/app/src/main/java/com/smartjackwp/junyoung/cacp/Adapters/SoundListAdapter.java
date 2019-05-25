package com.smartjackwp.junyoung.cacp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartjackwp.junyoung.cacp.Activities.PracticeAccentActivity;
import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;
import com.smartjackwp.junyoung.cacp.Utils.Tools;

import java.util.ArrayList;

public class SoundListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<AccentContents> contentsList;

    public SoundListAdapter(Context context, ArrayList<AccentContents> contentsList){
        this.context = context;
        this.contentsList = contentsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        return new SoundItemView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contents_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        SoundItemView view = (SoundItemView)viewHolder;
        final AccentContents item = contentsList.get(position);
        view.titleTextView.setText(item.getTitle());
        view.descriptionTextView.setText(item.getDescription());
        long duration = (long)item.getDuration();
        view.playTimeTextView.setText(Tools.getTimeFormat(duration));
        view.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, PracticeAccentActivity.class)
                        .putExtra(AccentContents._ID, item.getId()));
            }
        });
        view.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if(ChineseAccentComparison.getInstance(context).deleteContents(item))
                                    notifyDataSetChanged();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", dialogClickListener)
                        .setNegativeButton("취소", dialogClickListener)
                        .show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if(contentsList != null)
            return contentsList.size();

        return 0;
    }

    class SoundItemView extends RecyclerView.ViewHolder{
        LinearLayout itemLayout;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView playTimeTextView;

        public SoundItemView(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            playTimeTextView = itemView.findViewById(R.id.playTimeTextView);
            itemLayout = itemView.findViewById(R.id.itemLayout);
        }


    }


}
