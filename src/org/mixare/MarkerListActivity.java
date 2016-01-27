/*
 * Copyright (C) 2010- Peer internet solutions
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
package org.mixare;

import java.util.ArrayList;
import java.util.List;

import org.mixare.data.DataHandler;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;
import org.mixare.map.MixMap;
import org.mixare.sectionedlist.Item;
import org.mixare.sectionedlist.SectionItem;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MarkerListActivity extends SherlockActivity {
    private static final int MENU_MAPVIEW_ID = 0;
    private static final int MENU_SEARCH_ID = 1;
    private SectionedListAdapter sectionedListAdapter;
    private ListView listView;
    private MarkerRenderer markerRenderer;
    private EditText editText;
    private MenuItem search;
    /* The sections for the list in meter */
    private static final int[] sections = { 250, 500, 1000, 1500, 3500, 5000,
            10000, 20000, 50000 };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.markerRenderer = MixViewActivity.getMarkerRendererStatically();

        editText = new EditText(this);

        List<Item> list;

		if (Intent.ACTION_SEARCH.equals(this.getIntent().getAction())) {
			// Get search query from IntentExtras
			String query = this.getIntent().getStringExtra(SearchManager.QUERY);
			list = createList(query);
			editText.setText(query);
		} else {

        // MarkerListFragment is started directly

            list = createList();

		}


        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.list);
        sectionedListAdapter = new SectionedListAdapter(this, this, 0, list);

    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Open mapView
		menu.add(MENU_MAPVIEW_ID, MENU_MAPVIEW_ID, MENU_MAPVIEW_ID, "MapView")
				.setIcon(android.R.drawable.ic_menu_mapmode)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		// The editText to use for search
		editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		editText.setHint(getString(R.string.list_view_search_hint));
		// Show the keyboard
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (hasFocus) {
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

				} else {
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
				}
			}
		});
		// Search at typing
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable edit) {
				// Recreate the list
				sectionedListAdapter.changeList(createList(edit.toString()));
			}
		});

		// Create a ActionBarItem which adds a editText to the ActionBar used
		// for search
		search = menu.add(MENU_SEARCH_ID, MENU_SEARCH_ID, MENU_SEARCH_ID,
				getString(R.string.list_view_search_hint));
		search.setIcon(android.R.drawable.ic_menu_search);
		search.setActionView(editText);
		search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// ActionBarIcon pressed
		//	finish();
			break;
		case MENU_MAPVIEW_ID:
			// Start MixMap to choose which Map to start
			Intent map = new Intent(this, MixMap.class);
			startActivity(map);
			break;
		case MENU_SEARCH_ID:
			// give focus to searchTextBox to open Keyboard
			editText.requestFocus();
		}
		return true;
	}


	@Override
	public boolean onSearchRequested() {
		// Open searchBox and request focus to open Keyboard
		search.expandActionView();
		editText.requestFocus();
		return false;
	}


    /**
     * Creates the list to display without filtering. Same as createList(null).
     *
     * @return The list containing Item's to display
     */
    private List<Item> createList() {
        return createList(null);
    }

    /**
     * Creates the list to display
     *
	 * @param query The query to look for or null not to filter
     * @return The list containing Item's to display
     */
    private List<Item> createList(String query) {
        List<Item> list = new ArrayList<>();
        DataHandler dataHandler = markerRenderer.getDataHandler();
        String lastSection = "";
        int lastSectionId = -1;
        int markerCount = 0;
        int sectionCount = 0;

        for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
            Marker marker = dataHandler.getMarker(i);

            // Check the query
            if (query != null) {
                if (marker.getTitle().toLowerCase()
                        .indexOf(query.toLowerCase().trim()) < 0) {
                    continue;
                }
            }

            // Create MarkerInfo and the section string
            MarkerInfo markerInfo = new MarkerInfo(marker.getTitle(),
                    marker.getURL(), marker.getDistance(),
                    marker.getLatitude(), marker.getLongitude(),
                    marker.getColor());
            String markerSection = getSection(marker.getDistance());

            // If the lastSection is not equal to this Section create a new
            // Section before creating the new Entry
            if (!markerSection.equals(lastSection)) {
                if (lastSectionId != -1) {
                    ((SectionItem) list.get(lastSectionId))
                            .setMarkerCount(markerCount);
                    markerCount = 0;
                }
                SectionItem section = new SectionItem(markerSection);
                list.add(section);
                lastSectionId = list.size() - 1;
                lastSection = markerSection;
                sectionCount++;
            }

            // Add the EntryItem to the list
            EntryItem entry = new EntryItem(markerInfo);
            list.add(entry);
            markerCount++;
        }

        if (lastSectionId != -1) {
            ((SectionItem) list.get(lastSectionId)).setMarkerCount(markerCount);
        }

        if (list.size() == 0) {
            SectionItem noResultFound = new SectionItem(
                    getString(R.string.list_view_search_no_result));
            list.add(noResultFound);
            sectionCount++;
        }

		if(getSupportActionBar()!=null) {
            getSupportActionBar().setSubtitle(
                    getString(R.string.list_view_total_markers)
                            + (list.size() - sectionCount));
        }

        return list;
    }

    /**
     * Create the string that represents a section
     *
     * Example: distance = 600 Method returns 500m - 1km (if these are the
     * nearest sections) distance = 200 Method returns < 250 (if 250 is the
     * smallest section) distance = 60000 Method returns > 50km (if 50000 is the
     * biggest section)
     *
	 * @param distance the distance from the marker to your location
     * @return A string that indicates how far you are away from a point
     *
     */
    private String getSection(double distance) {
        // TODO: Optimize
        String section = "";
        for (int i = 0; i < sections.length; i++) {
            if (distance <= sections[i]) {
                if (i == 0) {
                    section = "< " + MixUtils.formatDist(sections[i]);
                    break;
                } else if (distance > sections[i - 1]) {
                    section = MixUtils.formatDist(sections[i - 1]) + " - "
                            + MixUtils.formatDist(sections[i]);
                    break;
                }
            } else {
                section = "> " + MixUtils.formatDist(sections[i]);
            }
        }
        return section;
    }

	/* private classes */

    /**
     * Save some memory. We are only interested in these 6 things.
     *
     * @author KlemensE
     *
     */
    public class MarkerInfo {
        private String title;
        private String url;
        private Double dist;
        private Double latitude;
        private Double longitude;
        private int color;

        /**
         * Constructor
         *
         * @param title
         *            The title of the marker
         * @param url
         *            The URL where the marker points to
         * @param dist
         *            The distance to my position
         * @param latitude
         *            The latitude of the marker
         * @param longitude
         *            The longitude of the marker
         */
        public MarkerInfo(String title, String url, Double dist,
                          Double latitude, Double longitude, int color) {
            this.title = title;
            this.url = url;
            this.dist = dist;
            this.latitude = latitude;
            this.longitude = longitude;
            this.color = color;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public Double getDist() {
            return dist;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public int getColor() {
            return this.color;
        }
    }

    /**
     * This class is an implementation of the Item class which tells us whether
     * to draw a Section or an Entry
     *
     * @author KlemensE
     */
    public class EntryItem implements Item {
        MarkerInfo markerInfo;

        private EntryItem(MarkerInfo info) {
            this.markerInfo = info;
        }

        public MarkerInfo getMarkerInfo() {
            return markerInfo;
        }

        @Override
        public boolean isSection() {
            return false;
        }
    }
}