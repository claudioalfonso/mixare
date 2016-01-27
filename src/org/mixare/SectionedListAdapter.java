package org.mixare;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
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

    private MixListView mixListView;
    private List<Item> items;

    public SectionedListAdapter(MixListView mixListView, Context context, int textViewResourceId,
								List<Item> objects) {
        super(context, textViewResourceId, objects);
        this.mixListView = mixListView;
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
        Log.d(Config.TAG, "getView: " + position);
        if (i != null) {
            if (i.isSection()) {
                SectionViewHolder sectionViewHolder;
                Object tag = null;
                try {
                    tag = convertView.getTag(R.string.list_view_section);
                } catch (Exception e) {
                }

//					Log.d(Config.TAG, "getView: " + position + " tag: " + tag + " section");
                if (tag == null) {
                    convertView = mixListView.getLayoutInflater().inflate(
                            R.layout.list_item_section, null);

                    sectionViewHolder = new SectionViewHolder();
                    sectionViewHolder.title = (TextView) convertView
                            .findViewById(R.id.section_title);
                    sectionViewHolder.markerCount = (TextView) convertView
                            .findViewById(R.id.section_marker_count);

                    convertView.setTag(R.string.list_view_section,
                            sectionViewHolder);
                } else {
                    sectionViewHolder = (SectionViewHolder) tag;
                }

                convertView.setOnClickListener(null);
                convertView.setOnLongClickListener(null);
                convertView.setLongClickable(false);

                sectionViewHolder.title.setText(((SectionItem) i)
                        .getTitle());
                sectionViewHolder.markerCount.setText(mixListView.getString(R.string.list_view_marker_in_section) + ((SectionItem) i)
                        .getMarkerCount());
            } else {
                ViewHolder holder;
                Object tag = null;
                try {
                    tag = convertView.getTag(R.string.list_view_entry);
                } catch (Exception e) {
                }
//					Log.d("test", "getView: " + position + " tag: " + tag + " entry");
                if (tag == null) {
                    convertView = mixListView.getLayoutInflater().inflate(
                            R.layout.marker_list, null);

                    holder = new ViewHolder();

                    holder.sideBar = convertView
                            .findViewById(R.id.side_bar);
                    holder.title = (TextView) convertView
                            .findViewById(R.id.marker_list_title);
                    holder.desc = (TextView) convertView
                            .findViewById(R.id.marker_list_summary);
                    holder.centerMap = (ImageButton) convertView
                            .findViewById(R.id.marker_list_mapbutton);

                    convertView.setTag(R.string.list_view_entry, holder);
                } else {
                    holder = (ViewHolder) tag;
                }

                MixListView.EntryItem item = (MixListView.EntryItem) i;

                MixListView.MarkerInfo markerInfo = item.getMarkerInfo();
                SpannableString spannableString = new SpannableString(
                        markerInfo.getTitle());

                if (markerInfo.getUrl() != null) {
                    spannableString.setSpan(new UnderlineSpan(), 0,
                            spannableString.length(), 0);
                    convertView.setOnClickListener(new OnClickListenerWebView(position));
                } else {
                    convertView.setOnClickListener(null);
                }

                holder.sideBar.setBackgroundColor(markerInfo.getColor());
                holder.centerMap.setTag(position);
                holder.centerMap
                        .setOnClickListener(onClickListenerCenterMap);
                holder.title.setText(spannableString);
                holder.desc.setText(Math.round(markerInfo.getDist()) + "m");
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
        ImageButton centerMap;
    }

    public int getCount() {
        return items.size();
    };

    /**
     * Handles the click event of the centerMap Button, to center the marker
     * on the Map.
     */
    View.OnClickListener onClickListenerCenterMap = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MixListView.MarkerInfo markerInfo = ((MixListView.EntryItem) getItem((Integer) v
                    .getTag())).getMarkerInfo();

            Intent startMap = new Intent(mixListView, MixMap.class);
            startMap.putExtra("center", true);
            startMap.putExtra("latitude", markerInfo.getLatitude());
            startMap.putExtra("longitude", markerInfo.getLongitude());
            mixListView.startActivityForResult(startMap, 76);
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
            MixListView.MarkerInfo markerInfo = ((MixListView.EntryItem) getItem(position))
                    .getMarkerInfo();

            String selectedURL = markerInfo.getUrl();
            if (selectedURL != null) {
                try {
                    if (selectedURL.startsWith("webpage")) {
                        String newUrl = MixUtils.parseAction(selectedURL);
						MixContext.getInstance().getActualMixViewActivity().getMarkerRenderer().getContext().getWebContentManager()
                                .loadWebPage(newUrl, MixContext.getInstance());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
