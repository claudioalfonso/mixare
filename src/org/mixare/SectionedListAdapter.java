package org.mixare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.mixare.lib.MixUtils;
import org.mixare.map.MixMap;
import org.mixare.sectionedlist.Item;
import org.mixare.sectionedlist.SectionItem;

import java.util.List;

/**
 * This class extends the ArrayAdapter to be able to create our own View and
 * OnClickListeners
 *
 * @author KlemensE
 */
class SectionedListAdapter extends ArrayAdapter<Item> {

    private Activity parentActivity;
    private List<Item> items;

    public SectionedListAdapter(Activity parentActivity, Context context, int textViewResourceId, List<Item> objects) {
        super(context, textViewResourceId, objects);
        this.parentActivity = parentActivity;
        this.items = objects;
    }

    public void changeList(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public Item getItem(int position) {
        if (position > items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item i = getItem(position);
//        Log.d(Config.TAG, "getView: " + position);
        if (i != null) {
            if (i.isSection()) {
                SectionViewHolder sectionViewHolder;
                Object tag = null;
                try {
                    tag = convertView.getTag(R.string.list_view_section);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

//					Log.d(Config.TAG, "getView: " + position + " tag: " + tag + " section");
                if (tag == null) {
                    convertView = parentActivity.getLayoutInflater().inflate(R.layout.list_item_section, parent, false);

                    sectionViewHolder = new SectionViewHolder();
                    sectionViewHolder.title = (TextView) convertView.findViewById(R.id.section_title);
                    sectionViewHolder.markerCount = (TextView) convertView.findViewById(R.id.section_marker_count);

                    convertView.setTag(R.string.list_view_section, sectionViewHolder);
                } else {
                    sectionViewHolder = (SectionViewHolder) tag;
                }

                convertView.setOnClickListener(null);
                convertView.setOnLongClickListener(null);
                convertView.setLongClickable(false);

                sectionViewHolder.title.setText(((SectionItem) i).getTitle());
                sectionViewHolder.markerCount.setText(
                        parentActivity.getString(R.string.list_view_marker_in_section,((SectionItem) i).getMarkerCount() )
                );
            } else {
                ViewHolder holder;
                Object tag = null;
                try {
                    tag = convertView.getTag(R.string.list_view_entry);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
//					Log.d("test", "getView: " + position + " tag: " + tag + " entry");
                if (tag == null) {
                    convertView = parentActivity.getLayoutInflater().inflate(R.layout.marker_list, parent, false);

                    holder = new ViewHolder();

                    holder.sideBar = convertView.findViewById(R.id.side_bar);
                    holder.title = (TextView) convertView.findViewById(R.id.marker_list_title);
                    holder.desc = (TextView) convertView.findViewById(R.id.marker_list_summary);
                    holder.mapButton = (ImageButton) convertView.findViewById(R.id.marker_list_mapbutton);
                    holder.directionsButton = (ImageButton) convertView.findViewById(R.id.marker_list_destinationbutton);


                    convertView.setTag(R.string.list_view_entry, holder);
                } else {
                    holder = (ViewHolder) tag;
                }

                MarkerListFragment.EntryItem item = (MarkerListFragment.EntryItem) i;

                MarkerListFragment.MarkerInfo markerInfo = item.getMarkerInfo();
                SpannableString spannableString = new SpannableString(markerInfo.getTitle());

                if (markerInfo.getUrl() != null) {
                    spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
                    convertView.setOnClickListener(new OnClickListenerWebView(position));
                } else {
                    convertView.setOnClickListener(null);
                }

                holder.sideBar.setBackgroundColor(markerInfo.getColor());
                holder.mapButton.setTag(position);
                holder.mapButton.setOnClickListener(onClickListenerCenterMap);
                holder.directionsButton.setTag(position);
                holder.directionsButton.setOnClickListener(onClickListenerDirections);
                holder.title.setText(spannableString);
                holder.desc.setText(getContext().getString(R.string.distance_format, markerInfo.getDist()));
            }
        }

        return convertView;
    }

    private class SectionViewHolder {
        TextView title;
        TextView markerCount;
    }

    private class ViewHolder {
        View sideBar;
        TextView title;
        TextView desc;
        ImageButton mapButton;
        ImageButton directionsButton;

    }

    public int getCount() {
        return items.size();
    }

    /**
     * Handles the click event of the mapButton, to center the marker
     * on the Map.
     */
    View.OnClickListener onClickListenerCenterMap = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MarkerListFragment.MarkerInfo markerInfo = ((MarkerListFragment.EntryItem) getItem((Integer) v.getTag())).getMarkerInfo();

            Intent startMap = new Intent(parentActivity, MixMap.class);
            startMap.putExtra("center", true);
            startMap.putExtra("latitude", markerInfo.getLatitude());
            startMap.putExtra("longitude", markerInfo.getLongitude());
            parentActivity.startActivityForResult(startMap, 76);
        }
    };

    /**
     * Handles the click event of the directionsButton, to center the marker
     * on the Map.
     */
    View.OnClickListener onClickListenerDirections = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MarkerListFragment.MarkerInfo markerInfo = ((MarkerListFragment.EntryItem) getItem((Integer) v.getTag())).getMarkerInfo();
            Location destination=Config.getManualFix();
            destination.setLatitude(markerInfo.getLatitude());
            destination.setLongitude(markerInfo.getLongitude());

            MixViewDataHolder.getInstance().setCurDestination(destination);
            Intent startMap = new Intent(parentActivity, MixMap.class);
            startMap.putExtra("center", true);
            startMap.putExtra("latitude", markerInfo.getLatitude());
            startMap.putExtra("longitude", markerInfo.getLongitude());
            parentActivity.startActivityForResult(startMap, 76);
        }
    };

    /**
     * Handles the click on the list row to open the WebView
     */
    private class OnClickListenerWebView implements View.OnClickListener {
        private int position;
        public OnClickListenerWebView (int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            MarkerListFragment.MarkerInfo markerInfo = ((MarkerListFragment.EntryItem) getItem(position)).getMarkerInfo();

            String selectedURL = markerInfo.getUrl();
            if (selectedURL != null) {
                try {
                    if (selectedURL.startsWith("webpage")) {
                        String newUrl = MixUtils.parseAction(selectedURL);
						MixContext.getInstance().getWebContentManager().loadWebPage(newUrl, MixContext.getInstance());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
