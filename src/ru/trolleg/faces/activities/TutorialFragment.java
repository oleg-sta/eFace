package ru.trolleg.faces.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.trolleg.faces.R;

/**
 * Created by sov on 24.01.2016.
 */
public class TutorialFragment extends Fragment {
    private static final String TUTORIAL_SCREEN_TYPE_BUNDLE_KEY = "screen_type";
    private int position;

    public static final TutorialFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(TUTORIAL_SCREEN_TYPE_BUNDLE_KEY, position);
        TutorialFragment fragment = new TutorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.position = getArguments().getInt(TUTORIAL_SCREEN_TYPE_BUNDLE_KEY);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        switch (position) {
            case 0:
                return inflater.inflate(R.layout.tutorial_1, null);
            case 1:
                return inflater.inflate(R.layout.tutorial_2, null);
            case 2:
                return inflater.inflate(R.layout.tutorial_3, null);
            case 3:
                return inflater.inflate(R.layout.tutorial_4, null);
            case 4:
                return inflater.inflate(R.layout.tutorial_5, null);
            default:
                return getTutorial6(inflater, getActivity());

        }
    }

    private View getTutorial6(LayoutInflater inflater, final Context context) {
        View v = inflater.inflate(R.layout.tutorial_6, null);
        View vStart = v.findViewById(R.id.start);
        vStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        v.findViewById(R.id.mailto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", context.getString(R.string.mail_to), null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "eFace");
                startActivity(Intent.createChooser(intent, context.getString(R.string.send_mail_with)));
            }
        });
        return v;
    }

}
