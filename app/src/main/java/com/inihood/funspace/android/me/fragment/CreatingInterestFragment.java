package com.inihood.funspace.android.me.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.inihood.funspace.android.me.R;


public class CreatingInterestFragment extends android.app.DialogFragment {

    private Button close;
    private ProgressBar searchView;
    private TextView listView;
    private ArrayAdapter<String> adapter;

    public CreatingInterestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_creating_interest, container, false);


       searchView = view.findViewById(R.id.searchView);
       listView = view.findViewById(R.id.listview);
       close = view.findViewById(R.id.button5);



        return view;
    }

}
