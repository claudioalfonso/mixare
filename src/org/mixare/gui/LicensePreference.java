package org.mixare.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import org.mixare.R;

public class LicensePreference extends DialogPreference {
    Context context;

    public LicensePreference(Context context) {
        this(context, null);
    }

    public LicensePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    public class LicenseDialog extends AlertDialog {
        protected LicenseDialog() {
            super(LicensePreference.this.context);
            this.setMessage(context.getString(R.string.license_text));
            this.setTitle(context.getString(R.string.pref_item_license_dialogtitle));
            this.setButton(BUTTON_NEGATIVE, context.getString(R.string.close_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    public LicenseDialog getDialog(){
        return new LicenseDialog();
    }
}