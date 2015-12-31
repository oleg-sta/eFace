package ru.trolleg.faces.activities;

import ru.trolleg.faces.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class FragmentAlbumManager extends Fragment {
    private AlbumGridFragment fragment1;
    private Fragment fragment2;
    public Fragment3 fragment3;
    private FragmentTransaction transaction;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rest = inflater.inflate(R.layout.albums_fragment, null);
        super.onCreate(savedInstanceState);
        
        fragment1 = new AlbumGridFragment();
        fragment2 = new Fragment2(this);
        fragment3 = new Fragment3();
        transaction = getFragmentManager().beginTransaction();
        
        // transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
        
        transaction.replace(R.id.fragment, fragment1);
        transaction.addToBackStack(null);
        
        transaction.commit();
        
        return rest;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_album, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_search2:
            transaction = getFragmentManager().beginTransaction();
            
             transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
             transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);

            if(fragment1.isVisible()){
                transaction.replace(R.id.fragment, fragment2);
            }else{
                transaction.replace(R.id.fragment, fragment1);
            }
            transaction.commit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }

    }
}
