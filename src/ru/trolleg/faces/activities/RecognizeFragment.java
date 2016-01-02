package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.FaceFinderService.Operation;
import ru.trolleg.faces.NotificationReceiver;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter;
import ru.trolleg.faces.adapters.GalleryAdapter;
import ru.trolleg.faces.adapters.PersonListToRecogniseAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

public class RecognizeFragment extends Fragment {
    
    private Intent intent;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiver2;
    
    DictionaryOpenHelper dbHelper;
    public FacesGridAdapter adapterFaces;
    public PersonListToRecogniseAdapter adapterMans;
    public Integer currentMan = null;
    Context context;
    //ImageView button;
    MenuItem stMenu;
    //protected ImageView startMenu;
    
    TextView phoCOuntTw;
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
                Log.i("RecognizeFragment", "broadcastReceiver2 onReceive");
                onReceiveResult2(context, intent);
            }
        };
    }
    @Override
    public void onStart() {
        Log.v("RecognizeFragment", "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver),
                new IntentFilter(PeopleFragment.UPDATE_PEOPLE)
        );
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver2),
                new IntentFilter(PeopleFragment.UPDATE_FACES)
        );
        if (!FaceFinderService.buttonStart) {
            Log.i("RecognizeFragment", "!FaceFinderService.buttonStart");
            Intent intent = new Intent(context, FaceFinderService.class);
            intent.putExtra(FaceFinderService.OPER, Operation.FIND_PHOTOS);
            getActivity().startService(intent);
        }
    }
    

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver2);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recognition_fragment, container, false);
        Log.v("RecognizeFragment", "onCreate");
        dbHelper = new DictionaryOpenHelper(getActivity());

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        context = getActivity();
        Log.i("RecognizeFragment", "context " + context);
        final Context d = getActivity();
        final RecognizeFragment this1 = this;

        adapterFaces = new FacesGridAdapter(getActivity(), dbHelper.getAllIdsFacesForPerson(currentMan));
        final GridView listView = (GridView) rootView.findViewById(R.id.listFaces);
        listView.setColumnWidth(getResources().getDisplayMetrics().widthPixels / FacesGridAdapter.WIDTH_NUM_PICS);
        //listView.set
        listView.setAdapter(adapterFaces);
        adapterFaces.notifyDataSetChanged();
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        
        GalleryAdapter j = new GalleryAdapter(getActivity(), dbHelper.getAllIdsFaces());
        adapterMans = new PersonListToRecogniseAdapter(getActivity(), dbHelper.getAllIdsPerson(0, true), this);

        android.support.v4.view.ViewPager gal = (android.support.v4.view.ViewPager) rootView.findViewById(R.id.aaa);
        
        gal.setAdapter(adapterMans);

        phoCOuntTw = (TextView) rootView.findViewById(R.id.all_photos);
        phoProCOuntTw = (TextView) rootView.findViewById(R.id.photos_processed);
        facesCountTw = (TextView) rootView.findViewById(R.id.face_count);
        phoCOuntTw.setText("" + DataHolder.photoCount);
        phoProCOuntTw.setText("" + DataHolder.photoProcessedCount);
        facesCountTw.setText("" + DataHolder.facesCount);
        Log.i("RecognizeFragment", "stats " + DataHolder.photoCount + " "+ DataHolder.photoProcessedCount + " " + DataHolder.facesCount);
        
           
        LinearLayout men_lay = (LinearLayout) rootView.findViewById(R.id.men_lay);
        men_lay.getLayoutParams().height = getResources().getDisplayMetrics().widthPixels / (FacesGridAdapter.WIDTH_NUM_PICS + 1) + DataHolder.dp2Px(16, context);
        
        ImageView im2 = (ImageView) rootView.findViewById(R.id.add_face2);
        im2.setImageResource(R.drawable.add_face);
        im2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO add new, если нет выделенных людей, то переходим на неопознанных
                if (adapterFaces.checked.isEmpty()) {
                    if (this1.currentMan == null) {
                        Toast.makeText(getActivity(), "Сначала выделите лица.", Toast.LENGTH_SHORT).show();
                    } else {
                        this1.setCurrentMan(null);
                    }
                } else {
                    final EditText input = new EditText(getActivity());
                    input.setTextColor(Color.BLACK);
                    input.setHint("Введите имя");
                    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Добавление человека, выделено лиц - " + this1.adapterFaces.checked.size());
                    builder.setView(input).setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String newName =  input.getText().toString();
                            if ("".equals(newName)) {
                                newName = "Без имени";
                            }
                            final int newPerson = dbHelper.addPerson(newName);
                            adapterMans.men.add(newPerson);
                            PersonListToRecogniseAdapter.moveFaces(this1, newPerson, dbHelper);
                            adapterMans.notifyDataSetChanged();
                            
                            Intent intent = new Intent(PeopleFragment.UPDATE_PEOPLE);
                            broadcastManager.sendBroadcast(intent);
                        }
                    }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("DragOverListMen", "No");
                        }
                    });
                    // Create the AlertDialog object and return it
                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    alertDialog.show();
                }
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
                    }
                }
            }
        });

        return rootView;
    }

    public boolean buttonStart(Context context) {
        FaceFinderService.buttonStart = !FaceFinderService.buttonStart;
        if (!FaceFinderService.buttonStart) {
            //button.setImageResource(R.drawable.start);
        } else {
            //button.setImageResource(R.drawable.pause);
            // TODO нельзя запускать, если работает
            if (FaceFinderService.getInstance() == null) {
                Log.i("RecognizeFragment", "" + context);
                Log.i("RecognizeFragment", "" + this + " " + this.adapterFaces);
                Intent intent = new Intent(context, FaceFinderService.class);
                context.startService(intent);
            }
        }
        return FaceFinderService.buttonStart;
    }
    
    public void onReceiveResult2(Context context2, Intent intent2) {
        String photo = intent2.getStringExtra("photo");
        boolean ended = intent2.getBooleanExtra("ended", false);
        if (ended && stMenu != null) {
            stMenu.setIcon(R.drawable.start);
        }
        Log.i("RecognizeFragment", "stats22 " + DataHolder.photoCount + " "+ DataHolder.photoProcessedCount + " " + DataHolder.facesCount);
        phoCOuntTw.setText("" + DataHolder.photoCount);
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
        String name = dbHelper.getPersonName(manId);
        if (name == null) {
            name = "Лица";
        }
        adapterFaces.clear();
        adapterFaces.checked.clear();
        adapterFaces.addAll(dbHelper.getAllIdsFacesForPerson(currentMan));
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        adapterFaces.notifyDataSetChanged();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i("PeopleFragment", "onCreateOptionsMenu");
        inflater.inflate(R.menu.recognition, menu);
        stMenu = menu.findItem(R.id.miCompose);
        super.onCreateOptionsMenu(menu, inflater);
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
        FaceFinderService.buttonStart = !FaceFinderService.buttonStart;
        if (!FaceFinderService.buttonStart) {
            stMenu.setIcon(R.drawable.start);
        } else {
            stMenu.setIcon(R.drawable.pause);
            // TODO нельзя запускать, если работает
            if (FaceFinderService.getInstance() == null) {
                Intent intent = new Intent(context, FaceFinderService.class);
                getActivity().startService(intent);
            }
        }
    }

}
