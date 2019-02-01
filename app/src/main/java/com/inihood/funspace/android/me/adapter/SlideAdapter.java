package com.inihood.funspace.android.me.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inihood.funspace.android.me.R;

public class SlideAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SlideAdapter(Context context){
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.create_intere,
            R.drawable.tell_others,
            R.drawable.ic_done_white_24dp};

    public String[] slide_headings = {
            "Create an interest",
            "Tell others",
            "you're all set"};

    public String[] slide_title = {
            "Tell your followers what you like the most",
            "Your interest will be shown in their news feed only when they follow or when you add",
            "Ready?"};

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView imageView = view.findViewById(R.id.imageView8);
        TextView heading = view.findViewById(R.id.textView14);
        TextView desc = view.findViewById(R.id.textView15);

        imageView.setImageResource(slide_images[position]);
        heading.setText(slide_headings[position]);
        desc.setText(slide_title[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}