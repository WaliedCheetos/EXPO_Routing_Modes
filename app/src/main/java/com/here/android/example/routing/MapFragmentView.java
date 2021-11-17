/*
 * Copyright (c) 2011-2021 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.here.android.example.routing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.common.TransitType;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapTransitLayer;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.DrivingDirection;
import com.here.android.mpa.routing.DynamicPenalty;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteElement;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.routing.RoutingZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;



/**
 * This class encapsulates the properties and functionality of the Map view.A route calculation from
 * south of Berlin to the north of Berlin.
 */
public class MapFragmentView {
    private static final int ITEM_ID_SHOW_ZONES = 1;
    private static final int ITEM_ID_EXCLUDE_IN_ROUTING = 2;
    private static final int ITEM_ID_ADD_AVOIDED_AREAS = 3;
    private AndroidXMapFragment m_mapFragment;
    private Button m_createPTRouteButton;
    private Button m_requestPTRouteButton;

    private AppCompatActivity m_activity;
    private Map m_map;
    private MapRoute m_mapRoute;
    List<MapObject> mapObjectList = new ArrayList<MapObject>();

    List<MapObject> mapPolylineList = new ArrayList<MapObject>();
//    List<MapPolyline> mapPolylineList = new ArrayList<MapPolyline>();
    List<MapObject> mapLabeledMarkerList = new ArrayList<MapObject>();
//    List<MapLabeledMarker> mapLabeledMarkerList = new ArrayList<MapLabeledMarker>();
    List<MapMarker> mapMarkerList = new ArrayList<MapMarker>();

    private boolean m_isExcludeRoutingZones;
    private boolean m_addAvoidedAreas;

    private RequestQueue m_requestQueue;


    public FlexiblePolylineEncoderDecoder m_flexiblePolylineEncoderDecoder = new FlexiblePolylineEncoderDecoder();

    public MapFragmentView(AppCompatActivity activity) {
        m_activity = activity;
        initMapFragment();
        /*
         * We use a button in this example to control the route calculation
         */
        initCreateRouteButton();

        m_requestQueue = Volley.newRequestQueue(this.m_activity);
    }

    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) m_activity.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    private void initMapFragment() {
        /* Locate the mapFragment UI element */
        m_mapFragment = getMapFragment();

        if (m_mapFragment != null) {
            /* Initialize the AndroidXMapFragment, results will be given via the called back. */
            m_mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {

                    if (error == Error.NONE) {
                        /* get the map object */
                        m_map = m_mapFragment.getMap();

                        // sets the map scheme to include transit
                        m_map.setMapScheme(Map.Scheme.NORMAL_DAY_TRANSIT);
                        m_map.getMapTransitLayer().setMode(MapTransitLayer.Mode.EVERYTHING);

                        /*
                         * Set the map center to the south of Berlin.
                         */
//                        m_map.setCenter(new GeoCoordinate(52.406425, 13.193975, 0.0),
//                                Map.Animation.NONE);

                        m_map.setCenter(new GeoCoordinate(24.9604832,55.1508421, 0.0),
                                Map.Animation.NONE);

                        /* Set the zoom level to the average between min and max zoom level. */
                        m_map.setZoomLevel((m_map.getMaxZoomLevel() + m_map.getMinZoomLevel()) / 2);


                        m_mapFragment.getMapGesture()
                                .addOnGestureListener(new MapGesture.OnGestureListener.OnGestureListenerAdapter() {
//                                    @Override
//                                    public void onPanStart() {
////                                        super.onPanStart();
//                                    }
//
//                                    @Override
//                                    public void onPanEnd() {
////                                        super.onPanEnd();
//                                    }
//
//                                    @Override
//                                    public void onMultiFingerManipulationStart() {
////                                        super.onMultiFingerManipulationStart();
//                                    }
//
//                                    @Override
//                                    public void onMultiFingerManipulationEnd() {
////                                        super.onMultiFingerManipulationEnd();
//                                    }

                                    @Override
                                    public boolean onMapObjectsSelected(@NonNull List<ViewObject> viewObjectList) {
//                                        return super.onMapObjectsSelected(list);
                                        for (ViewObject viewObject : viewObjectList) {
                                            if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                                                if (((MapObject)viewObject).getType() == MapObject.Type.LABELED_MARKER) {
                                                    // At this point we have the originally added
                                                    // map marker, so we can do something with it
                                                    // (like change the visibility, or more
                                                    // marker-specific actions)
                                                    String time = "";
                                                    String name = "";
                                                    try {
                                                        name = ((JSONObject)(((MapObject)viewObject).getTag())).getJSONObject("departure").getJSONObject("place").getString("name");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    try {
                                                        time = ((JSONObject)(((MapObject)viewObject).getTag())).getJSONObject("departure").getString("time");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    new AlertDialog.Builder(m_activity).setMessage(
                                                            "Next departure at: " + time)
                                                            .setTitle(name)
                                                            .setNegativeButton(android.R.string.cancel,
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(
                                                                                DialogInterface dialog,
                                                                                int which) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).create().show();

                                                }
                                            }
                                        }
                                        // return false to allow the map to handle this callback also
                                        return false;
                                    }

//                                    @Override
//                                    public boolean onTapEvent(@NonNull PointF pointF) {
////                                        return super.onTapEvent(pointF);
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onDoubleTapEvent(@NonNull PointF pointF) {
//                                        return false;
////                                        return super.onDoubleTapEvent(pointF);
//                                    }
//
//                                    @Override
//                                    public void onPinchLocked() {
//                                        super.onPinchLocked();
//                                    }
//
//                                    @Override
//                                    public boolean onPinchZoomEvent(float v, @NonNull PointF pointF) {
//                                        return false;
////                                        return super.onPinchZoomEvent(v, pointF);
//                                    }
//
//                                    @Override
//                                    public void onRotateLocked() {
//                                        super.onRotateLocked();
//                                    }
//
//                                    @Override
//                                    public boolean onRotateEvent(float v) {
//                                        return super.onRotateEvent(v);
//                                    }
//
//                                    @Override
//                                    public boolean onTiltEvent(float v) {
//                                        return super.onTiltEvent(v);
//                                    }
//
//                                    @Override
//                                    public boolean onLongPressEvent(@NonNull PointF pointF) {
//                                        return super.onLongPressEvent(pointF);
//                                    }
//
//                                    @Override
//                                    public boolean onTwoFingerTapEvent(@NonNull PointF pointF) {
//                                        return super.onTwoFingerTapEvent(pointF);
//                                    }
//
//                                    @Override
//                                    public void onLongPressRelease() {
//                                        super.onLongPressRelease();
//                                    }
                                }, 0, false);

                    } else {
                        new AlertDialog.Builder(m_activity).setMessage(
                                "Error : " + error.name() + "\n\n" + error.getDetails())
                                .setTitle(R.string.engine_init_error)
                                .setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                m_activity.finish();
                                            }
                                        }).create().show();
                    }
                }
            });
        }
    }

    private void initCreateRouteButton() {
        m_createPTRouteButton = (Button) m_activity.findViewById(R.id.btn_createPTRouteButton);
        m_requestPTRouteButton = (Button) m_activity.findViewById(R.id.btn_requestPTRouteButton);

        m_createPTRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                m_map.removeMapObject(m_mapRoute);
//                m_mapRoute = null;
                m_map.removeAllMapObjects();

                mapObjectList.clear();
                mapLabeledMarkerList.clear();
                mapPolylineList.clear();
                mapMarkerList.clear();

//                createRoute(Collections.<RoutingZone>emptyList());
                createTransitRoute();
            }
        });
        m_requestPTRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                m_map.removeMapObject(m_mapRoute);
//                m_mapRoute = null;
                m_map.removeAllMapObjects();
                mapObjectList.clear();

//                createRoute(Collections.<RoutingZone>emptyList());
                requestPublicTransitRoute();
            }
        });

    }

    public void requestPublicTransitRoute(){
      //https://transit.router.hereapi.com/v8/routes?origin=41.79457,12.25473&destination=41.90096,12.50243&return=incidents,bookingLinks,travelSummary,actions,intermediate,fares,polyline&apikey=***
        //https://transit.router.hereapi.com/v8/routes?c&return=incidents,bookingLinks,travelSummary,actions,intermediate,fares,polyline&apikey=***

        String url = "https://transit.router.hereapi.com/v8/routes?origin=25.0636444,55.2392576&destination=24.9604832,55.1508421&return=incidents,bookingLinks,travelSummary,actions,intermediate,fares,polyline&apikey=***";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray routes = response.getJSONArray("routes");
                            //I will ONLY mind the very first route result
                            JSONObject route = routes.getJSONObject(0);

                            JSONArray sections = route.getJSONArray("sections");


                            for (int i = 0; i < sections.length(); i++) {
                                JSONObject section = sections.getJSONObject(i);
                                JSONObject transport  = section.getJSONObject("transport");

                                String flexiblePolyline = section.getString("polyline");
                                List<FlexiblePolylineEncoderDecoder.LatLngZ> flexibleCoordinates = m_flexiblePolylineEncoderDecoder.decode(flexiblePolyline);
                                List<GeoCoordinate> coordinates = new ArrayList<GeoCoordinate>();
                                for (int ii = 0; ii < flexibleCoordinates.size(); ii++) {
                                    GeoCoordinate coordinate = new GeoCoordinate(flexibleCoordinates.get(ii).lat, flexibleCoordinates.get(ii).lng);
                                    coordinates.add(coordinate);
                                }

                                MapPolyline mapPolyline = new MapPolyline(new GeoPolyline(coordinates));

                                if (transport.getString("mode").toUpperCase().equals("SUBWAY")){
                                    //RED
                                    mapPolyline.setLineColor(-65536);

                                    JSONArray intermediateStops = section.getJSONArray("intermediateStops");
                                    for (int iii = 0; iii < intermediateStops.length(); iii++) {
                                        String intermediateStop_Lat = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lat");
                                        String intermediateStop_Lng = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lng");
                                        String intermediateStop_Name = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getString("name");


//                                        MapMarker mapMarker = new MapMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)), new Image());
//                                        mapObjectList.add(mapMarker);

                                        MapLabeledMarker mapLabeledMarker = new MapLabeledMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                                        mapLabeledMarker.setCoordinate(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                                        mapLabeledMarker.setIcon(IconCategory.METRO_STATION);
                                        mapLabeledMarker.setTag(intermediateStops.getJSONObject(iii));
//                                        mapLabeledMarker.setLabelText(intermediateStop_Name, intermediateStop_Name);
                                        mapLabeledMarkerList.add(mapLabeledMarker);
                                    }

                                }
                                else if (transport.getString("mode").toUpperCase().equals("BUS")){
                                    //YELLOW
                                    mapPolyline.setLineColor(-256);

                                    JSONArray intermediateStops = section.getJSONArray("intermediateStops");
                                    for (int iii = 0; iii < intermediateStops.length(); iii++) {
                                        String intermediateStop_Lat = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lat");
                                        String intermediateStop_Lng = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getJSONObject("location").getString("lng");
                                        String intermediateStop_Name = intermediateStops.getJSONObject(iii).getJSONObject("departure").getJSONObject("place").getString("name");


//                                        MapMarker mapMarker = new MapMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)), new Image());
//                                        mapObjectList.add(mapMarker);

                                        MapLabeledMarker mapLabeledMarker = new MapLabeledMarker(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                                        mapLabeledMarker.setCoordinate(new GeoCoordinate(Double.valueOf(intermediateStop_Lat), Double.valueOf(intermediateStop_Lng)));
                                        mapLabeledMarker.setIcon(IconCategory.BUS_STATION);
                                        mapLabeledMarker.setTag(intermediateStops.getJSONObject(iii));
//                                        mapLabeledMarker.setLabelText(intermediateStop_Name, intermediateStop_Name);
                                        
                                        mapLabeledMarkerList.add(mapLabeledMarker);
                                    }
                                }
                                else if (transport.getString("mode").toUpperCase().equals("PEDESTRIAN")){
                                    //BLUE
                                    mapPolyline.setLineColor(-16776961);}

                                mapPolyline.setLineWidth(13);
//                                mapObjectList.add(mapPolyline);
                                mapPolylineList.add(mapPolyline);
                            }

//                            m_map.addMapObjects(mapObjectList);
                            m_map.addMapObjects(mapPolylineList);
                            m_map.addMapObjects(mapLabeledMarkerList);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //textView.setText("Response: " + response.toString());
                        Log.d("WaliedCheetos: ", response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        error.printStackTrace();
                    }
                });

        m_requestQueue.add(jsonObjectRequest);
    }

    private void createTransitRoute() {
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* set active data connection to query online timetable data that is available from municipalities */
        coreRouter.setConnectivity(CoreRouter.Connectivity.ONLINE);

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption. HERE Mobile SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routeOptions.setTransportMode(RouteOptions.TransportMode.PUBLIC_TRANSPORT);
        routeOptions.setPublicTransportTypeAllowed(TransitType.BUS_PUBLIC, true);
//        routeOptions.setPublicTransportTypeAllowed(TransitType.ORDERED_SERVICES_OR_TAXI, true);

        /* Calculate 1 route. */
        routeOptions.setRouteCount(1);

        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        /* START: South of Berlin */
//        //RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(52.406425, 13.193975));
//        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(25.0636444,55.2392576));
//        /* END: North of Berlin */
////        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(52.638623, 13.441998));
//        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(24.9604832,55.1508421));


//        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(49.1966286, -123.0053635)));
//        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(49.1947289, -123.1762924)));
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(25.0636444,55.2392576)));
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(24.9604832,55.1508421)));

        /* Add both waypoints to the route plan */
//        routePlan.addWaypoint(startPoint);
//        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the listener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>() {
                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults, RoutingError routingError) {
                        /* Calculation is done. Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            Route route = routeResults.get(0).getRoute();



//                            MapPolyline mapPolyline = new MapPolyline(new GeoPolyline(route.getRouteGeometry()));
//                            mapPolyline.setLineWidth(20);


                            for (RouteElement routeElement : route.getRouteElements().getElements()) {

                                Log.d("WaliedCheetos", "================ " + routeElement.getType().name() + " ================");


                                if (routeElement.getType() == RouteElement.Type.TRANSIT){
                                    MapPolyline mapPolyline = new MapPolyline(new GeoPolyline(routeElement.getTransitElement().getGeometry()));
                                    mapPolyline.setLineWidth(13);

                                    if ( routeElement.getTransitElement().getTransitType() == TransitType.BUS_PUBLIC)
                                        //RED
                                        mapPolyline.setLineColor(-65536);
                                    else if (routeElement.getTransitElement().getTransitType() == TransitType.RAIL_METRO)
                                        //YELLOW
                                        mapPolyline.setLineColor(-256);

                                    mapObjectList.add(mapPolyline);

                                    Log.d("WaliedCheetos",routeElement.getTransitElement().getTransitType().name());
                                    Log.d("WaliedCheetos",routeElement.getTransitElement().getLineName());
                                }
                                else if (routeElement.getType() == RouteElement.Type.ROAD){

                                    MapPolyline mapPolyline = new MapPolyline(new GeoPolyline(routeElement.getRoadElement().getGeometry()));
                                    mapPolyline.setLineWidth(13);

//                                    //GREEN
//                                    mapPolyline.setLineColor(-16711936);
                                    //BLUE
                                    mapPolyline.setLineColor(-16776961);

                                    mapObjectList.add(mapPolyline);

                                    Log.d("WaliedCheetos",routeElement.getRoadElement().getRouteName());
                                    Log.d("WaliedCheetos",routeElement.getRoadElement().getRoadName());
                                }
                                Log.d("WaliedCheetos","=====================================");
                            }

                            //Log.d("WaliedCheetos",route.getRouteElements().getElements().get(0).getType().name());

                            //display the source attribution retrieved
//                            Log.d("WaliedCheetos", route.getTransitRouteSourceAttribution().toString());

//                            for (RouteElement routeElement: route.getRouteElements().getElements()) {
//                                Log.d("WaliedCheetos", routeElement.getTransitElement().getTransitType().name());
//                            }

                                /* Create a MapRoute so that it can be placed on the map */
                                m_mapRoute = new MapRoute(route);

                                /* Show the maneuver number on top of the route */
                                m_mapRoute.setManeuverNumberVisible(true);


//                            Toast.makeText(m_activity,
//                                    route.getTransitRouteSourceAttribution().getAttribution(),
//                                    Toast.LENGTH_LONG).show();


                                /* Add the MapRoute to the map */
//                                m_map.addMapObject(m_mapRoute);
//                            m_map.addMapObject(mapPolyline);
                            m_map.addMapObjects(mapObjectList);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                m_map.zoomTo(route.getBoundingBox(), Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                        } else {
                            Toast.makeText(m_activity,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void createRoute(final List<RoutingZone> excludedRoutingZones) {
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption. HERE Mobile SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        /* Disable highway in this route. */
        routeOptions.setHighwaysAllowed(false);
        /* Calculate the shortest route available. */
        routeOptions.setRouteType(RouteOptions.Type.SHORTEST);
        /* Calculate 1 route. */
        routeOptions.setRouteCount(1);
        /* Exclude routing zones. */
        if (!excludedRoutingZones.isEmpty()) {
            routeOptions.excludeRoutingZones(toStringIds(excludedRoutingZones));
        }

        if (m_addAvoidedAreas) {
            DynamicPenalty dynamicPenalty = new DynamicPenalty();
            // There are two option to avoid certain areas during routing
            // 1. Add banned area using addBannedArea API
            GeoPolygon geoPolygon = new GeoPolygon();
            geoPolygon.add(Arrays.asList(new GeoCoordinate(52.631692, 13.437591),
                    new GeoCoordinate(52.631905, 13.437787),
                    new GeoCoordinate(52.632577, 13.438357)));
            // Note, the maximum supported number of banned areas is 20.
            dynamicPenalty.addBannedArea(geoPolygon);

            // 1. Add banned road link using addRoadPenalty API
            // Note, map data needs to be present to get RoadElement by the GeoCoordinate.
            RoadElement roadElement = RoadElement
                    .getRoadElement(new GeoCoordinate(52.406611, 13.194916), "MAC");
            if (roadElement != null) {
                dynamicPenalty.addRoadPenalty(roadElement, DrivingDirection.DIR_BOTH,
                        0/*new speed*/);
            }
            coreRouter.setDynamicPenalty(dynamicPenalty);
        }
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        /* START: South of Berlin */
        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(52.406425, 13.193975));
        /* END: North of Berlin */
        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(52.638623, 13.441998));

        /* Add both waypoints to the route plan */
        routePlan.addWaypoint(startPoint);
        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the listener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>() {
                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults,
                            RoutingError routingError) {
                        /* Calculation is done. Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            Route route = routeResults.get(0).getRoute();

                            if (m_isExcludeRoutingZones && excludedRoutingZones.isEmpty()) {
                                // Here we exclude all available routing zones in the route.
                                // Also RoutingZoneRestrictionsChecker can be used to get
                                // available routing zones for specific RoadElement.
                                createRoute(route.getRoutingZones());
                            } else {
                                /* Create a MapRoute so that it can be placed on the map */
                                m_mapRoute = new MapRoute(route);

                                /* Show the maneuver number on top of the route */
                                m_mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                m_map.addMapObject(m_mapRoute);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                m_map.zoomTo(route.getBoundingBox(), Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.getItemId() == ITEM_ID_SHOW_ZONES) {
            EnumSet<Map.FleetFeature> features;
            if (item.isChecked()) {
                features = EnumSet.of(Map.FleetFeature.ENVIRONMENTAL_ZONES);
            } else {
                features = EnumSet.noneOf(Map.FleetFeature.class);

            }
            m_map.setFleetFeaturesVisible(features);
        } else if (item.getItemId() == ITEM_ID_EXCLUDE_IN_ROUTING) {
            m_isExcludeRoutingZones = item.isChecked();
            if (m_mapRoute != null) {
                Toast.makeText(m_activity, "Please recalculate the route to apply this setting",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == ITEM_ID_ADD_AVOIDED_AREAS) {
            m_addAvoidedAreas = item.isChecked();
            if (m_mapRoute != null) {
                Toast.makeText(m_activity, "Please recalculate the route to apply this setting",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ITEM_ID_SHOW_ZONES, Menu.NONE, "Show environmental zones")
                .setCheckable(true);
        menu.add(0, ITEM_ID_EXCLUDE_IN_ROUTING, Menu.NONE, "Exclude all zones in routing")
                .setCheckable(true);

        menu.add(0, ITEM_ID_ADD_AVOIDED_AREAS, Menu.NONE, "Add avoided areas")
                .setCheckable(true);

        return true;
    }

    static List<String> toStringIds(List<RoutingZone> excludedRoutingZones) {
        ArrayList<String> ids = new ArrayList<>();
        for (RoutingZone zone : excludedRoutingZones) {
            ids.add(zone.getId());
        }
        return ids;
    }
}
