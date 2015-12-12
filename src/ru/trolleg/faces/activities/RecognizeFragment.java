package ru.trolleg.faces.activities;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.FaceFinderService.Operation;
import ru.trolleg.faces.NotificationReceiver;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter;
import ru.trolleg.faces.adapters.GalleryAdapter;
import ru.trolleg.faces.adapters.PersonListToRecogniseAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RecognizeFragment extends Fragment implements NotificationReceiver.Listener {
    
    DictionaryOpenHelper dbHelper;
    public FacesGridAdapter adapterFaces;
    public PersonListToRecogniseAdapter adapterMans;
    public Integer currentMan = null;
    Context context;
    //ImageView button;
    
    public RecognizeFragment() {
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
        listView.setColumnWidth(getResources().getDisplayMetrics().widthPixels / 4);
        //listView.set
        listView.setAdapter(adapterFaces);
        adapterFaces.notifyDataSetChanged();
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        
        GalleryAdapter j = new GalleryAdapter(getActivity(), dbHelper.getAllIdsFaces());
        adapterMans = new PersonListToRecogniseAdapter(getActivity(), dbHelper.getAllIdsPerson(), this);

        android.support.v4.view.ViewPager gal = (android.support.v4.view.ViewPager) rootView.findViewById(R.id.aaa);
        
        gal.setAdapter(adapterMans);

        
        //button = (ImageView) rootView.findViewById(R.id.start_stop);
        int newPhotos = dbHelper.getCountNewPhotos();
//        if (newPhotos == 0) {
//            button.setAlpha(0.5f);
//        } else {
//            button.setAlpha(1f);
//        }
//        if (FaceFinderService.buttonStart) {
//            button.setImageResource(R.drawable.pause);
//        } else {
//            button.setImageResource(R.drawable.start);
//        }
        FaceFinderService inst = FaceFinderService.getInstance();
        if (inst != null) {
            NotificationReceiver receiver = new NotificationReceiver(new Handler());
            receiver.setListener(this1);
            inst.setReceiver(receiver);
        }
//        button.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                FaceFinderService.buttonStart = !FaceFinderService.buttonStart;
//                if (!FaceFinderService.buttonStart) {
//                    button.setImageResource(R.drawable.start);
//                } else {
//                    button.setImageResource(R.drawable.pause);
//                    // TODO нельзя запускать, если работает
//                    if (FaceFinderService.getInstance() == null) {
//                        Intent intent = new Intent(context, FaceFinderService.class);
//                        NotificationReceiver receiver = new NotificationReceiver(new Handler());
//                        receiver.setListener(this1);
//                        intent.putExtra("receiver", receiver);
//                        getActivity().startService(intent);
//                    }
//                }
//            }
//        });
        
        if (!FaceFinderService.buttonStart) {
            Intent intent = new Intent(context, FaceFinderService.class);
            NotificationReceiver receiver = new NotificationReceiver(new Handler());
            receiver.setListener(this1);
            intent.putExtra("receiver", receiver);
            intent.putExtra(FaceFinderService.OPER, Operation.FIND_PHOTOS);
            getActivity().startService(intent);
        }
            
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

    public void buttonStart(Context context) {
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
                NotificationReceiver receiver = new NotificationReceiver(new Handler());
                receiver.setListener(this);
                intent.putExtra("receiver", receiver);
                context.startService(intent);
            }
        }
    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String photo = resultData.getString("photo");
        String message = resultData.getString("message");
        String progressStr = resultData.getString("progress");
        boolean ended = resultData.getBoolean("ended", false);
        if (ended) {
            //button.setImageResource(R.drawable.start);
        }
        if (photo != null && currentMan == null) {
            Log.i("sss", "s " + adapterFaces + " " + this.adapterFaces);
            adapterFaces.addAll(dbHelper.getIdsFacesForPhoto(photo));
            adapterFaces.notifyDataSetChanged();
        }
        if (message != null && getView() != null) {
            ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar);
            int progress = 0;
            if (progressStr != null) {
                progress = Integer.valueOf(progressStr);
//                if (progress < 100) {
//                    if (button != null) {
//                        bar.setVisibility(View.VISIBLE);
//                        button.setAlpha(1f);
//                    }
//                } else {
//                    if (button != null) {
//                        bar.setVisibility(View.GONE);
//                        //button.setAlpha(0.5f);
//                    }
//                }
            }

            //bar.setVisibility(View.VISIBLE);
            //bar.setProgress(progress);
        }
        // show button start
        //adapterFaces.notifyDataSetChanged();
        
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

}
