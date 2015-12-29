package ru.trolleg.faces.activities;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FirstFacesOnPersonActivity;
import ru.trolleg.faces.adapters.PersonForSearchAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Fragment2 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, null);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
        final ListView listView2 = (ListView) v.findViewById(R.id.list_search_people);
        listView2.setAdapter(new PersonForSearchAdapter(getActivity(), dbHelper.getAllIdsPerson(0, true)));
        return v;
    }
}