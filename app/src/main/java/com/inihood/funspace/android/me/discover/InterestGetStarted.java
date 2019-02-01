package com.inihood.funspace.android.me.discover;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.adapter.SlideAdapter;

public class InterestGetStarted extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private SlideAdapter adapter;
    private TextView[] mDot;
    private Button next;
    private Button back;
    private int mCurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_get_started);

        linearLayout = findViewById(R.id.dot_layout);
        viewPager = findViewById(R.id.slide_viewpager);
        back = findViewById(R.id.back);
        next = findViewById(R.id.next);
        adapter = new SlideAdapter(this);
        viewPager.setAdapter(adapter);
        adddotsIndicator(0);
        viewPager.addOnPageChangeListener(listener);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(mCurrentPage - 1);
            }
        });
    }
    public void adddotsIndicator(int position){

        mDot = new TextView[3];
        linearLayout.removeAllViews();

        for (int i = 0; i < mDot.length; i++){
            mDot[i] = new TextView(this);
            mDot[i].setText(Html.fromHtml("&#8226;"));
            mDot[i].setTextSize(35);
            mDot[i].setTextColor(getResources().getColor(R.color.whiteTransparentHalf));
            linearLayout.addView(mDot[i]);
        }
        if (mDot.length > 0){
            mDot[position].setTextColor(getResources().getColor(R.color.white));
        }
    }
    ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            adddotsIndicator(position);
            mCurrentPage = position;

            if (position == 0){

                next.setEnabled(true);
                back.setEnabled(false);
                back.setVisibility(View.INVISIBLE);
                next.setText("Next");
                back.setText("previous");

            }else if (position == mDot.length -1){

                next.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                next.setText("Finish");
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(InterestGetStarted.this, CreateInterestActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                back.setText("previous");

            }else {

                next.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                next.setText("Next");
                back.setText("previous");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
