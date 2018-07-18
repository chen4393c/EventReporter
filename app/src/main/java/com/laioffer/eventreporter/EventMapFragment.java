package com.laioffer.eventreporter;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "EventMapFragment";

    private MapView mMapView;
    private View mView;
    private DatabaseReference database;
    private List<Event> events;

    public EventMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_event_map, container, false);
        database = FirebaseDatabase.getInstance().getReference();
        events = new ArrayList<>();
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.event_map_view);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume(); // needed to get the map display immediately
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        final  LocationTracker locationTracker = new LocationTracker(getActivity());
        locationTracker.getLocation();
        double curLatitude = locationTracker.getLatitude();
        double curLongitude = locationTracker.getLongitude();

        // Set up camera configuration, set camera to latitude, longitude and zoom
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(curLatitude, curLongitude)).zoom(12).build();

        // Animate the zoom process
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        setUpMarkersCloseToCurLocation(googleMap, curLatitude, curLongitude);
    }

    // Go through data from database, and find out events that are less or equal to
    // ten miles away from current location
    private void setUpMarkersCloseToCurLocation(final GoogleMap googleMap,
                                                final double curLatitude,
                                                final double curLongitude) {
        events.clear();
        database.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get all available events
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Event event = noteDataSnapshot.getValue(Event.class);
                    double destLatitude = event.getLatitude();
                    double destLongitude = event.getLongitude();

                    int distance = Utils.distanceBetweenTwoLocations(curLatitude, curLongitude,
                            destLatitude, destLongitude);
                    if (distance <= 10) {
                        events.add(event);
                    }
                }

                // Set up every events
                for (Event event : events) {
                    // Create marker
                    MarkerOptions marker = new MarkerOptions().position(
                            new LatLng(event.getLatitude(), event.getLongitude())
                    ).title(event.getTitle());

                    // Change marker icon
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                    // Add marker
                    googleMap.addMarker(marker);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
