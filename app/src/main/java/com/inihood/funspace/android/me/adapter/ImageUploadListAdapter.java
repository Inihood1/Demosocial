package com.inihood.funspace.android.me.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inihood.funspace.android.me.R;

import java.util.List;

public class ImageUploadListAdapter extends RecyclerView.Adapter<ImageUploadListAdapter.ViewHolder> {

    private List<String> fileNameList;
    private List<String> fileDoneList;
    private Context context;

    public ImageUploadListAdapter(List<String> fileNameList, List<String> fileDoneList){
        this.fileNameList = fileNameList;
        this.fileDoneList = fileDoneList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_single,
                parent, false);
        context = parent.getContext();
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String filename = fileNameList.get(position);
        holder.filenameView.setText(filename);

        String fileDone = fileDoneList.get(position);
        if (fileDone.equals("Uploading...")){
            holder.fileDoneView.setImageResource(R.drawable.ic_loading);
        }else {
            holder.fileDoneView.setImageResource(R.drawable.ic__done);
        }

    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        public TextView filenameView;
        public ImageView fileDoneView;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            filenameView = mView.findViewById(R.id.loading_text);
            fileDoneView = mView.findViewById(R.id.done);
            imageView = mView.findViewById(R.id.image);
        }
    }
}
