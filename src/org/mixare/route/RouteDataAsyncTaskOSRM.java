package org.mixare.route;

import android.location.Location;
import android.os.AsyncTask;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

public class RouteDataAsyncTaskOSRM extends AsyncTask<Location,Void,MyRoute> {
    MyRoute myRoute;

        public AsyncResponse delegate = null;

        public RouteDataAsyncTaskOSRM(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected MyRoute doInBackground(Location... params) {
            RouteRequestorOSRM rs = new RouteRequestorOSRM();
            myRoute = rs.init(params[0],params[1]);
            return myRoute;
        }

        @Override
        protected void onPostExecute(MyRoute myRoute) {
            super.onPostExecute(myRoute);
            delegate.processFinish(myRoute);
        }
}