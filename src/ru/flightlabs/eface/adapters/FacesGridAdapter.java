package ru.flightlabs.eface.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.flightlabs.eface.BitmapWorkerFaceCrop;
import ru.flightlabs.eface.DataHolder;
import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.activities.ShowCommonPhoto;
import ru.flightlabs.eface.data.Face;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Лица для распознавания
 * 
 * @author sov
 * 
 */
public class FacesGridAdapter extends ArrayAdapter<Integer> {

    public static final int WIDTH_NUM_PICS = 4;
    private final Activity context;
    public final List<Integer> faces;
    public final Set<Integer> checked = new HashSet<Integer>(); // TODO лучше faces id хранить, так независимо будет от единичного убирания

    public FacesGridAdapter(Activity context, List<Integer> faces) {
        super(context, R.layout.one_face, faces);
        this.faces = faces;
        this.context = context;
    }

    
    @Override
    public int getCount() {
        if (faces.size() == 0) {
            return 0;
        }
        return faces.size() + WIDTH_NUM_PICS + 1;
    }


    @Override
    public int getItemViewType(int position) {
        return (position < WIDTH_NUM_PICS || position >= (faces.size() + WIDTH_NUM_PICS)) ? 1 : 0;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public Integer getItem(int position) {
        return (position < WIDTH_NUM_PICS || position >= (faces.size() + WIDTH_NUM_PICS)) ?
                null : faces.get(position - WIDTH_NUM_PICS);
    }
    
    
    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        final int position = pos - WIDTH_NUM_PICS;
        if (position < 0 || position >= faces.size()) {
            if (convertView == null) {
                convertView = new View(context);
            }
            if (position < 0) {
                convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 0));
            } else if (position / WIDTH_NUM_PICS == (faces.size() - 1) / WIDTH_NUM_PICS) {
                // TODO не совсем верный размер вычисляется, но благодярю ему добавляеи дополнительный паддинг снизу
                convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, context.getResources().getDisplayMetrics().widthPixels / WIDTH_NUM_PICS));
            } else {
                convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, DataHolder.dp2Px(80, context)));
            }
            return convertView;
        }
        
        ViewHolder2 holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face, null, true);
            holder = new ViewHolder2();
            holder.view = (ImageView)convertView.findViewById(R.id.one_face1);
            holder.view2 = (ImageView)convertView.findViewById(R.id.checked);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder2) convertView.getTag();
            // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // нельзя так делать, list  в адаптере могут сменить
            if (holder.position == position) {
//                return convertView;
            }
        }
        holder.position = position;
        final ImageView view2 = holder.view2;
        view2.setVisibility(checked.contains(position)? View.VISIBLE : View.INVISIBLE);
        ImageView view = holder.view;
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        if (face == null) {
            view.setImageBitmap(null);
            view.setOnClickListener(null);
            view.setOnTouchListener(null);
            view.setOnLongClickListener(null);
        } else {
            view.setAlpha(face.probability);
            BitmapWorkerFaceCrop.loadImage(face, context, holder, position);
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String photo = dbHelper.getPhotoPathByFaceId(faceId);
                    Intent personIntent = new Intent(context, ShowCommonPhoto.class);
                    personIntent.putExtra(ShowCommonPhoto.PHOTO, photo);
                    context.startActivity(personIntent);
                    return true;
                }
            });
            view.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            Drawable drawable = view.getDrawable();
                            boolean hasImage = (drawable != null);
                            if (hasImage && (drawable instanceof BitmapDrawable)) {
                                hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
                            }
                            if (hasImage) {
                                view.setBackgroundColor(Color.WHITE);
                                view.setPadding(5, 5, 5, 5);
                                view.invalidate();
                            }
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            view.setPadding(0, 0, 0, 0);
                            view.invalidate();
                            break;
                        }
                    }
                    return false;
                }

            });
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    view2.setVisibility(view2.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    if (view2.getVisibility() == View.VISIBLE) {
                        checked.add(position);
                    } else {
                        checked.remove(position);
                    }

                }
            });
        }
        return convertView;
    }
    
    public static class ViewHolder2 {
        public ImageView view;
        public ImageView view2;
        public int position;
    }

}