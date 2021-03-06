package ru.flightlabs.eface.activities;

import ru.flightlabs.eface.DataHolder;
import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.FaceFinderService;
import ru.flightlabs.eface.FaceFinderService.Operation;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.adapters.FacesGridAdapter;
import ru.flightlabs.eface.adapters.PersonListToRecogniseAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import ru.flightlabs.eface.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RecognizeFragment extends Fragment {
    
    private Intent intent;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiver2;

    public static final String PHOTO = "photo";
    public static final String ENDED = "ended";

    DictionaryOpenHelper dbHelper;
    public FacesGridAdapter adapterFaces;
    public PersonListToRecogniseAdapter adapterMans;
    public Integer currentMan = null;
    Context context;
    MenuItem stMenu;

    View empty;

    TextView phoCOuntTw;
    ProgressBar phoCOuntTwPr;
    TextView phoProCOuntTw;
    TextView facesCountTw;
    
    
    public RecognizeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapterMans.notifyDataSetChanged();
            }
        };
        broadcastReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onReceiveResult2(context, intent);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver),
                new IntentFilter(PeopleFragment.UPDATE_PEOPLE)
        );
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver2),
                new IntentFilter(PeopleFragment.UPDATE_FACES)
        );
    }
    @Override
    public void onStart() {
        super.onStart();
        if (!FaceFinderService.buttonStart) {
            if (!FaceFinderService.instance) {
                FaceFinderService.instance = true;
                Intent intent = new Intent(context, FaceFinderService.class);
                intent.putExtra(FaceFinderService.OPER, Operation.FIND_PHOTOS);
                getActivity().startService(intent);
            }
        }
    }
    

    @Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver2);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recognition_fragment, container, false);
        dbHelper = new DictionaryOpenHelper(getActivity());

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        context = getActivity();
        final Context d = getActivity();
        final RecognizeFragment this1 = this;

        adapterFaces = new FacesGridAdapter(getActivity(), dbHelper.getAllIdsFacesForPerson(currentMan));
        final GridView listView = (GridView) rootView.findViewById(R.id.listFaces);
        listView.setColumnWidth(getResources().getDisplayMetrics().widthPixels / FacesGridAdapter.WIDTH_NUM_PICS);
        empty = rootView.findViewById(R.id.empty);
        setEmpty();
        listView.setAdapter(adapterFaces);
        adapterFaces.notifyDataSetChanged();
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        
        adapterMans = new PersonListToRecogniseAdapter(getActivity(), dbHelper.getAllIdsPerson(0, true), this);
        android.support.v4.view.ViewPager gal = (android.support.v4.view.ViewPager) rootView.findViewById(R.id.aaa);
        gal.setAdapter(adapterMans);

        phoCOuntTw = (TextView) rootView.findViewById(R.id.all_photos);
        phoCOuntTwPr = (ProgressBar) rootView.findViewById(R.id.all_photos_progress);
        phoProCOuntTw = (TextView) rootView.findViewById(R.id.photos_processed);
        facesCountTw = (TextView) rootView.findViewById(R.id.face_count);
        if (DataHolder.photoCount > 0) {
            phoCOuntTw.setText("" + DataHolder.photoCount);
            phoCOuntTw.setVisibility(View.VISIBLE);
            phoCOuntTwPr.setVisibility(View.INVISIBLE);
        }
        phoProCOuntTw.setText("" + DataHolder.photoProcessedCount);
        facesCountTw.setText("" + DataHolder.facesCount);
        Log.i("RecognizeFragment", "stats " + DataHolder.photoCount + " " + DataHolder.photoProcessedCount + " " + DataHolder.facesCount);

        LinearLayout men_lay = (LinearLayout) rootView.findViewById(R.id.men_lay);
        men_lay.getLayoutParams().height = getResources().getDisplayMetrics().widthPixels / (FacesGridAdapter.WIDTH_NUM_PICS + 1) + DataHolder.dp2Px(16 + 16, context);
        
        ImageView im2 = (ImageView) rootView.findViewById(R.id.add_face2);
        im2.setImageResource(R.drawable.add_face);
        im2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO add new, если нет выделенных людей, то переходим на неопознанных
                if (adapterFaces.checked.isEmpty()) {
                    if (this1.currentMan == null) {
                        Toast.makeText(getActivity(), R.string.first_pick_faces, Toast.LENGTH_SHORT).show();
                    } else {
                        this1.setCurrentMan(null);
                    }
                } else {
                    final EditText input = new EditText(getActivity());
                    input.setTextColor(Color.BLACK);
                    input.setHint(R.string.enter_name);
                    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(String.format(d.getString(R.string.add_man), this1.adapterFaces.checked.size()));
                    builder.setView(input).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String newName = input.getText().toString();
                            if ("".equals(newName)) {
                                newName = d.getString(R.string.without_name);
                            }
                            Integer i1 = null;
                            for (Integer i : adapterFaces.checked) {
                                i1 = i;
                                break;
                            }
                            int faceId = adapterFaces.faces.get(i1);

                            final int newPerson = dbHelper.addPerson(newName, faceId);
                            adapterMans.men.add(newPerson);
                            PersonListToRecogniseAdapter.moveFaces(this1, newPerson, dbHelper);
                            adapterMans.notifyDataSetChanged();

                            Intent intent = new Intent(PeopleFragment.UPDATE_PEOPLE);
                            broadcastManager.sendBroadcast(intent);
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("DragOverListMen", "No");
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    alertDialog.show();
                }
            }
        });
        im2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("RecognizeFragment", "setOnLongClickListener");
                if (currentMan == null) {
                    if (adapterFaces.checked.isEmpty()) {
                        for (int i = 0; i < adapterFaces.faces.size(); i++) {
                            adapterFaces.checked.add(i);
                        }
                    } else {
                        adapterFaces.checked.clear();
                    }
                    adapterFaces.notifyDataSetChanged();
                }
                return true;
            }
        });
        
        ImageView im = (ImageView) rootView.findViewById(R.id.trash);
        im.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int thrashid = dbHelper.getOrCreatePerson(MainActivity.NO_FACES);
                if (adapterFaces.checked.isEmpty()) {
                    this1.setCurrentMan(thrashid);
                } else {
                    if (currentMan == null || currentMan != thrashid) {
                        PersonListToRecogniseAdapter.moveFaces(this1, thrashid, dbHelper);
                        Intent intent = new Intent(PeopleFragment.UPDATE_PEOPLE);
                        broadcastManager.sendBroadcast(intent);
                        SharedPreferences prefs = getActivity().getSharedPreferences("first_start", Context.MODE_PRIVATE);
                        if (prefs.getBoolean("first_trash", true)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("first_trash", false);
                            editor.commit();
                            Toast.makeText(getActivity(), R.string.trash_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        return rootView;
    }

    private void setEmpty() {
        if (dbHelper.hasAnyFace()) {
            empty.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    public void onReceiveResult2(Context context2, Intent intent2) {
        setEmpty();
        String photo = intent2.getStringExtra(PHOTO);
        boolean ended = intent2.getBooleanExtra(ENDED, false);
        if (ended && stMenu != null) {
            stMenu.setIcon(R.drawable.start);
        }
        phoCOuntTwPr.setVisibility(View.INVISIBLE);
        phoCOuntTw.setText("" + DataHolder.photoCount);
        phoCOuntTw.setVisibility(View.VISIBLE);
        phoProCOuntTw.setText("" + DataHolder.photoProcessedCount);
        facesCountTw.setText("" + DataHolder.facesCount);
        if (photo != null && currentMan == null) {
            Log.i("sss", "s " + adapterFaces + " " + this.adapterFaces);
            adapterFaces.addAll(dbHelper.getIdsFacesForPhoto(photo));
            adapterFaces.notifyDataSetChanged();
        }
        
    }
    
    public void setCurrentMan(Integer manId) {
        currentMan = manId;
        adapterFaces.clear();
        adapterFaces.checked.clear();
        adapterFaces.addAll(dbHelper.getAllIdsFacesForPerson(currentMan));
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        adapterFaces.notifyDataSetChanged();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i("RecognizeFragment", "onCreateOptionsMenu");
        inflater.inflate(R.menu.recognition, menu);
        stMenu = menu.findItem(R.id.miCompose);
        if (!FaceFinderService.buttonStart) {
            stMenu.setIcon(R.drawable.start);
        } else {
            stMenu.setIcon(R.drawable.pause);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onPause() {
        Log.i("RecognizeFragment", "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i("RecognizeFragment", "onResume");
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.miCompose:
            onClickStart22();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void onClickStart22() {
        Log.i("FaceFinder", "onClickStart22");
        FaceFinderService.buttonStart = !FaceFinderService.buttonStart;
        if (!FaceFinderService.buttonStart) {
            stMenu.setIcon(R.drawable.start);
        } else {
            Log.i("FaceFinder", "onClickStart222 " + FaceFinderService.buttonStart);
            stMenu.setIcon(R.drawable.pause);
            // TODO нельзя запускать, если работает
            if (!FaceFinderService.instance) {
                Log.i("FaceFinder", "onClickStart223 " + FaceFinderService.instance);
                FaceFinderService.instance = true;
                Intent intent = new Intent(context, FaceFinderService.class);
                getActivity().startService(intent);
            }
        }
    }

    public boolean backPress() {
        Log.i("RecognizeFragment", "backPress " + currentMan + " " + this);
        if (currentMan != null) {
            Log.i("RecognizeFragment", "backPress2");
            setCurrentMan(null);
            return true;
        }
        return false;
    }
}
