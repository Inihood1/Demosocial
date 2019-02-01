package com.inihood.funspace.android.me.dialogsFragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnInterestCategorySelected;


public class InterestSelectionFragment extends android.app.DialogFragment {

    private Button close;
    private SearchView searchView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private OnInterestCategorySelected interestCategorySelected;
    private String whichEver;

    public interface Capture{
        void onClickListner(String txt);
    }

    public Capture captureText;

    public InterestSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_interest_selection, container, false);

       getDialog().setTitle("Search here");

       searchView = view.findViewById(R.id.searchView);
       listView = view.findViewById(R.id.listview);
       close = view.findViewById(R.id.button5);

       adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
               getResources().getStringArray(R.array.categories));
       listView.setAdapter(adapter);

       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               try {

                   String  itemValue = (String) listView.getItemAtPosition(position);

               if (captureText != null){
                   captureText.onClickListner(itemValue);
               }

               dismiss();

           }catch (Exception e){
                   e.printStackTrace();
                   ShowToast showToast = new ShowToast();
                   showToast.toast("Please Select again", getActivity());
                   dismiss();
               }
           }
       });

       searchView.setQueryHint("Search here");
       searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               adapter.getFilter().filter(newText);
               return false;
           }
       });

       close.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               dismiss();
           }
       });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            captureText = (Capture) getActivity();
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }
}
