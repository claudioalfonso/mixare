package org.mixare.route;

import android.location.Location;
import android.os.AsyncTask;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelanieW on 03.02.2016.
 */
public class RouteDataAsyncTask extends AsyncTask<Location,Void,MyRoute> {

    List<LatLong> coordinateList = new ArrayList<>();
    List<LatLong> latLong = new ArrayList<>();
    MyRoute myRoute;

        public AsyncResponse delegate = null;

        public RouteDataAsyncTask(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected MyRoute doInBackground(Location... params) {
            RouteRequestor rs = new RouteRequestor();
            myRoute = rs.init(params[0],params[1]);
           // latLong = rs.init(params[0],params[1]);
           // return latLong;
            return myRoute;
        }

        @Override
        protected void onPostExecute(MyRoute myRoute) {
            super.onPostExecute(myRoute);
            delegate.processFinish(myRoute);
        }
}
