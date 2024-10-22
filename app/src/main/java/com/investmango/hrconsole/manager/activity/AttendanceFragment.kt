package com.investmango.hrconsole.manager.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentAttendanceBinding
import com.investmango.hrconsole.model.Attendance
import com.investmango.hrconsole.model.LocationModel
import com.investmango.hrconsole.model.TodayAttendnce
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.SharedUtils
import com.zerobranch.layout.SwipeLayout
import com.zerobranch.layout.SwipeLayout.SwipeActionsListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Objects

class AttendanceFragment : Fragment(), OnMapReadyCallback {

    private var userId: Long = 0
    private var token: String = ""
    lateinit var binding: FragmentAttendanceBinding
    lateinit var apiInterface: ApiInterface
    private var mLocationCallback: LocationCallback? = null
    var mLocationClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    var longitude: Double = 0.0
    private val MAX_RETRIES = 5
    var outId = 0
    var outTime: Long = 0
    var inTime: Long = 0
    lateinit var current: String
    lateinit var addressList: List<Address>
    var latitude = 0.0
    lateinit var geocoder: Geocoder
    private val MY_PERMISSIONS_REQUEST = 1001
      var gpsEnable:Boolean = false
    lateinit var progressDialog: AwesomeProgressDialog
      var isWithin10km:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        mLocationCallbackInitialization()
        geocoder = Geocoder(requireContext())

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH:mm")
        current = formatter.format(time)


        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)
        progressDialog.showDialog();

        gpsEnable= isGpsEnabled()




    }

    private fun isGpsEnabled(): Boolean {
        val locationManager =
            context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (providerEnabled) {
            return true
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Your Location seems to be disabled. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                    context!!.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
            val alert = builder.create()
            alert.show()
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_attendance, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)
        token = preferences.getString("token", "0").toString()


        binding.liveMapOfUser.onCreate(null)
        binding.liveMapOfUser.onResume()
        binding.liveMapOfUser.getMapAsync(this);

//        getCoordinate(userId)

        getLocationUpdates()

        getTodayAttendance()



        binding.swipeLayout.setOnActionsListener(object : SwipeActionsListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onOpen(direction: Int, isContinuous: Boolean) {

//                if (latitude == 0.0 && longitude == 0.0) {
//                    binding.swipeLayout.isEnabledSwipe = false
//
//                    if (isAdded) {
//                        Toast.makeText(context, "Turn On your location.", Toast.LENGTH_SHORT).show()
//
//                        mLocationCallbackInitialization()
//                        tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
//                    }
//                } else {
                    if (direction == SwipeLayout.RIGHT) {
//                    val animationZoomIn = AnimationUtils.loadAnimation(context, R.anim.move)
//                    binding.someswipe.startAnimation(animationZoomIn)
                        Log.e("ACTION_MOVEG", "onViewCreated: right")
                        binding.swipeIn.visibility = View.GONE
                        binding.swipeOut.visibility = View.VISIBLE
                        binding.someswipe.visibility = View.GONE
                        updateUserOutTimee("out")
//                    binding.swipeLayout.openLeft()
//                    binding.swipeLayout.close()

                    } else if (direction == SwipeLayout.LEFT) {
//                    val animationZoomIn = AnimationUtils.loadAnimation(context, R.anim.move)
//                    binding.someswipe.startAnimation(animationZoomIn)
                        Log.e("ACTION_MOVEG", "onViewCreated: left")
                        binding.swipeOut.visibility = View.GONE
                        binding.swipeIn.visibility = View.VISIBLE
                        binding.someswipe.visibility = View.GONE
//                        if (isWithin10km)
                        saveUserInTimeAndLocation("In")
//                        else Toast.makeText(context,"Not in Range",Toast.LENGTH_SHORT).show()

                    }
                }
//            }

            override fun onClose() {
            }

        })

        binding.swipeIn.setOnClickListener {
            binding.someswipe.visibility = View.VISIBLE
            binding.swipeIn.visibility = View.GONE
        }
        binding.AttendanceReport.setOnClickListener {
            (activity as (ManagerActivity)).replaceFragment(AttendanceListFragment())
        }

        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }

    }

    override fun onResume() {
        super.onResume()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveUserInTimeAndLocation(typ: String) {
        var attendance = Attendance()
        val now = Instant.now()
        // Convert to epoch milliseconds
        val epochMillis = now.toEpochMilli()
//        val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
        val latLng = "$latitude,$longitude"
        progressDialog.showDialog();
        attendance.inLatLong = latLng
        attendance.inTime = epochMillis
//            attendance?.inTime=current.toLong()
        if (latitude != 0.0 && longitude != 0.0) {
            Log.e("AttendanceFrag", "saveUserInTimeAndLocation: " + attendance.inLatLong)
            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface
            val call: Call<ResponseBody> = apiInterface.saveAttendance(attendance, userId)
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>,
                ) {
                    if (response.code() == 200) {
                        progressDialog.dismissDialog();
                        try {
                            val resp = Objects.requireNonNull(response.body())?.string()

                            Toast.makeText(
                                context,
                                resp,
                                Toast.LENGTH_SHORT
                            ).show()
                            if (resp?.equals("You are already in.") == true) {
                                binding.inTime.text = DateAndTimeUtility.getTimeInHourFromLong(
                                    inTime
                                )
                            } else
                                binding.inTime.text = current
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        SharedUtils(requireContext()).setLastUsedTimeInSharedPreference(
                            DateAndTimeUtility.getCurrentDateAndTime()
                        )

                    } else {
                        progressDialog.dismissDialog()
                        Toast.makeText(
                            activity,
                            "Server error " + response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Failure", response.message())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    progressDialog.dismissDialog()
                    Toast.makeText(activity, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                    Objects.requireNonNull(t.message)?.let { Log.e("Failure", it) }
                }
            })
        } else {
            if (isAdded) {
                mLocationCallbackInitialization()
                tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
                Toast.makeText(context, "Please check your internet and Try again .", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUserOutTimee(typ: String) {

        var attendance = Attendance()

        val now = Instant.now()
        // Convert to epoch milliseconds
        val epochMillis = now.toEpochMilli()
        val latLng = "$latitude,$longitude"

        attendance.id = outId.toLong()
        attendance.outLatLong = latLng
        attendance.outTime = epochMillis
        progressDialog.showDialog();
//            attendance?.outTime=epochMillis
        if (latitude != 0.0 && longitude != 0.0) {

                Log.e("AttendanceFrag", "saveUserInTimeAndLocation: " + attendance.outLatLong)
            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface
            val call: Call<ResponseBody> =
                apiInterface.updateUserAttendance( attendance, userId)
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>,
                ) {
                    if (response.code() == 200) {
                        progressDialog.dismissDialog()
                        try {
                            val resp = Objects.requireNonNull(response.body())?.string()

                            Toast.makeText(
                                context,
                                resp,
                                Toast.LENGTH_SHORT
                            ).show()
                            if (resp?.equals("You are already out") == true) {
                                binding.outTime.setText(
                                    DateAndTimeUtility.getTimeInHourFromLong(
                                        outTime
                                    )
                                )
                            } else
                                binding.outTime.setText(current)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        SharedUtils(requireContext()).setLastUsedTimeInSharedPreference(
                            DateAndTimeUtility.getCurrentDateAndTime()
                        )

                    } else {
                        progressDialog.dismissDialog()
                        Toast.makeText(
                            activity,
                            "Server error " + response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Failure", response.message())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    progressDialog.dismissDialog()
                    Toast.makeText(activity, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                    Objects.requireNonNull(t.message)?.let { Log.e("Failure", it) }
                }
            })
        } else {

            if (isAdded) {
                Toast.makeText(context, "Please check your internet and Try again .", Toast.LENGTH_SHORT).show()
                mLocationCallbackInitialization()
                tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
            }
        }

    }

    private fun getTodayAttendance(): Long {

        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<TodayAttendnce> = apiInterface.getTodayAttendance(userId)
        call.enqueue(object : Callback<TodayAttendnce?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<TodayAttendnce?>,
                response: Response<TodayAttendnce?>,
            ) {
                Log.e("TodayAttendnce", "onResponse: " + response.body()?.id)
                if (response.body()!!.id != null) {
                    outId = response.body()?.id!!
                    inTime = response.body()!!.inTime!!
                    outTime = response.body()!!.outTime!!
                }
                if (!response.body()?.outLat?.equals(0)!!) {
                    latitude = response.body()?.outLat!!
                    longitude = response.body()?.outLong!!
                } else if (!response.body()?.inLat?.equals(0)!!) {
                    latitude = response.body()?.inLat!!
                    longitude = response.body()?.inLong!!
                }
                if (inTime != 0L) {
                    binding.inTime.setText(DateAndTimeUtility.getTimeInHourFromLong(response.body()?.inTime))
                    binding.swipeIn.visibility = View.VISIBLE
                    binding.someswipe.visibility = View.GONE
                }
                if (outTime != 0L)
                    binding.outTime.setText(DateAndTimeUtility.getTimeInHourFromLong(response.body()?.outTime))
            }

            override fun onFailure(call: Call<TodayAttendnce?>, t: Throwable) {
                Toast.makeText(activity, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                Objects.requireNonNull(t.message)?.let { Log.e("Failure", it) }
            }
        })
        return outTime
    }


    private fun mLocationCallbackInitialization() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (mLocationCallback == null) {
                    return
                }
                val locations = locationResult.lastLocation
                userLocationResult(Objects.requireNonNull<Location?>(locations))

                Log.e("longitude", "onLocationResult: " + locations)
            }
        }
    }

    private fun userLocationResult(locations: Location) {
        val locationModel = LocationModel()
        latitude = locations.latitude
        longitude = locations.longitude
        val latLng = "$latitude,$longitude"
        locationModel.dateAndTime = DateAndTimeUtility.getCurrentDateAndTime()
        locationModel.latitude = latitude
        locationModel.longitude = longitude
        Log.e("longitude", "userLocationResult: " + latitude + "  " + longitude)

    }

    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(5000).setFastestInterval(2000)
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission_group.READ_MEDIA_VISUAL,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                ), MY_PERMISSIONS_REQUEST
            )
        }
        val handlerThread = HandlerThread("LocationThread")
        handlerThread.start()


        mLocationClient?.requestLocationUpdates(
            locationRequest,
            mLocationCallback!!,
            handlerThread.looper
        )

        Handler(Looper.getMainLooper()).postDelayed({
            mLocationClient?.lastLocation
                ?.addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location!=null)
                        userLocationResult(Objects.requireNonNull<Location?>(location))
                }
        }, 300)


    }

    fun stopLocationUpdate() {
        if (mLocationClient != null) {
            mLocationClient?.removeLocationUpdates(mLocationCallback!!)
        }
    }
//    override fun onMapReady(p0: GoogleMap) {
//        mMap = p0
//        p0.mapType = GoogleMap.MAP_TYPE_TERRAIN
//        val someHandler = Handler(Looper.getMainLooper())
//        try {
//
//            someHandler.postDelayed(object : Runnable {
//                override fun run() {
//                    try {
//                        val inLatLng = LatLng(latitude, longitude)
//                         addressList =
//                             geocoder.getFromLocation(latitude, longitude, 1)!!
//                        if (addressList?.size != 0) {
//
//                            val marker = mMap!!.addMarker(
//
//
//                                MarkerOptions()
//                                    .position(inLatLng)
//                                    .title(
//                                        addressList!![0].featureName + ", " + addressList!![0]
//                                            .subLocality + ", " + addressList!![0]
//                                            .locality + ", " + addressList!![0].countryName
//                                    )
//                                    .icon(
//                                        BitmapDescriptorFactory.defaultMarker(
//                                            BitmapDescriptorFactory.HUE_RED
//                                        )
//                                    )
//                            )
//
//                            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(inLatLng, 14f))
//
//                            marker!!.position = inLatLng
//                        } else {
//                            geocoder = Geocoder(requireContext())
//                            mLocationCallbackInitialization()
//
//                             addressList =
//                                 geocoder.getFromLocation(latitude, longitude, 1)!!
//                            Toast.makeText(
//                                context,
//                                "Please on your location.",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            Log.e("latitude", "run: "+ latitude + " "+ longitude +  "  " +  addressList)
//                            return
//                        }
//
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                        Log.e("IOException", "setLiveMap: " + e.message)
//                    }
//                }
//            }, 2000)
//        } catch (e: Exception) {
//
//            Log.e("IOException", "setLiveMap: " + e.message)
//
//        }
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (isAdded)
            tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
    }

    private fun tryToAddMarkerAndZoom(
        context: Context,
        latitudee: Double,
        longitudee: Double,
        retries: Int,
    ) {
        if (retries <= 0) {
            if (isAdded)
                Toast.makeText(
                    context,
                    "Unable to get address after multiple attempts.",
                    Toast.LENGTH_LONG
                ).show()
            return
        }

        geocoder = Geocoder(context)
        val inLatLng = LatLng(latitudee, longitudee)
//         addressList: List<Address>? = null
        val someHandler = Handler(Looper.getMainLooper())
        someHandler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    addressList = geocoder.getFromLocation(latitudee, longitudee, 1)!!

                    if (!addressList.isNullOrEmpty()) {
                        latitude = latitudee
                        longitude = longitudee

                        progressDialog.dismissDialog()

                        val address = addressList[0]
                        val marker = mMap?.addMarker(
                            MarkerOptions()
                                .position(inLatLng)
                                .title("${address.featureName}, ${address.subLocality}, ${address.locality}, ${address.countryName}")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )

                        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(inLatLng, 14f))
                        marker?.position = inLatLng
                        checkRange10km(latitudee, longitudee)
                        progressDialog.dismissDialog();

                    } else {
                        // Retry fetching address if addressList is empty
                        Log.d("Geocoder", "Address list is empty. Retrying...")
                        tryToAddMarkerAndZoom(context, latitude, longitude, retries)

                    }

                } catch (e: IOException) {
                    Log.e("Geocoder", "IOException: ${e.message}")
                    // Retry on IOException
                    progressDialog.dismissDialog()
                    tryToAddMarkerAndZoom(context, latitude, longitude, retries - 1)
                }
            }
        }, 10)
    }

    fun checkRange10km(latitudee: Double, longitudee: Double) {
        if (latitudee != 0.0 && longitudee != 0.0) {
            val results = FloatArray(1)
            Location.distanceBetween(
                28.5916076,
                77.3841801,
                latitudee,
                longitudee,
                results
            )
            val distanceInMeters = results[0]
             isWithin10km = distanceInMeters < 1000
            Log.e(
                "isWithin10km",
                "onViewCreated: " + isWithin10km + " " + latitudee + " " + longitudee
            )


            if (!isWithin10km) {
                binding.notOfficeRange.visibility = View.VISIBLE
            } else binding.notOfficeRange.visibility = View.GONE

        }
    }

}