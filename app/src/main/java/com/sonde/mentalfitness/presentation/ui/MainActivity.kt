package com.sonde.mentalfitness.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.edgeapp.SoundTriggerTestService
import com.sonde.mentalfitness.Constants
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.local.user.UserLocalDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSourceImpl
import com.sonde.mentalfitness.data.repository.UserRepositoryImpl
import com.sonde.mentalfitness.domain.DailyRecordingWorker
import com.sonde.mentalfitness.presentation.ui.checkIn.CheckInHostActivity
import com.sonde.mentalfitness.presentation.ui.textIndependent.EnrollmentActivity
import com.sonde.mentalfitness.presentation.ui.record.foregroundrecording.PassiveForegroundRecordingActivity
import com.sonde.mentalfitness.presentation.ui.setting.SettingActivity
import com.sonde.mentalfitness.presentation.utils.showSnackbar
import com.sonde.mentalfitness.presentation.utils.util.copyBinFile
import com.sonde.mentalfitness.presentation.utils.util.readAndWriteBinFile
import com.twilio.voipcall.VoiceActivity
import com.twilio.voipcall.utils.WaveProcessService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), SoundTriggerTestService.UserActivity {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var viewModel: MainViewModel
    val sharedPreferenceHelper =
        SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
    private lateinit var container: View
    private lateinit var userName: TextView

    private var mRequestMessenger: Messenger? = null
    private var bound: Boolean = false
    private lateinit var serviceIntent: Intent
    private var mService: SoundTriggerTestService? = null
    private var mSelectedModelUuid: UUID? = null


    companion object {
        const val GET_RANDOM = 0
        const val REQUEST_CODE_PASSIVE_MODE = 100
        const val REQUEST_CODE_PASSIVE_MODE_FOREGROUND = 103
        const val REQUEST_CODE_VOIP_CALL = 101
        const val REQUEST_CODE_MOBILE_CALL = 102
        const val MIC_PERMISSION_REQUEST_CODE = 201
        const val DEMO_TYPE = "DEMO_TYPE"
    }

    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )

    val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
//            mRequestMessenger = Messenger(service)
//            bound = true

            synchronized(this@MainActivity) {

                // We've bound to LocalService, cast the IBinder and get LocalService instance
                val binder: SoundTriggerTestService.SoundTriggerTestBinder =
                    service as SoundTriggerTestService.SoundTriggerTestBinder
                mService = binder.getService()
                mService!!.setUserActivity(this@MainActivity)

            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
//            mRequestMessenger = null
//            bound = false
            synchronized(this@MainActivity) {
                mService!!.setUserActivity(null)
                mService = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        container = findViewById(R.id.container)
        userName = findViewById(R.id.textUser)
        observe(viewModel.event, ::onViewEvent)
//        Log.d(TAG,"getFilesDir()==>${this.getFilesDir()}")
//        Log.d(TAG,"getExternalFilesDir()==>${this.getExternalFilesDir(null)?.getAbsolutePath()}")
//        readAndWriteBinFile(this,"rahul.bin")
//        sharedPreferenceHelper.setRecordingServiceRunning(false)

//        getSystemDeviceInfo()
        GlobalScope.launch {
            try {
                userRepository.getUser().collect {
                    when (it) {
                        is Result.Success -> {
                            userName.text = "Hello, ${it.data.firstName} ${it.data.lastName}"
                        }
                        else ->{
                            userName.text = "Hello, User"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("Get User==>", "$e")
                userName.text = "Hello, User"
            }
        }

    }

    private fun getSystemDeviceInfo() {
//        Log.i(TAG, "SERIAL: " + Build.SERIAL);
        Log.i(TAG,"MODEL: " + Build.MODEL);
        Log.i(TAG,"ID: " + Build.ID);
        Log.i(TAG,"Manufacture: " + Build.MANUFACTURER);
        Log.i(TAG,"Hardware: " + Build.HARDWARE);
        Log.i(TAG,"brand: " + Build.BRAND);
        Log.i(TAG,"type: " + Build.TYPE);
        Log.i(TAG,"user: " + Build.USER);
        Log.i(TAG,"BASE: " + Build.VERSION_CODES.BASE);
        Log.i(TAG,"INCREMENTAL " + Build.VERSION.INCREMENTAL);
        Log.i(TAG,"SDK  " + Build.VERSION.SDK);
        Log.i(TAG,"BOARD: " + Build.BOARD);
        Log.i(TAG,"BRAND " + Build.BRAND);
        Log.i(TAG,"HOST " + Build.HOST);
        Log.i(TAG,"FINGERPRINT: "+Build.FINGERPRINT);
        Log.i(TAG,"Version Code: " + Build.VERSION.RELEASE);
    }

    private fun onViewEvent(viewEvent: MainViewEvent) {
        when (viewEvent) {
            is MainViewEvent.PassCallEvent -> {
                launchVoipCall(viewEvent.userName)
            }
        }
    }

    //            calculateScore("/data/data/com.sonde.mentalfitness.debug/files/sp/demo.wav")
//    fun calculateScore(audioFilePath: String) {
//
//        Thread({
//            try {
//                InferenceEngine(MentalFitnessApplication.applicationContext()).getScore(
//                    audioFilePath, MetaData("MALE", "2000"),
//                    HealthCheckType.RESPIRATORY_FITNESS
//                )
//            } catch (e : Exception) {
//                Log.e("!!!", "Error : " + e)
//            }
//        }).start()
//
//
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onUseCaseButtonClick(view: View) {
        when (view.id) {
            R.id.btn_ahh_respiratory -> {
                launchCheckInScreen()
            }
            R.id.btn_voip_call -> {
                viewModel.onPassiveCallButtonClick()
            }
            R.id.btn_free_respiratory -> {
                launchFreeSpeechFlow();
            }
            R.id.btn_mental_fitness -> {
                launchMentalFitness();
            }
            R.id.btn_passive -> {
//                startSoundTriggerService()
                //startServiceOfAnotherApp()
                launchPassiveMode()
            }
            R.id.btn_passive_foreground -> {
                launchPassiveModeForeground()
            }
            R.id.btn_passive_mobile -> {
//                stopServiceOfAnotherApp()
                launchMobileCall()
//                startSoundTrigger()
//                copyBinFile(this, "rahul.bin")
            }
//            R.id.btn_call_ipc -> {
//                loadAndStart()
////                callIPC()
//            }
//            R.id.btn_call_start -> {
//                startSoundTrigger()
////                callIPC()
//            }
            R.id.img_setting -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
            R.id.img_power -> {
                Log.d(TAG, "Power Button Clicked==>>")
                stopAllServicesAndRestart()
            }
        }
    }

    private fun stopAllServicesAndRestart() {

//        val mActivityManager: ActivityManager =
//            getSystemService(ACTIVITY_SERVICE) as ActivityManager
//
//        val myPackage = applicationContext.packageName
//
//        mActivityManager.restartPackage(myPackage)


        serviceIntent = Intent()
        serviceIntent.component = ComponentName(
            "com.sonde.edgeapp",
            "com.sonde.mentalfitness.domain.RecordingService"
        )
        stopService(serviceIntent)

        val serviceIntentSoundTrigger = Intent()
        serviceIntentSoundTrigger.component = ComponentName(
            "com.sonde.edgeapp",
            "com.sonde.edgeapp.SoundTriggerTestService"
        )
        stopService(serviceIntentSoundTrigger)

        sharedPreferenceHelper.setRecordingServiceRunning(false)

        showSnackbar(this.container,"Demo has been restarted.")
    }

    fun startSoundTriggerService() {
        // Make sure that the service is started, so even if our activity goes down, we'll still
        // have a request for it to run.
        Log.d(TAG, "SoundTrigger isServiceRunning2 ==>${isServiceRunning2()}")
        if (!isServiceRunning2()) {
            sharedPreferenceHelper.setDemoType(Constants.IC_LEVEL_DEMO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(baseContext, SoundTriggerTestService::class.java))
            } else {
                startService(Intent(baseContext, SoundTriggerTestService::class.java))
            }

            // Bind to SoundTriggerTestService.
            val intent = Intent(this, SoundTriggerTestService::class.java)
            bindService(intent, mConnection, BIND_AUTO_CREATE)
            Handler().postDelayed({
                loadAndStart()
            }, 3000)
        } else {
            Toast.makeText(this, "Sound Trigger Service already running..", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun loadAndStart() {
        if (mService == null) {
            Log.e(
                TAG,
                "Could not load sound model: not bound to SoundTriggerTestService"
            )
        } else {
            mService!!.loadModel(mSelectedModelUuid)
            startSoundTrigger()
        }
    }

    fun startSoundTrigger() {
        mService!!.startRecognition(mSelectedModelUuid)
    }

    fun startServiceOfAnotherApp() {
//        serviceIntent = Intent()
////        serviceIntent.component = ComponentName(
////            "com.sonde.soundtrigger",
////            "com.sonde.soundtrigger.SoundTriggerTestService"
////        )
//        serviceIntent.component = ComponentName(
//            "com.gslab.edgetestapp",
//            "com.gslab.edgetestapp.MessengerService"
//        )
//        Log.d("MainActivity", "isMyServiceRunning==>${isServiceRunning2()}")
//        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)
//        startService(serviceIntent)

        val intent = Intent()
        intent.action = "com.sonde.edgeapp"
        intent.putExtra("data", "Service start by Sonde Vocal Biomarker")
        val componentName =
            ComponentName("com.sonde.edgeapp", "com.sonde.mentalfitness.IPCBroadcastReceiver")
        intent.component = componentName
        this.applicationContext.sendBroadcast(intent)

    }

    fun stopServiceOfAnotherApp() {
        Log.d("MainActivity", "isMyServiceRunning==>${isServiceRunning2()}")
        serviceIntent = Intent()
        serviceIntent.component = ComponentName(
            "com.sonde.soundtrigger",
            "com.sonde.soundtrigger.SoundTriggerTestService"
        )
//        serviceIntent.component = ComponentName(
//            "com.gslab.edgetestapp",
//            "com.gslab.edgetestapp.MessengerService"
//        )
        stopService(serviceIntent)
    }

    fun callIPC() {

        serviceIntent = Intent()
        serviceIntent.component = ComponentName(
            "com.sonde.soundtrigger",
            "com.sonde.soundtrigger.SoundTriggerTestService"
        )
//        serviceIntent.component = ComponentName(
//            "com.gslab.edgetestapp",
//            "com.gslab.edgetestapp.MessengerService"
//        )
        Log.d("MainActivity", "isMyServiceRunning==>${isServiceRunning2()}")
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)
        if (bound) {
            val msg: Message = Message.obtain(null, GET_RANDOM, 0, 0)
            try {
                mRequestMessenger?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        } else {
            Snackbar.make(
                container,
                "Service Unbound. Can't get nnumber",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStop() {
        super.onStop()

        // Unbind from the service.
        if (mService != null) {
            mService!!.setUserActivity(null)
            unbindService(mConnection)
        }
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            Log.d("MainActivity", "Service Name==>${service.service.className}")
            if ("com.gslab.edgetestapp.MessengerService" == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            Log.d("MainActivity", "Service Name==>${service.service.className}")
            if (SoundTriggerTestService::class.java.simpleName == service.service.className) {
                return true
            }
        }
        return false
    }

    @Suppress("DEPRECATION") // Deprecated for third party Services.
    inline fun Context.isServiceRunning2() =
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Integer.MAX_VALUE)
//            .any { it.service.className == "com.sonde.soundtrigger.SoundTriggerTestService" }
            .any { it.service.className == SoundTriggerTestService::class.java.simpleName }

    private fun launchPassiveMode() {
        if (!sharedPreferenceHelper.isVoiceEnrollmentDone()) {
            startActivityForResult(
                Intent(this, EnrollmentActivity::class.java).putExtra(
                    DEMO_TYPE,
                    REQUEST_CODE_PASSIVE_MODE
                ),
                REQUEST_CODE_PASSIVE_MODE
            )
        } else {
            startSoundTriggerService() //TODO: Uncomment this while production and comment below 3 lines of code
//            val timeDiff = 0L
//            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyRecordingWorker>()
//                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build()
//            WorkManager.getInstance(this).enqueue(dailyWorkRequest)
        }
    }

    private fun launchPassiveModeForeground() {
        if (!sharedPreferenceHelper.isVoiceEnrollmentDone()) {
            startActivityForResult(
                Intent(this, EnrollmentActivity::class.java).putExtra(
                    DEMO_TYPE,
                    REQUEST_CODE_PASSIVE_MODE_FOREGROUND
                ),
                REQUEST_CODE_PASSIVE_MODE_FOREGROUND
            )
        } else {

            startActivity(Intent(this, PassiveForegroundRecordingActivity::class.java))
        }
    }

    private fun launchMentalFitness() {
        storeMentalFitnessConfigData()
        startActivity(Intent(this, CheckInHostActivity::class.java))
    }

    private fun launchVoipCall(userName: String) {
        if (!sharedPreferenceHelper.isVoiceEnrollmentDone()) {
            startActivity(Intent(this, EnrollmentActivity::class.java))
        } else {
            startActivity(VoiceActivity.newInstance(this, userName))
        }
    }

    private fun launchMobileCall() {
        if (!sharedPreferenceHelper.isVoiceEnrollmentDone()) {
            startActivity(Intent(this, EnrollmentActivity::class.java))
        } else {

        }
    }

    private fun launchCheckInScreen() {
        storeConfigData();
        startActivity(Intent(this, CheckInHostActivity::class.java))
    }

    private fun launchFreeSpeechFlow() {
        storeFreeSpeechConfigData();
        startActivity(Intent(this, CheckInHostActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PASSIVE_MODE -> {
                    launchPassiveMode()
                }
                REQUEST_CODE_VOIP_CALL -> {
                    viewModel.onPassiveCallButtonClick()
                }
                REQUEST_CODE_MOBILE_CALL -> {
                    launchMobileCall()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone()
        }
        val intent = Intent(this, SoundTriggerTestService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }


    private fun checkPermissionForMicrophone(): Boolean {
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val resultPhone =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        return resultMic == PackageManager.PERMISSION_GRANTED && resultPhone == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Snackbar.make(
                container,
                "Microphone permissions needed. Please allow in your application settings.",
                Snackbar.LENGTH_LONG
            ).show()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_PHONE_STATE
            )
        ) {
            Snackbar.make(
                container,
                "Read phone state permissions needed. Please allow in your application settings.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE),
                MIC_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        /*
         * Check if microphone permissions is granted
         */
        if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                    container,
                    "Microphone permissions needed. Please allow in your application settings.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                    container,
                    "Read phone state permissions needed. Please allow in your application settings.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun storeConfigData() {
        sharedPreferenceHelper.setCheckInConfigData(
            "{\n" +
                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
                    "    \"passages\": [\n" +
                    "        {\n" +
                    "            \"prompt\": \"Say Ahh\",\n" +
                    "            \"timerLength\": 6\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"healthchecks\": [\n" +
                    "        {\n" +
                    "            \"name\": \"Mental fitness\",\n" +
                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Concentration\",\n" +
                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Engagement\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Optimism\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"questionnaire\": {\n" +
                    "        \"id\": \"qnr-5e61f4715\",\n" +
                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
                    "        \"language\": \"en\",\n" +
                    "        \"questions\": [\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
                    "                \"text\": \"How is your breathing today?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Excellent\",\n" +
                    "                        \"score\": 0\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Good Enough\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Some Difficulty\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Challenging\",\n" +
                    "                        \"score\": 3\n" +
                    "                    }" +
                    "                ]\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            } ,\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
                    "    },\n" +
                    "    \"mainHealthcheckIndex\": 0\n" +
                    "}"
        )


//        sharedPreferenceHelper.setCheckInConfigData(
//            "{\n" +
//                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
//                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
//                    "    \"passages\": [\n" +
//                    "        {\n" +
//                    "            \"prompt\": \"Say Ahh\",\n" +
//                    "            \"timerLength\": 6\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"healthchecks\": [\n" +
//                    "        {\n" +
//                    "            \"name\": \"Mental fitness\",\n" +
//                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Concentration\",\n" +
//                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Engagement\",\n" +
//                    "            \"sondeplatformName\": \"nasality\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Optimism\",\n" +
//                    "            \"sondeplatformName\": \"nasality\"\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"questionnaire\": {\n" +
//                    "        \"id\": \"qnr-5e61f4715\",\n" +
//                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
//                    "        \"language\": \"en\",\n" +
//                    "        \"questions\": [\n" +
//                    "            {\n" +
//                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
//                    "                \"text\": \"How are you feeling today?\",\n" +
//                    "                \"isSkippable\": true,\n" +
//                    "                \"options\": [\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Sad\",\n" +
//                    "                        \"score\": 0\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Stressed\",\n" +
//                    "                        \"score\": 1\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Unsure\",\n" +
//                    "                        \"score\": 2\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Relaxed\",\n" +
//                    "                        \"score\": 3\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Happy\",\n" +
//                    "                        \"score\": 4\n" +
//                    "                    }\n" +
//                    "                ]\n" +
//                    "            },\n" +
//                    "            {\n" +
//                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
//                    "                \"text\": \"Any specific reasons you would like to share?\",\n" +
//                    "                \"isSkippable\": true,\n" +
//                    "                \"options\": [\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Work\",\n" +
//                    "                        \"score\": 6\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"School\",\n" +
//                    "                        \"score\": 5\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Family\",\n" +
//                    "                        \"score\": 4\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Health\",\n" +
//                    "                        \"score\": 3\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Money\",\n" +
//                    "                        \"score\": 2\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Friends\",\n" +
//                    "                        \"score\": 1\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Relationship\",\n" +
//                    "                        \"score\": 0\n" +
//                    "                    }\n" +
//                    "                ]\n" +
//                    "            }\n" +
//                    "        ],\n" +
//                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
//                    "    },\n" +
//                    "    \"mainHealthcheckIndex\": 0\n" +
//                    "}"
//        )
    }

    private fun storeFreeSpeechConfigData() {
        val sharedPreferenceHelper =
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        sharedPreferenceHelper.setCheckInConfigData(
            "{\n" +
                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
                    "    \"passages\": [\n" +
                    "        {\n" +
                    "            \"prompt\": \"Tell us about how your week has been so far?\",\n" +
                    "            \"timerLength\": 30\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"healthchecks\": [\n" +
                    "        {\n" +
                    "            \"name\": \"Mental fitness\",\n" +
                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Concentration\",\n" +
                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Engagement\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Optimism\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"questionnaire\": {\n" +
                    "        \"id\": \"qnr-5e61f4715\",\n" +
                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
                    "        \"language\": \"en\",\n" +
                    "        \"questions\": [\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
                    "                \"text\": \"How is your breathing today?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Excellent\",\n" +
                    "                        \"score\": 0\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Good Enough\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Some Difficulty\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Challenging\",\n" +
                    "                        \"score\": 3\n" +
                    "                    }" +
                    "                ]\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            } ,\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
                    "    },\n" +
                    "    \"mainHealthcheckIndex\": 0\n" +
                    "}"
        )


//        sharedPreferenceHelper.setCheckInConfigData(
//            "{\n" +
//                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
//                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
//                    "    \"passages\": [\n" +
//                    "        {\n" +
//                    "            \"prompt\": \"Say Ahh\",\n" +
//                    "            \"timerLength\": 6\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"healthchecks\": [\n" +
//                    "        {\n" +
//                    "            \"name\": \"Mental fitness\",\n" +
//                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Concentration\",\n" +
//                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Engagement\",\n" +
//                    "            \"sondeplatformName\": \"nasality\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Optimism\",\n" +
//                    "            \"sondeplatformName\": \"nasality\"\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"questionnaire\": {\n" +
//                    "        \"id\": \"qnr-5e61f4715\",\n" +
//                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
//                    "        \"language\": \"en\",\n" +
//                    "        \"questions\": [\n" +
//                    "            {\n" +
//                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
//                    "                \"text\": \"How are you feeling today?\",\n" +
//                    "                \"isSkippable\": true,\n" +
//                    "                \"options\": [\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Sad\",\n" +
//                    "                        \"score\": 0\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Stressed\",\n" +
//                    "                        \"score\": 1\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Unsure\",\n" +
//                    "                        \"score\": 2\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Relaxed\",\n" +
//                    "                        \"score\": 3\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Happy\",\n" +
//                    "                        \"score\": 4\n" +
//                    "                    }\n" +
//                    "                ]\n" +
//                    "            },\n" +
//                    "            {\n" +
//                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
//                    "                \"text\": \"Any specific reasons you would like to share?\",\n" +
//                    "                \"isSkippable\": true,\n" +
//                    "                \"options\": [\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Work\",\n" +
//                    "                        \"score\": 6\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"School\",\n" +
//                    "                        \"score\": 5\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Family\",\n" +
//                    "                        \"score\": 4\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Health\",\n" +
//                    "                        \"score\": 3\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Money\",\n" +
//                    "                        \"score\": 2\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Friends\",\n" +
//                    "                        \"score\": 1\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Relationship\",\n" +
//                    "                        \"score\": 0\n" +
//                    "                    }\n" +
//                    "                ]\n" +
//                    "            }\n" +
//                    "        ],\n" +
//                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
//                    "    },\n" +
//                    "    \"mainHealthcheckIndex\": 0\n" +
//                    "}"
//        )
    }

    private fun storeMentalFitnessConfigData() {
        val sharedPreferenceHelper =
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        sharedPreferenceHelper.setCheckInConfigData(
            "{\n" +
                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
                    "    \"passages\": [\n" +
                    "        {\n" +
                    "            \"prompt\": \"Describe your perfect day.\",\n" +
                    "            \"timerLength\": 30\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"healthchecks\": [\n" +
                    "        {\n" +
                    "            \"name\": \"Mental fitness\",\n" +
                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Concentration\",\n" +
                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Engagement\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Optimism\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"questionnaire\": {\n" +
                    "        \"id\": \"qnr-5e61f4715\",\n" +
                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
                    "        \"language\": \"en\",\n" +
                    "        \"questions\": [\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
                    "                \"text\": \"How is your breathing today?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Excellent\",\n" +
                    "                        \"score\": 0\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Good Enough\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Some Difficulty\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Challenging\",\n" +
                    "                        \"score\": 3\n" +
                    "                    }" +
                    "                ]\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            } ,\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
                    "    },\n" +
                    "    \"mainHealthcheckIndex\": 0\n" +
                    "}"
        )
    }

    override fun addModel(modelUuid: UUID?, name: String?) {
        Log.d(TAG, "UUID Print==>$modelUuid, name==>$name")
        mSelectedModelUuid = modelUuid
    }

    override fun setModelState(modelUuid: UUID?, state: String?) {
        Log.d(TAG, "UUID Print==>$modelUuid, state==>$state")
        mSelectedModelUuid = modelUuid
    }

    override fun showMessage(msg: String?, showToast: Boolean) {
        Log.d(TAG, "Message Print==>$msg, showToast==>$showToast")
    }

    override fun handleDetection(p0: UUID?) {
        Toast.makeText(this, "Voice Detected==>", Toast.LENGTH_SHORT).show()
    }
}