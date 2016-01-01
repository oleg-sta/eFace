package ru.trolleg.faces.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.PersonForSearchAdapter;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Fragment2 extends Fragment {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MMM dd");
    private static final SimpleDateFormat DATE_FORMAT_S = new SimpleDateFormat("dd.MM.yyyy");
    Date startDates;
    Date endDateS;
    
    TextView periodView;
    
    boolean startdatePick;
    Date startDate;
    Date endDate;
    Button startDateButton;
    Button endDateButton;
    
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

        if (startDates == null) {
            startDates = new Date();
        }
        if (endDateS == null) {
            endDateS = new Date();
        }
        dbHelper.getMaxMin(startDates, endDateS);
        periodView = (TextView) v.findViewById(R.id.period_search2);
        updatePeriod();
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
                fragmentAlbumManager.fragment3.startDate = startDates;
                fragmentAlbumManager.fragment3.endDate = endDateS;
                
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
                startDate = startDates;
                endDate = endDateS;
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("Установите период");
                LinearLayout view = (LinearLayout) getActivity().getLayoutInflater()
                        .inflate(R.layout.dialog_picker_dates, null);
                startDateButton = (Button) view.findViewById(R.id.first_date);
                startDateButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startdatePick = true;
                        Calendar cal = Calendar.getInstance();
                        if (startDate != null) {
                            cal.setTime(startDate);
                        }
                        DatePickerDialog tpd = new DatePickerDialog(getActivity(), myCallBack, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                        tpd.show();
                    }
                });
                endDateButton = (Button) view.findViewById(R.id.last_date);
                adb.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                endDateButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startdatePick = false;
                        Calendar cal = Calendar.getInstance();
                        if (endDate != null) {
                            cal.setTime(endDate);
                        }
                        DatePickerDialog tpd = new DatePickerDialog(getActivity(), myCallBack, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                        tpd.show();
                    }
                });
                adb.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startDates = startDate;
                        endDateS = endDate;
                        updatePeriod();
                    }
                });
                adb.setView(view);
                adb.create().show();
                updateButton();
            }
        });
        return v;
    }
    
    OnDateSetListener myCallBack = new OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (startdatePick) {
                startDate = cal.getTime();
            } else {
                endDate = cal.getTime();
            }
            updateButton();
        }
    };
    
    public void updateButton() {
        if (startDate != null) {
            startDateButton.setText(DATE_FORMAT.format(startDate));
        }
        if (endDate != null) {
            endDateButton.setText(DATE_FORMAT.format(endDate));
        }
    }
    public void updatePeriod() {
        String text = "Пероид поиска: " + (startDates == null? "..." : DATE_FORMAT_S.format(startDates)) + "-" + (endDateS == null? "..." : DATE_FORMAT_S.format(endDateS));
        periodView.setText(text);
    }
}