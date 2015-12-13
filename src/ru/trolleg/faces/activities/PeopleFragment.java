package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FirstFacesOnPersonActivity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class PeopleFragment extends Fragment implements YourFragmentInterface {
    FirstFacesOnPersonActivity adapterMans;
    DictionaryOpenHelper dbHelper;
    
    public PeopleFragment() {
    }

    
    @Override
    public void onResume() {
        adapterMans.clear();
        adapterMans.addAll(dbHelper.getAllIdsPerson());
        adapterMans.notifyDataSetChanged();
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.people_fragment, container, false);
        
        dbHelper = new DictionaryOpenHelper(getActivity());
        adapterMans = new FirstFacesOnPersonActivity(getActivity(), dbHelper.getAllIdsPerson());
        final ListView listView2 = (ListView) rootView.findViewById(R.id.list_man);
        listView2.setEmptyView(rootView.findViewById(R.id.empty));
        listView2.setAdapter(adapterMans);
        return rootView;
    }


    @Override
    public void fragmentBecameVisible() {
        adapterMans.clear();
        adapterMans.addAll(dbHelper.getAllIdsPerson());
        adapterMans.notifyDataSetChanged();
    }
}
