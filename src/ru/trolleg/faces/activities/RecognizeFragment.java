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
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
                this1.setCurrentMan(null);
            }
        });
        
        ImageView im = (ImageView) rootView.findViewById(R.id.first_face);
        im.setImageResource(R.drawable.full_trash);
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
        if (message != null) {
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
        adapterFaces.clear();
        adapterFaces.addAll(dbHelper.getAllIdsFacesForPerson(currentMan));
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        adapterFaces.notifyDataSetChanged();
    }

}
