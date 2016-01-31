package ru.flightlabs.eface.activities;

import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.adapters.FirstFacesOnPersonActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import ru.flightlabs.eface.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class PeopleFragment extends Fragment implements OnQueryTextListener, OnCloseListener   {
    
    public final static String UPDATE_PEOPLE = "ru.trolleg.update_people";
    public final static String UPDATE_FACES = "ru.trolleg.update_faces";

    View empty;
    
    FirstFacesOnPersonActivity adapterMans;
    DictionaryOpenHelper dbHelper;
    
    private BroadcastReceiver broadcastReceiver;
    
    private String filterName = null;
    private int sortMode = 0;
    private boolean sortAsc = true;
    
    public PeopleFragment() {
    }

    
    @Override
    public void onResume() {
        Log.i("PeopleFragment", "onResume");
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("PeopleFragment", "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("PeopleFragment", "broadcastReceiver broadcastReceiver");
                researchPeople();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,
                new IntentFilter(UPDATE_PEOPLE)
        );

    }

    @Override
    public void onStart() {
        Log.i("PeopleFragment", "onStart");
        super.onStart();
    }
    

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }


    @Override
    public void onStop() {
        Log.i("PeopleFragment", "onStop");
        super.onStop();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("PeopleFragment", "onCreateView");
        View rootView = inflater.inflate(R.layout.people_fragment, container, false);

        empty = rootView.findViewById(R.id.empty);
        dbHelper = new DictionaryOpenHelper(getActivity());
        adapterMans = new FirstFacesOnPersonActivity(getActivity(), new ArrayList<Integer>());
        final ListView listView2 = (ListView) rootView.findViewById(R.id.list_man);
        listView2.setAdapter(adapterMans);
        researchPeople();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i("PeopleFragment", "onCreateOptionsMenu");
        inflater.inflate(R.menu.people_menu, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getActivity().getString(R.string.query_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.sort_people_by_name:
            sortMode(0);
            return true;
        case R.id.sort_people_by_count:
            sortMode(1);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    private void sortMode(int i) {
        if (sortMode == i) {
            sortAsc = !sortAsc;
        }
        sortMode = i;
        researchPeople();
        
    }
    
    private void researchPeople() {
        adapterMans.clear();
        adapterMans.addAll(dbHelper.getAllIdsPerson(sortMode, sortAsc, filterName));
        adapterMans.notifyDataSetChanged();
        if (dbHelper.hasAnyMan()) {
            empty.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onQueryTextChange(String textNew) {
        Log.i("QUERY", "New text is " + textNew);
        filterName = textNew;
        researchPeople();
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String textNew) {
        Log.i("QUERY", "New text is1 " + textNew);
        return true;
    }


    @Override
    public boolean onClose() {
        filterName = null;
        researchPeople();
        return false;
    }
}
