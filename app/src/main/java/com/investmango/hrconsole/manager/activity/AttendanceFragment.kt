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
import android.os.Looper
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
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
import com.google.android.gms.location.Priority
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
import com.investmango.hrconsole.model.MeetingBreakResp
import com.investmango.hrconsole.model.TodayAttendnce
import com.investmango.hrconsole.model.User
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.SharedUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Objects

class AttendanceFragment : Fragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userId: Long = 0
    private var token: String = ""
    lateinit var binding: FragmentAttendanceBinding
    lateinit var apiInterface: ApiInterface
    private var mLocationCallback: LocationCallback? = null
    var mLocationClient: FusedLocationProviderClient? = null
//    private var mMap: GoogleMap? = null
private lateinit var mMap: GoogleMap
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
    var gpsEnable: Boolean = false
    lateinit var progressDialog: AwesomeProgressDialog
    var isWithin10km: Boolean = false
    var Office_Lat: Double = 0.0
    var Office_Long: Double = 0.0
     var breakId:Int = 0
     var meetId:Int = 0
     var breakReason:String = ""
    private var lastSelectedPosition = 0  // Store previous valid selection

    //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        mLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
//        mLocationCallbackInitialization()
//        geocoder = Geocoder(requireContext())
//
//        val time = Calendar.getInstance().time
//        val formatter = SimpleDateFormat("HH:mm")
//        current = formatter.format(time)
//
//
//        progressDialog = AwesomeProgressDialog(context)
//        progressDialog.addTitle("Loading...") // add your title here.
//        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
//        progressDialog.isCancelable(false)
//        progressDialog.showDialog();
//
//        gpsEnable = isGpsEnabled()
//
//
//    }
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize location client
    mLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

    // Initialize location callback
    mLocationCallbackInitialization()

    // Initialize geocoder
    geocoder = Geocoder(requireContext())

    // Get current time if needed
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("HH:mm")
    current = formatter.format(time)

    // Initialize progress dialog
    progressDialog = AwesomeProgressDialog(requireContext()).apply {
        addTitle("Getting your location...")
        setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        isCancelable(false)
    }

    // Check if location permissions are granted before proceeding
    if (checkLocationPermission()) {
        // Check if GPS is enabled
        gpsEnable = isGpsEnabled()

        if (gpsEnable) {
            progressDialog.showDialog()
            // Start location updates
            getLocationUpdates()
        }
    } else {
        // Will request permissions in checkLocationPermission()
        Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
    }
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        setupLeaveTypeSpinner()
        getLocationUpdates()
        getCurrentUser();
        getTodayAttendance()

        binding.punchIn.setOnClickListener {
            saveUserInTimeAndLocation()
        }
        binding.punchOut.setOnClickListener {
            updateUserOutTimee()
        }
        binding.takeBreak.setOnClickListener {
            AddBreak(0);
        }
        binding.backToWork.setOnClickListener {
//            if (breakReason=="Meeting")
//            {
//                EndMeet()
//            }else
             EndBreak()
        }
//        binding.swipeLayout.setOnActionsListener(object : SwipeActionsListener {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onOpen(direction: Int, isContinuous: Boolean) {
//                if (direction == SwipeLayout.RIGHT) {
//                  Log.e("ACTION_MOVEG", "onViewCreated: right")
//                    binding.swipeIn.visibility = View.GONE
//                    binding.swipeOut.visibility = View.VISIBLE
//                    binding.someswipe.visibility = View.GONE
//                    updateUserOutTimee("out")
//
//                } else if (direction == SwipeLayout.LEFT) {
//                 Log.e("ACTION_MOVEG", "onViewCreated: left")
//                    binding.swipeOut.visibility = View.GONE
//                    binding.swipeIn.visibility = View.VISIBLE
//                    binding.someswipe.visibility = View.GONE
////                        if (isWithin10km)
//                    saveUserInTimeAndLocation("In")
////                        else Toast.makeText(context,"Not in Range",Toast.LENGTH_SHORT).show()
//
//                }
//            }
////            }
//
//            override fun onClose() {
//            }
//
//        })
//
//        binding.swipeIn.setOnClickListener {
//            binding.someswipe.visibility = View.VISIBLE
//            binding.swipeIn.visibility = View.GONE
//        }


        binding.AttendanceReport.setOnClickListener {
            (activity as (ManagerActivity)).replaceFragment(AttendanceListFragment())
        }
        binding.breakReports.setOnClickListener {
            (activity as (ManagerActivity)).replaceFragment(BreakListFragment())
        }

        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveUserInTimeAndLocation() {
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
                            showbreaksSection()
                            Toast.makeText(
                                context,
                                resp,
                                Toast.LENGTH_SHORT
                            ).show()
                            if (resp?.equals("You are already in.") == true) {
                                binding.InTime.text = DateAndTimeUtility.getTimeInHourFromLong(
                                    inTime
                                )
                            } else
                                binding.InTime.text = current
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
//                        SharedUtils(requireContext()).setLastUsedTimeInSharedPreference(
//                            DateAndTimeUtility.getCurrentDateAndTime()
//                        )

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
                Toast.makeText(
                    context,
                    "Please check your internet and Try again .",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUserOutTimee() {

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
                apiInterface.updateUserAttendance(attendance, userId)
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>,
                ) {
                    if (response.code() == 200) {
                        if (isAdded)
                            progressDialog.dismissDialog()
                        try {
                            val resp = Objects.requireNonNull(response.body())!!.string()

                            Toast.makeText(
                                context,
                                resp,
                                Toast.LENGTH_SHORT
                            ).show()
//                            if (resp?.equals("You are already out") == true) {
//                                hidebreaksSection()
//                            } else
//                                hidebreaksSection()

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
                Toast.makeText(
                    context,
                    "Please check your internet and Try again .",
                    Toast.LENGTH_SHORT
                ).show()
                mLocationCallbackInitialization()
                tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
            }
        }

    }

    fun AddBreak(meetId: Int ) {
        val latLng = "$latitude,$longitude"

        val jsonObject = JSONObject().apply {
            put("userId", userId)
            put("reason", binding.breakType.selectedItem.toString())
            if (binding.breakType.selectedItem.toString().equals("Meeting") )
                put("inLatLong",latLng)
        if (meetId!=0)
    put("meetingId",meetId)
        }

        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<String> = apiInterface.AddBreak(requestBody)

        call.enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.e("backToWork", "Success: $response")
                    Toast.makeText(requireContext(), "Break started", Toast.LENGTH_SHORT).show()
                    getTodayAttendance();
                    ShowBacktoWork()
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage="Something went wrong. "
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                         errorMessage = jsonObject.optString("message", "Error occurred")
                        } catch (e: Exception) {
                            Log.e("backToWork", "Error parsing error message", e)
                        }
                    }
                    Log.e("backToWork", "Error: $response.code() - $errorBody")
                    Toast.makeText(requireContext(), "$errorMessage", Toast.LENGTH_LONG).show()

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
                Log.e("backToWork", "onFailure: $t")
            }
        })
    }
    fun  AddMeeting(jsonObject:JSONObject) {

        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<MeetingBreakResp> = apiInterface.AddMeetingBreak(requestBody)

        call.enqueue(object : Callback<MeetingBreakResp> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<MeetingBreakResp>, response: Response<MeetingBreakResp>) {
                if (response.isSuccessful) {
                    Log.e("backToWork", "Success: $response")
                    Toast.makeText(requireContext(), "Meeting Added.", Toast.LENGTH_SHORT).show()
                    response.body()?.id?.let { AddBreak(it) }
                }
                else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage="Something went wrong. "
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                         errorMessage = jsonObject.optString("message", "Error occurred")
                        } catch (e: Exception) {
                            Log.e("backToWork", "Error parsing error message", e)
                        }
                    }
                    Log.e("backToWork", "Error: $response.code() - $errorBody")
                    Toast.makeText(requireContext(), "$errorMessage", Toast.LENGTH_LONG).show()

                }
            }

            override fun onFailure(call: Call<MeetingBreakResp>, t: Throwable) {
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
                Log.e("backToWork", "onFailure: $t")
            }
        })
    }
    fun EndMeet() {
        val latLng = "$latitude,$longitude"

        val jsonObject = JSONObject().apply {
         put("id", meetId)
         put("outLatLong",latLng)

        }

     val requestBody =
         RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

     val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<String> = apiInterface.StopMeeting(breakId,requestBody)

        call.enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    EndBreak()
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage="Something went wrong. "
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                         errorMessage = jsonObject.optString("message", "Error occurred")
                        } catch (e: Exception) {
                            Log.e("backToWork", "Error parsing error message", e)
                        }
                    }
                    Log.e("backToWork", "Error: $response.code() - $errorBody")
                    Toast.makeText(requireContext(), "$errorMessage", Toast.LENGTH_LONG).show()

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
                Log.e("backToWork", "onFailure: $t")
            }
        })
    }
    fun EndBreak() {
        val latLng = "$latitude,$longitude"

        val jsonObject = JSONObject().apply {
         put("id", breakId)
//            if (binding.breakType.selectedItem.toString().equals("Meeting") )
                put("outLatLong",latLng)

        }

     val requestBody =
         RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

     val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<String> = apiInterface.StopBreak(breakId,requestBody)

        call.enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.e("backToWork", "Success: $response")
                    Toast.makeText(requireContext(), "Break Ended", Toast.LENGTH_SHORT).show()
                    showbreaksSection()
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage="Something went wrong. "
                    errorBody?.let {
                        try {
                            val jsonObject = JSONObject(it)
                         errorMessage = jsonObject.optString("message", "Error occurred")
                        } catch (e: Exception) {
                            Log.e("backToWork", "Error parsing error message", e)
                        }
                    }
                    Log.e("backToWork", "Error: $response.code() - $errorBody")
                    Toast.makeText(requireContext(), "$errorMessage", Toast.LENGTH_LONG).show()

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
                Log.e("backToWork", "onFailure: $t")
            }
        })
    }

    fun hidebreaksSection() {
        binding.backToWork.visibility=View.GONE
        binding.reasons.visibility = View.GONE
        binding.breaks.visibility = View.GONE
        binding.punchIn.visibility = View.VISIBLE

    }

    fun showbreaksSection() {
        binding.backToWork.visibility=View.GONE
        binding.reasons.visibility = View.VISIBLE
        binding.breaks.visibility = View.VISIBLE
        binding.punchIn.visibility = View.GONE

    }
    fun ShowBacktoWork(){
        binding.backToWork.visibility=View.VISIBLE
        binding.reasons.visibility = View.GONE
        binding.breaks.visibility = View.GONE
        binding.punchIn.visibility = View.GONE
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
                Log.e("TodayAttendnce", "onResponse: " + response.body())
                if (response.isSuccessful()) {
                    if (response.body()!!.id != null) {
                        outId = response.body()?.id!!
                        inTime = response.body()!!.inTime!!
                        outTime = response.body()!!.outTime!!
                    }
                    if (response.body()?.outLat != null && !response.body()?.outLat?.equals(0)!!) {
                        latitude = response.body()?.outLat!!
                        longitude = response.body()?.outLong!!
                    } else if (response.body()?.inLat != null && !response.body()?.inLat?.equals(0)!!) {
                        latitude = response.body()?.inLat!!
                        longitude = response.body()?.inLong!!
                    }
                    if (inTime != 0L) {
                        binding.InTime.setText(DateAndTimeUtility.getTimeInHourFromLong(response.body()?.inTime))
                        if (response.body()!!.serviceRecords!=null) {
                            if (response.body()!!.serviceRecords?.startTime == null) {
                                showbreaksSection()
                                Log.e("breaks", "onResponse: showbreaksSection " , )
                            } else {
                                if (response.body()!!.serviceRecords?.endTime != null) {
                                    showbreaksSection()
                                    Log.e("breaks", "onResponse: showbreaksSection " +response.body()!!.serviceRecords?.endTime   )

                                } else {
                                    Log.e("breaks", "onResponse: ShowBacktoWork " +response.body()!!.serviceRecords?.id?.toInt()!! )

                                    breakId = response.body()!!.serviceRecords?.id?.toInt()!!
//                                    meetId = response.body()!!.serviceRecords?.id?.toInt()!!
                                    breakReason = response.body()!!.serviceRecords?.reason!!
                                    binding.BreakTime.text =
                                        DateAndTimeUtility.getTimeInHourFromLong(response.body()!!.serviceRecords?.startTime)
                                    ShowBacktoWork()
                                }
                            }
                        }else{
                            showbreaksSection()
                        }

                    }
                }

            }

            override fun onFailure(call: Call<TodayAttendnce?>, t: Throwable) {
                if (isAdded)
                    Toast.makeText(context, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                Objects.requireNonNull(t.message)?.let { Log.e("Failure", it) }
                progressDialog.dismissDialog()
            }
        })
        return outTime
    }

    private fun setupLeaveTypeSpinner() {
        val list = resources.getStringArray(R.array.BreakType)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.color_spinner_layout, list)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        binding.breakType.setAdapter(arrayAdapter)

        binding.breakType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "Meeting") {
                    binding.breakType.setSelection(lastSelectedPosition) // Revert selection first
                    showMeetingPopup(position)
                } else {
                    lastSelectedPosition = position // Update last valid selection
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    fun showMeetingPopup(position:Int){
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        val latLng = "$latitude,$longitude"

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.meeting_form, null)
        builder.setView(customLayout)

        val channelPartnerName = customLayout.findViewById<EditText>(R.id.channelPartnerName)
        val ClientEmail = customLayout.findViewById<EditText>(R.id.ClientEmail)
        val ClientPhone = customLayout.findViewById<EditText>(R.id.ClientPhone)
        val Source = customLayout.findViewById<EditText>(R.id.Source)
        val ClientName = customLayout.findViewById<EditText>(R.id.ClientName)
        val Feedback = customLayout.findViewById<EditText>(R.id.Feedback)
         val ClientAddress = customLayout.findViewById<EditText>(R.id.ClientAddress)
        val ProjectDetails = customLayout.findViewById<EditText>(R.id.ProjectDetails)
        val checkBox =customLayout.findViewById<CheckBox>(R.id.checkbox)

        val okBtn = customLayout.findViewById<TextView>(R.id.ok_btn)
        val cancel = customLayout.findViewById<TextView>(R.id.cancel)


        val dialog = builder.create()
        okBtn.setOnClickListener {
            val isChecked = checkBox.isChecked
            val requiredFields = if (isChecked) {
                listOf(Source, ClientName, Feedback, ClientEmail, ClientPhone)
            } else {
                listOf(Source, ClientName, Feedback, ClientPhone)
            }
            val allFieldsFilled = requiredFields.all { it.text.isNotEmpty() }

            if (!allFieldsFilled) {
                Toast.makeText(context, "Fill all necessary details.", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.PHONE.matcher(ClientPhone.text).matches()) {
                Toast.makeText(context, "Enter a valid phone number.", Toast.LENGTH_SHORT).show()
            } else if ( ClientEmail.text.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(ClientEmail.text).matches()) {
                Toast.makeText(context, "Enter a valid email address.", Toast.LENGTH_SHORT).show()
            }
            else {
                lastSelectedPosition = position // Only update selection when valid
                binding.breakType.setSelection(position)
                val jsonObject = JSONObject().apply {
                    put("clientName", ClientName.text.toString())
                    put("clientPhone", ClientPhone.text.toString())
                    put("clientAddress", ClientAddress.text.toString())
                    put("feedback", Feedback.text.toString())
                    put("channelPartnerName", channelPartnerName.text.toString())
                    put("projectDetails", ProjectDetails.text.toString())
                    put("source", Source.text.toString())
                    put("clientEmail",ClientEmail.text.toString())
                    put("inLatLong",latLng)
                }
                AddMeeting(jsonObject);
                dialog.dismiss()
            }
        }
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getCurrentUser() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val call: Call<User> = apiInterface.getCurrentUser()
        call.enqueue(object : Callback<User?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful && response.body()?.latlong!=null) {

                    val latlong = (response.body()?.latlong)!!.split(",")
                    Office_Lat = latlong[0].toDouble()
                    Office_Long = latlong[1].toDouble()
                    Log.e("Office_Long", "onResponse: " +response)
                    progressDialog.dismissDialog()
                } else {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            context,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                progressDialog.dismissDialog()
                val errorMessage = "Error: " + t.message
                Log.e("LoginError", errorMessage)
            }
        })
    }

    private fun mLocationCallbackInitialization() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        userLocationResult(location)
                        break
                    }
                }
            }
        }
    }

    private fun userLocationResult(location: Location) {
        latitude = location.latitude
        longitude = location.longitude

        Log.d("Location", "Updated location: $latitude, $longitude")

        // Update location model
        val locationModel = LocationModel().apply {
            dateAndTime = DateAndTimeUtility.getCurrentDateAndTime()
            latitude = location.latitude
            longitude = location.longitude
        }

        // If map is ready, add marker
        if (::mMap.isInitialized) {
            tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
        }
    }

    private fun checkLocationPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), permissions, MY_PERMISSIONS_REQUEST)
            return false
        }
        return true
    }
    private fun getLocationUpdates() {
        if (!checkLocationPermission()) return

        val locationRequest = LocationRequest.Builder(10000) // interval in milliseconds
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(5000) // minimum interval in milliseconds
            .setMaxUpdateDelayMillis(60000)   // maximum delay in milliseconds
            .build()

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationClient?.requestLocationUpdates(
                locationRequest,
                mLocationCallback!!,
                Looper.getMainLooper()
            )

            // Get last known location immediately
            mLocationClient?.lastLocation
                ?.addOnSuccessListener { location ->
                    location?.let {
                        userLocationResult(it)
                    }
                }
                ?.addOnFailureListener { e ->
                    Log.e("Location", "Failed to get last location: ${e.message}")
                }
        }
    }

    fun stopLocationUpdate() {
        mLocationClient?.removeLocationUpdates(mLocationCallback!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
        }

        // Enable my location layer if permission is granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
        }

        // Check if location is already available
        if (latitude != 0.0 && longitude != 0.0) {
            tryToAddMarkerAndZoom(requireContext(), latitude, longitude, MAX_RETRIES)
        } else {
            // If location is not yet available, use a default location
            val defaultLocation = LatLng(0.0, 0.0) // Replace with appropriate default
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 2f))

            // Start location updates if not already started
            if (checkLocationPermission() && gpsEnable) {
                getLocationUpdates()
            }
        }
    }

    private fun tryToAddMarkerAndZoom(
        context: Context,
        latitudee: Double,
        longitudee: Double,
        retries: Int
    ) {
        if (retries <= 0) {
            if (isAdded) {
                // Even if geocoding fails, at least show the marker at the coordinates
                addMarkerWithoutAddress(latitudee, longitudee)
                progressDialog.dismissDialog()
                Toast.makeText(context, "Using coordinates. Address lookup failed.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Set a timeout for the geocoding operation
                val addressList = withContext(Dispatchers.IO) {
                    withTimeoutOrNull(5000) { // 5 second timeout
                        try {
                            getAddressFromLocation(context, latitudee, longitudee)
                        } catch (e: IOException) {
                            Log.e("Geocoder", "IOException: ${e.message}")
                            null
                        }
                    }
                }

                if (!addressList.isNullOrEmpty()) {
                    latitude = latitudee
                    longitude = longitudee
                    progressDialog.dismissDialog()

                    val address = addressList[0]
                    addMarkerWithAddress(latitudee, longitudee, address)
                    checkRange10km(latitudee, longitudee)
                } else {
                    // If geocoding fails, try up to 3 times with increasing delays
                    if (retries > 1) {
                        delay(1000L * (MAX_RETRIES - retries + 1)) // Increasing delay
                        tryToAddMarkerAndZoom(context, latitudee, longitudee, retries - 1)
                    } else {
                        // Last attempt failed, show marker without address
                        addMarkerWithoutAddress(latitudee, longitudee)
                        progressDialog.dismissDialog()
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error during geocoding: ${e.message}")
                if (retries > 1) {
                    delay(1000L * (MAX_RETRIES - retries + 1))
                    tryToAddMarkerAndZoom(context, latitudee, longitudee, retries - 1)
                } else {
                    addMarkerWithoutAddress(latitudee, longitudee)
                    progressDialog.dismissDialog()
                }
            }
        }
    }

    private suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): List<Address>? {
        return withContext(Dispatchers.IO) {
            try {
                Geocoder(context).getFromLocation(latitude, longitude, 1)
            } catch (e: IOException) {
                Log.e("Geocoder", "Error getting address: ${e.message}")
                null
            }
        }
    }

    // Helper function to add marker without address
    private fun addMarkerWithoutAddress(lat: Double, lng: Double) {
        val markerPosition = LatLng(lat, lng)
        val marker = mMap?.addMarker(
            MarkerOptions()
                .position(markerPosition)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(markerPosition, 14f)
        )

        checkRange10km(lat, lng)
    }

    // Helper function to add marker with address
    private fun addMarkerWithAddress(lat: Double, lng: Double, address: Address) {
        val markerPosition = LatLng(lat, lng)
        val addressText = buildAddressText(address)

        val marker = mMap?.addMarker(
            MarkerOptions()
                .position(markerPosition)
                .title(addressText)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(markerPosition, 14f)
        )

        checkRange10km(lat, lng)
    }

    private fun buildAddressText(address: Address): String {
        val parts = listOfNotNull(
            address.featureName,
            address.subLocality,
            address.locality,
            address.countryName
        ).filter { it.isNotEmpty() }

        return if (parts.isNotEmpty()) parts.joinToString(", ") else "Current Location"
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                    gpsEnable = isGpsEnabled()
                    if (gpsEnable) {
                        progressDialog.showDialog()
                        getLocationUpdates()
                    }
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Location permission is necessary for this feature", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lastSelectedPosition=0;
        binding.breakType.setSelection(lastSelectedPosition)

        // Check if GPS is enabled when returning to the fragment
        if (!isGpsEnabled()) {
            gpsEnable = false
            return
        }

        gpsEnable = true

        // Check permissions before requesting location updates
        if (checkLocationPermission()) {
            // Show progress dialog if we need to get location again
            if (latitude == 0.0 || longitude == 0.0) {
                progressDialog.showDialog()
            }

            // Start location updates
            getLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog.dismissDialog()
    }



fun checkRange10km(latitudee: Double, longitudee: Double) {
        if (latitude != 0.0 && longitude != 0.0 && Office_Lat != 0.0 && Office_Long != 0.0) {
            val results = FloatArray(1)
            Location.distanceBetween(
                Office_Lat,
                Office_Long,
                latitudee,
                longitudee,
                results
            )
            val distanceInMeters = results[0]
            isWithin10km = distanceInMeters < 2000
            Log.e(
                "isWithin10km",
                "onViewCreated: " + isWithin10km + " " + latitudee + " " + longitudee
            )
            if (!isWithin10km) {
                binding.notOfficeRange.visibility = View.VISIBLE
            } else binding.notOfficeRange.visibility = View.GONE

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::progressDialog.isInitialized) {
            try {
                progressDialog.dismissDialog()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}