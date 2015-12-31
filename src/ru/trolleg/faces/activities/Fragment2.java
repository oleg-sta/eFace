package ru.trolleg.faces.activities;

import java.util.HashSet;
import java.util.Set;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.PersonForSearchAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class Fragment2 extends Fragment {
    FragmentAlbumManager fragmentAlbumManager;

    public Fragment2(FragmentAlbumManager fragmentAlbumManager) {
        this.fragmentAlbumManager = fragmentAlbumManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, null);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
        final ListView listView2 = (ListView) v.findViewById(R.id.list_search_people);
        final PersonForSearchAdapter adap = new PersonForSearchAdapter(getActivity(), dbHelper.getAllIdsPerson(0, true));
        listView2.setAdapter(adap);

        ImageView searchButton = (ImageView) v.findViewById(R.id.search_button_album);
        searchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Set<Integer> personIds = new HashSet<Integer>();
                Log.i("Fragment2", "checked " + adap.checked + " " + adap.men);
                for(Integer ch : adap.checked) {
                    personIds.add(adap.men.get(ch));
                }
                fragmentAlbumManager.fragment3.filterMan = personIds;
                
                FragmentTransaction transaction = fragmentAlbumManager.getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
                transaction.replace(R.id.fragment, fragmentAlbumManager.fragment3);
                transaction.commit();

            }
        });
        
        ImageView calendarButton = (ImageView) v.findViewById(R.id.calendar_search);
        calendarButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("Custom dialog");
                LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                        .inflate(R.layout.dialog_picker_dates, null);
                adb.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                adb.setPositiveButton("w", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });
                adb.setView(view);
                adb.create().show();
            }
        });
        return v;
    }
}