package ru.trolleg.faces.activities;

import ru.trolleg.faces.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

/**
 * ����������� �������
 * @author sov
 *
 */
public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        TextView r = (TextView) findViewById(R.id.help_context);
        
        Spanned spannedText = Html.fromHtml(getString(R.string.article_main));
        //Spannable reversedText = revertSpanned(spannedText);
        
        r.setText(spannedText);
    }

}
