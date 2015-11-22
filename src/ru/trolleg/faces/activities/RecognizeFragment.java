package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.DragOnTrashListener;
import ru.trolleg.faces.DragOverListMen;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.NotificationReceiver;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter;
import ru.trolleg.faces.adapters.PersonListToRecogniseAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RecognizeFragment extends Fragment implements NotificationReceiver.Listener {
    
    DictionaryOpenHelper dbHelper;
    public FacesGridAdapter adapterFaces;
    public PersonListToRecogniseAdapter adapterMans;
    public Integer currentMan = null;
    ImageView button; 
    
    public RecognizeFragment() {
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        Log.v("RecognizeFragment", "onCreate");
        dbHelper = new DictionaryOpenHelper(getActivity());
        // SQLiteDatabase db = dbHelper.getReadableDatabase();
        // dbHelper.onUpgrade(db, 1, 1); // ��������

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        final Context context = getActivity();
        final Context d = getActivity();
        final RecognizeFragment this1 = this;

        adapterFaces = new FacesGridAdapter(getActivity(), dbHelper.getAllIdsFacesForPerson(currentMan));
        final GridView listView = (GridView) rootView.findViewById(R.id.listFaces);
        listView.setAdapter(adapterFaces);
        adapterFaces.notifyDataSetChanged();
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        
        final LinearLayout la1 = (LinearLayout) rootView.findViewById(R.id.mainLay);
        final View vi1 = rootView.findViewById(R.id.vie);
        la1.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                    int oldBottom) {
                Log.i("MainActivity", "size2 " + la1.getMeasuredWidth());
                int num = DataHolder.px2Dp(la1.getMeasuredWidth(), d) / (80 + 2)- 1;
                listView.getLayoutParams().width = num * DataHolder.dp2Px(80 + 2, d);
                listView.setNumColumns(num);
                vi1.getLayoutParams().width = la1.getMeasuredWidth() - DataHolder.dp2Px((80 +2)*num + 80, d); 
            }
        });
        
        adapterMans = new PersonListToRecogniseAdapter(getActivity(), dbHelper.getAllIdsPerson(), this);
        final ListView listView2 = (ListView) rootView.findViewById(R.id.listOfMan);
        listView2.setAdapter(adapterMans);
        adapterMans.notifyDataSetChanged();
        
        button = (ImageView) rootView.findViewById(R.id.start_stop);
        if (FaceFinderService.buttonStart) {
            button.setImageResource(R.drawable.pause);
        } else {
            button.setImageResource(R.drawable.start);
        }
        FaceFinderService inst = FaceFinderService.getInstance();
        if (inst != null) {
            NotificationReceiver receiver = new NotificationReceiver(new Handler());
            receiver.setListener(this1);
            inst.setReceiver(receiver);
        }
        button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                FaceFinderService.buttonStart = !FaceFinderService.buttonStart;
                if (!FaceFinderService.buttonStart) {
                    button.setImageResource(R.drawable.start);
                } else {
                    button.setImageResource(R.drawable.pause);
                    // TODO нельзя запускать, если работает
                    if (FaceFinderService.getInstance() == null) {
                        Intent intent = new Intent(context, FaceFinderService.class);
                        NotificationReceiver receiver = new NotificationReceiver(new Handler());
                        receiver.setListener(this1);
                        intent.putExtra("receiver", receiver);
                        getActivity().startService(intent);
                    }
                }
            }
        });
        ImageView im2 = (ImageView) rootView.findViewById(R.id.add_face2);
        im2.setImageResource(R.drawable.add_face);
        im2.setOnDragListener(new DragOverListMen(this));
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
                            adapterMans.add(newPerson);
                            PersonListToRecogniseAdapter.moveFaces(this1, newPerson, dbHelper);
                            adapterMans.notifyDataSetChanged();
                        }
                    }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("DragOverListMen", "No");
                        }
                    });
                    // Create the AlertDialog object and return it
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
        
        ImageView im = (ImageView) rootView.findViewById(R.id.first_face);
        im.setImageResource(R.drawable.full_trash);
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
        im.setOnDragListener(new DragOnTrashListener(this));

        return rootView;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String photo = resultData.getString("photo");
        String message = resultData.getString("message");
        String progressStr = resultData.getString("progress");
        boolean ended = resultData.getBoolean("ended", false);
        if (ended) {
            button.setImageResource(R.drawable.start);
        }
        if (photo != null && currentMan == null) {
            adapterFaces.addAll(dbHelper.getIdsFacesForPhoto(photo));
            adapterFaces.notifyDataSetChanged();
        }
        if (message != null && getView() != null) {
            int progress = 0;
            if (progressStr != null) {
                progress = Integer.valueOf(progressStr);
            }
            TextView text = (TextView) getView().findViewById(R.id.text_message);
            text.setText(message);

            ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar);
            bar.setVisibility(View.VISIBLE);
            bar.setProgress(progress);
        }
        adapterFaces.notifyDataSetChanged();
        
    }
    
    public void setCurrentMan(Integer manId) {
        currentMan = manId;
        String name = dbHelper.getPersonName(manId);
        if (name == null) {
            name = "Лица";
        }
        TextView nameView = (TextView) getView().findViewById(R.id.name);
        nameView.setText(name);
        adapterFaces.clear();
        adapterFaces.checked.clear();
        adapterFaces.addAll(dbHelper.getAllIdsFacesForPerson(currentMan));
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        adapterFaces.notifyDataSetChanged();
    }

}
