package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter2;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PhotoFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.comon_photo_pager, container, false);
        TextView nameView = (TextView) rootView.findViewById(R.id.name_man);
        final ViewPager mPager = (ViewPager) rootView.findViewById(R.id.pager);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
        List<String> photos = dbHelper.getAllPhotos();
        final PagerAdapter mPagerAdapter = new CommonPhotoAdapter2(getActivity(), photos, nameView);
        mPager.setAdapter(mPagerAdapter);
        
        return rootView;
    }

}
