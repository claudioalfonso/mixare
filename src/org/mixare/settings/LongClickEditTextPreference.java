package org.mixare.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.mixare.Config;
import org.mixare.R;

public class LongClickEditTextPreference extends EditTextPreference implements View.OnLongClickListener{
    public LongClickEditTextPreference(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LongClickEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LongClickEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LongClickEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent){
        ((ListView) parent).setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ListAdapter listAdapter = listView.getAdapter();
                Object listObj = listAdapter.getItem(position);
                if (listObj != null && listObj instanceof View.OnLongClickListener) {
                    View.OnLongClickListener longClickListener = (View.OnLongClickListener) listObj;
                    return longClickListener.onLongClick(view);
                }
                return false;
            }
        });
        return super.onCreateView(parent);
    }

    @Override
    public boolean onLongClick(View view) {
        setSummary(getText());
        return true;
    }
}
