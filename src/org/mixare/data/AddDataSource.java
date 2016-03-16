/*
 * Copyright (C) 2012- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.data;

import org.mixare.R;

import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to add new DataSources
 * @author KlemensE
 */
public class AddDataSource extends Activity {
	
	private static final int MENU_SAVE_ID = Menu.FIRST;
	
	EditText nameField;
	EditText urlField;
	EditText colorField;
	Spinner typeSpinner;
	Spinner displaySpinner;
	Spinner blurSpinner;
	
	Bundle extras;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datasource_details);
		
		nameField = (EditText) findViewById(R.id.ds_detail_name_input);
		urlField = (EditText) findViewById(R.id.ds_detail_url_input);
		colorField = (EditText) findViewById(R.id.ds_detail_color_input);
		typeSpinner = (Spinner) findViewById(R.id.ds_detail_dstype_input);
		displaySpinner = (Spinner) findViewById(R.id.ds_detail_displaytype_input);
		blurSpinner = (Spinner) findViewById(R.id.ds_detail_blurgps_input);
		
		extras = getIntent().getExtras();
		if (extras != null) {
			// Get DataSource
			if (extras.containsKey("DataSourceId")) {
				DataSource ds = DataSourceStorage.getInstance().getDataSource(
						extras.getInt("DataSourceId"));
				nameField.setText(ds.getName(), TextView.BufferType.EDITABLE);
				urlField.setText(ds.getUrl(), TextView.BufferType.EDITABLE);
				typeSpinner.setSelection(ds.getTypeId());
				displaySpinner.setSelection(ds.getDisplayId());
				blurSpinner.setSelection(ds.getBlurId());
				colorField.setText(ds.getColorString(), TextView.BufferType.EDITABLE);
			}
			
			// Check whether DataSource can be edited or not
			if (extras.containsKey("isEditable")) {
				boolean activated = extras.getBoolean("isEditable");
//				nameField.setActivated(activated);
				nameField.setFocusable(activated);
//				urlField.setActivated(activated);
				urlField.setFocusable(activated);
//				typeSpinner.setActivated(activated);
				typeSpinner.setClickable(activated);
//				displaySpinner.setActivated(activated);
				displaySpinner.setClickable(activated);
			}
		}
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * Creates a new DataSource and Saves it to the SharedPreferences
	 */
	private boolean saveNewDataSource() {
		String name = nameField.getText().toString();
		String url = urlField.getText().toString();
		String colorString = colorField.getText().toString();
		int typeId = (int) typeSpinner.getItemIdAtPosition(typeSpinner
				.getSelectedItemPosition());
		int displayId = (int) displaySpinner.getItemIdAtPosition(displaySpinner
				.getSelectedItemPosition());
		int blurId = (int) blurSpinner.getItemIdAtPosition(blurSpinner
				.getSelectedItemPosition());
		
		if (!name.isEmpty() && !url.isEmpty()) {
			if (extras != null) {
				if (extras.containsKey("DataSourceId")) {
					// DataSource already exists
					DataSource ds = DataSourceStorage.getInstance().getDataSource(
							extras.getInt("DataSourceId"));
					ds.setName(name);
					ds.setUrl(url);
					ds.setType(typeId);
					ds.setDisplay(displayId);
					ds.setBlur(blurId);
					ds.setColor(Color.parseColor(colorString));
					
					DataSourceStorage.getInstance(getApplicationContext()).save();
					return true;
				}
			}
			// New DataSource
			DataSource ds = new DataSource(name, url,
					DataSource.TYPE.values()[typeId],
					DataSource.DISPLAY.values()[displayId], true);
			ds.setBlur(DataSource.BLUR.values()[blurId]);
			ds.setColor(Color.parseColor(colorString));
			DataSourceStorage.getInstance().add(ds);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case MENU_SAVE_ID: 
			if (saveNewDataSource()) {
				finish();
			} else {
				Toast.makeText(this, "Error saving DataSource", Toast.LENGTH_LONG).show();
			}
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SAVE_ID, Menu.NONE, "Save").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	/**
	 * Creates a new Dialog to describe the different Types of DataSources
	 * @param v
	 */
	public void onDataSourceInfoClick(View v) {
		Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage(R.string.ds_detail_dstype_infobutton_message	);
		builder.setNegativeButton(getString(R.string.close_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert1 = builder.create();
		alert1.setTitle(getString(R.string.ds_detail_dstype_infobutton_title));
		alert1.show();
	}
}