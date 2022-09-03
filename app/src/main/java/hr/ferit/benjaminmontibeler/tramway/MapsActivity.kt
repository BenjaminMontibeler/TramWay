package hr.ferit.benjaminmontibeler.tramway

import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import hr.ferit.benjaminmontibeler.tramway.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var db = Firebase.firestore
    var location1 = LatLng(0.0,0.0)
    var location2 = LatLng(0.0,0.0)
    var polyline_final: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityMapsBinding.inflate(layoutInflater)
     setContentView(binding.root)

        val autotextView1 = findViewById<AutoCompleteTextView>(R.id.idSearchView1)
        val autotextView2 = findViewById<AutoCompleteTextView>(R.id.idSearchView2)
        val stops = resources.getStringArray(R.array.stop_array)
        val adapter1 = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, stops)
        autotextView1.setAdapter(adapter1)
        val adapter2 = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, stops)
        autotextView2.setAdapter(adapter2)

        val textView = findViewById<TextView>(R.id.TextView)
        val btn_click_me = findViewById<Button>(R.id.idbutton)

        var message1 = ""
        var message2 = ""

        autotextView1.setOnItemClickListener(OnItemClickListener { parent, view, position, rowId ->
            val selection1 = parent.getItemAtPosition(position) as String
            message1 = selection1
            //TODO Do something with the selected text
        })

        autotextView2.setOnItemClickListener(OnItemClickListener { parent, view, position, rowId ->
            val selection2 = parent.getItemAtPosition(position) as String
            message2 = selection2
            //TODO Do something with the selected text
        })

        btn_click_me.setOnClickListener {

            /*var polyline1 = mMap.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    )*/
            db.collection("Trams").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var bool = false
                    var publilcbool1 = false
                    var publilcbool2 = false
                    for (document in task.result) {
                        if (document != null) {

                            //val value = document.get("Stops") as List<*>
                            val value2 = document.getString("Linija")

                            val value = document["Stops"] as List<String>?

                            if (value != null) {
                                if (value.contains(message1) && value.contains(message2)){
                                    textView.setText(value2.toString())
                                    bool = true

                                    db.collection("Linije").get().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            for (doc in task.result) {
                                                if (doc != null) {
                                                    if (doc.getString("title").equals(message1)) {

                                                        val geoPoint1: GeoPoint? =
                                                            doc.getGeoPoint("GeoPoint")
                                                        location1 = LatLng(
                                                            geoPoint1!!.latitude,
                                                            geoPoint1!!.longitude
                                                        )
                                                        publilcbool1 = true
                                                    }

                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 16.0f))

                                                    if(doc.getString("title").equals(message2)){

                                                        val geoPoint2: GeoPoint? = doc.getGeoPoint("GeoPoint")
                                                        location2 = LatLng(
                                                            geoPoint2!!.latitude, geoPoint2!!.longitude
                                                        )
                                                        publilcbool2 = true
                                                    }

                                                    if(publilcbool1 && publilcbool2) {

                                                        if (polyline_final!=null)
                                                        {
                                                            polyline_final!!.remove();
                                                        }

                                                        //polyline1.remove()
                                                         polyline_final = mMap.addPolyline(
                                                            PolylineOptions()
                                                                .clickable(true)
                                                                .add(
                                                                    location1,
                                                                    location2
                                                                )
                                                        )

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!bool){
                        textView.setText("No direct tram lines.")
                        if (polyline_final!=null)
                        {
                            polyline_final!!.remove();
                        }
                    }
                }
            }
        }

        db = FirebaseFirestore.getInstance()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        //val db = FirebaseFirestore.getInstance()
        db.collection("Linije").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    if (document != null) {
                        val geoPoint: GeoPoint? = document.getGeoPoint("GeoPoint")
                        val value = document.getString("title")
                        val location = LatLng(
                            geoPoint!!.latitude, geoPoint!!.longitude
                        )

                        mMap.addMarker(MarkerOptions().position(location).title(value))

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))

                    }
                }
            }
        }


    }

            // Add a marker in Sydney and move the camera
            //val sydney = LatLng(-34.0, 151.0)
            //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
}


