package com.pedroapp.labtest1_pedro;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;

    Polyline line;
    Polygon shape;
    static LatLng myLocation;
    private static final int POLYGON_SIDES = 4;

    List<Marker> markers = new ArrayList();
    List<Marker> Textmarkers = new ArrayList();
    List<Polyline> Polylines = new ArrayList();
    public float perimeter = 0;

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();


        // apply long press gesture
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                String MarkerTitle = "";
                switch(markers.size()){
                    case 0:
                        MarkerTitle = "A";
                        break;
                    case 1:
                        MarkerTitle = "B";
                        break;
                    case 2:
                        MarkerTitle = "C";
                        break;
                    case 3:
                        MarkerTitle = "D";
                        break;
                    default:
                        MarkerTitle = "A";
                        break;
                }

                setMarker(latLng, MarkerTitle );


                switch(MarkerTitle){
                    case "B":
                        drawLine(markers.get(0).getPosition(),markers.get(1).getPosition());
                        break;
                    case "C":
                        drawLine(markers.get(1).getPosition(),markers.get(2).getPosition());
                        break;
                    case "D":
                        drawLine(markers.get(2).getPosition(),markers.get(3).getPosition());
                        drawLine(markers.get(3).getPosition(),markers.get(0).getPosition());
                        break;
                    default:
                        break;
                }

            }

            private void setMarker(LatLng latLng, String title) {

                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);

                String distance = String.valueOf(df.format(calcDistance(latLng)))+"km";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .snippet(distance);

                if (markers.size() == POLYGON_SIDES)
                    clearMap();

                markers.add(mMap.addMarker(options));
                if (markers.size() == POLYGON_SIDES)
                    drawShape();
            }

            private void drawShape() {
                PolygonOptions options = new PolygonOptions()
                        .clickable(true)
                        .fillColor(0x353A5F0B)
                        .strokeColor(Color.RED)
                        .strokeWidth(5);

                for (int i=0; i<POLYGON_SIDES; i++) {
                    options.add(markers.get(i).getPosition());
                }

                shape = mMap.addPolygon(options);

            }

            private void drawLine(LatLng firstPosition, LatLng secondposition) {
                PolylineOptions options = new PolylineOptions()
                        .clickable(true)
                        .color(Color.RED)
                        .width(10)
                        .add(firstPosition, secondposition);

                float distance = calcDistanceLine(firstPosition,secondposition);
                 perimeter += distance;


                Polylines.add(mMap.addPolyline(options));

            }

            public void clearMap() {

                for (Marker marker: markers)
                    marker.remove();

                for (Polyline polyline: Polylines)
                    polyline.remove();

                for (Marker marker: Textmarkers)
                    marker.remove();

                Textmarkers.clear();
                Polylines.clear();
                markers.clear();
                shape.remove();
                shape = null;
                perimeter = 0;
            }


        });

    mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(@NonNull Polyline polyline) {

            List<LatLng> points = polyline.getPoints();

            LatLng pointA = points.get(0);
            LatLng pointB = points.get(1);

            Double latitudeC = (pointA.latitude + pointB.latitude)/2;
            Double longitudeC = (pointA.longitude + pointB.longitude)/2;

            LatLng pointC = new LatLng(latitudeC,longitudeC);

            float distanceBetween = calcDistanceLine(pointA,pointB);

            String result = String.valueOf(distanceBetween);

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);

            String distance = String.valueOf(result)+"km";


            MarkerOptions options = new MarkerOptions()
                    .icon(createPureTextIcon(distance))
                    .position(pointC);

           Textmarkers.add(mMap.addMarker(options));

        }
    });


    mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
        @Override
        public void onPolygonClick(@NonNull Polygon polygon) {

            Toast.makeText(MapsActivity.this,"Perimeter: "+ String.valueOf(perimeter)+ "km",0).show();
        }
    });


    //On click Marker Show address
    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(@NonNull Marker marker) {


            Geocoder geocoder = new Geocoder(MapsActivity.this);


                Double markerLongitude = marker.getPosition().longitude;
                Double markerLatitude = marker.getPosition().latitude;


                try {
                    List<Address> addresses = geocoder.getFromLocation(markerLatitude,markerLongitude,1);
                    Address address = addresses.get(0);

                    String Street = address.getThoroughfare();
                    String AdNumber = address.getFeatureName();
                    String postalCode = address.getPostalCode();
                    String city = address.getSubAdminArea();
                    String province = address.getAdminArea();

                    /*
                    Log.d("Testing", "Street: "+AdNumber+","+Street);
                    Log.d("Testing", "Postalcode: "+postalCode);
                    Log.d("Testing", "City "+city);
                    Log.d("Testing", "Province "+province);
                    */

                    Toast.makeText(MapsActivity.this,AdNumber+", "+Street+", "+postalCode+", "+city+", "+province,Toast.LENGTH_LONG).show();

                }catch(IOException e) {
                    e.printStackTrace();
                }



            return false;
        }
    });


    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(@NonNull Marker marker) {

        }

        @Override
        public void onMarkerDrag(@NonNull Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(@NonNull Marker marker) {

        }
    });

    }

    // I use this to draw a text in the map
    public BitmapDescriptor createPureTextIcon(String text) {

        Paint textPaint = new Paint(); // Adapt to your needs

        textPaint.setTextSize(42);
        textPaint.setARGB(255,148,0,211);

        float textWidth = textPaint.measureText(text);
        float textHeight = textPaint.getTextSize();
        int width = (int) (textWidth);
        int height = (int) (textHeight);

        Bitmap image = Bitmap.createBitmap(width,height+20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height);

        canvas.drawColor(Color.BLACK);

        canvas.drawText(text, 0, 0, textPaint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
        return icon;
    }


    public static float calcDistance(LatLng latLng) {

        Location a = new Location("a");
        a.setLongitude(myLocation.longitude);
        a.setLatitude(myLocation.latitude);

        Location b = new Location("b");
        b.setLatitude(latLng.latitude);
        b.setLongitude(latLng.longitude);

        return a.distanceTo(b)/1000;
    }

    public static float calcDistanceLine(LatLng latLngA,LatLng latLngB) {

        Location a = new Location("a");
        a.setLongitude(latLngA.latitude);
        a.setLatitude(latLngA.longitude);

        Location b = new Location("b");
        b.setLatitude(latLngB.latitude);
        b.setLongitude(latLngB.longitude);

        return a.distanceTo(b)/1000;
    }


    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);

        /*Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setHomeMarker(lastKnownLocation);*/
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 8));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
            }
        }
    }

}