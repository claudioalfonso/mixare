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

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;

import org.mixare.data.DataHandler;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;
import org.mixare.sectionedlist.Item;
import org.mixare.sectionedlist.SectionItem;

import java.util.ArrayList;
import java.util.List;

public class MarkerListFragment extends DialogFragment {
	private SectionedListAdapter sectionedListAdapter;
	private ListView listView;
	private MarkerRenderer markerRenderer;

    private String searchString="";

	/* The sections for the list in meter */
	private static final int[] sections = { 250, 500, 1000, 1500, 3500, 5000,
			10000, 20000, 50000 };

	public MarkerListFragment(){
		this.markerRenderer = MixViewActivity.getMarkerRendererStatically();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		List<Item> list;

		list = createList();

		sectionedListAdapter = new SectionedListAdapter(this.getActivity(), this.getActivity(), 0, list);

	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View myView=inflater.inflate(R.layout.list, container, false);
        listView = (ListView) myView.findViewById(R.id.section_list_view);
        listView.setAdapter(sectionedListAdapter);
        return myView;
    }

    public void updateList(String searchString){
        this.searchString=searchString;
        if(getActivity()!=null) {
            sectionedListAdapter.changeList(createList(searchString));
        }
    }

    @Override
    public void onAttach(Context context){
        updateList(this.searchString);
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
	protected List<Item> createList(String query) {
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
				if (!marker.getTitle().toLowerCase().contains(query.toLowerCase().trim())) {
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
		 *            The menuItemTitles of the marker
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

		EntryItem(MarkerInfo info) {
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