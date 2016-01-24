package ru.trolleg.faces.activities;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FirstFacesOnPersonActivity;
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
import ru.trolleg.faces.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PeopleFragment extends Fragment implements OnQueryTextListener, OnCloseListener   {
    
    public final static String UPDATE_PEOPLE = "ru.trolleg.update_people";
    public final static String UPDATE_FACES = "ru.trolleg.update_faces";
    
    
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
        //researchPeople();
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
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,
//                new IntentFilter(UPDATE_PEOPLE)
//        );
    }
    

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }


    @Override
    public void onStop() {
        Log.i("PeopleFragment", "onStop");
        super.onStop();
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("PeopleFragment", "onCreateView");
        View rootView = inflater.inflate(R.layout.people_fragment, container, false);
        
        dbHelper = new DictionaryOpenHelper(getActivity());
        adapterMans = new FirstFacesOnPersonActivity(getActivity(), dbHelper.getAllIdsPerson(sortMode, sortAsc));
        final ListView listView2 = (ListView) rootView.findViewById(R.id.list_man);
        listView2.setEmptyView(rootView.findViewById(R.id.empty));
        listView2.setAdapter(adapterMans);
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
//        adapterMans.clear();
//        adapterMans.addAll(dbHelper.getAllIdsPerson(sortMode, textNew));
//        adapterMans.notifyDataSetChanged();
        return true;
    }


    @Override
    public boolean onClose() {
        filterName = null;
        researchPeople();
        return false;
    }
}
