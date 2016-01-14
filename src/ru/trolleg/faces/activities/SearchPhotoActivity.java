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
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchPhotoActivity extends AppCompatActivity {
    
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        final SearchPhotoActivity fragment2 = this;
        CheckBox allCheck = (CheckBox) findViewById(R.id.all_check);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        final ListView listView2 = (ListView) findViewById(R.id.list_search_people);
        final PersonForSearchAdapter adap = new PersonForSearchAdapter(this, dbHelper.getAllIdsPerson(0, true));
        listView2.setAdapter(adap);
        allCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adap.checkAll(isChecked);
                adap.notifyDataSetChanged();
            }
        });

        if (startDates == null) {
            startDates = new Date();
        }
        if (endDateS == null) {
            endDateS = new Date();
        }
        dbHelper.getMaxMin(startDates, endDateS);
        periodView = (TextView) findViewById(R.id.period_search2);
        updatePeriod();
        ImageView searchButton = (ImageView) findViewById(R.id.search_button_album);
        searchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Set<Integer> personIds = new HashSet<Integer>();
                Log.i("Fragment2", "checked " + adap.checked + " " + adap.men);
                for(Integer ch : adap.checked) {
                    personIds.add(adap.men.get(ch));
                }

                Intent searchIntent = new Intent(fragment2, ShowSearchResultActivity.class);
                if (startDates != null) {
                    searchIntent.putExtra(ShowSearchResultActivity.START_DATE, startDates.getTime());
                }
                if (endDateS != null) {
                    searchIntent.putExtra(ShowSearchResultActivity.END_DATE, endDateS.getTime());
                }
                int[] i = new int[personIds.size()];
                int k = 0;
                for (int j : personIds) {
                    i[k] = j;
                    k++;
                }
                searchIntent.putExtra(ShowSearchResultActivity.PERSON_IDS, i);
                fragment2.startActivity(searchIntent);

            }
        });
        
        ImageView calendarButton = (ImageView) findViewById(R.id.calendar_search);
        calendarButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startDate = startDates;
                endDate = endDateS;
                AlertDialog.Builder adb = new AlertDialog.Builder(fragment2);
                adb.setTitle("Установите период");
                LinearLayout view = (LinearLayout) getLayoutInflater()
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
                        DatePickerDialog tpd = new DatePickerDialog(fragment2, myCallBack, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
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
                        DatePickerDialog tpd = new DatePickerDialog(fragment2, myCallBack, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
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