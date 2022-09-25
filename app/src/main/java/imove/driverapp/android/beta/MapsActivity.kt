package imove.driverapp.android.beta

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import imove.driverapp.android.beta.R
import imove.driverapp.android.beta.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

  private lateinit var map: GoogleMap
  private lateinit var binding: ActivityMapsBinding

  private val presenter = MapPresenter(this)
  private var counter = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)

    binding = ActivityMapsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment = supportFragmentManager
        .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    binding.btnStartStop.setOnClickListener {
      if (binding.btnStartStop.text == getString(R.string.start_label)) {
        startTracking()
        binding.btnStartStop.setText(R.string.stop_label)
      } else {
        stopTracking()
        binding.btnStartStop.setText(R.string.start_label)
      }
    }

    presenter.onViewCreated()
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
    map = googleMap

    presenter.ui.observe(this) { ui ->
      updateUi(ui)
    }

    presenter.onMapLoaded()
    map.uiSettings.isZoomControlsEnabled = true
  }

  private fun startTracking() {
    binding.container.txtDistance.text = ""
    binding.container.txtTime.base = SystemClock.elapsedRealtime()
    binding.container.txtTime.start()
    map.clear()

    presenter.startTracking()
  }

  private fun stopTracking() {
    presenter.stopTracking()
    binding.container.txtTime.stop()
  }

  @SuppressLint("MissingPermission")
  private fun updateUi(ui: Ui) {

    if (ui.currentLocation != null && ui.currentLocation != map.cameraPosition.target) {
      map.isMyLocationEnabled = true
      if (counter == 1) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ui.currentLocation, 14f))
        counter++
      }
    }
    binding.container.txtDistance.text = ui.formattedDistance
    drawRoute(ui.userPath)
  }

  private fun drawRoute(locations: List<LatLng>) {
    val polylineOptions = PolylineOptions()

    map.clear()

    val points = polylineOptions.points
    points.addAll(locations)

    map.addPolyline(polylineOptions)
  }
}