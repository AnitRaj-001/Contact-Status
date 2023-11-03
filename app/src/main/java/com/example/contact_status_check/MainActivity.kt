package com.example.contact_status_check

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var statusEditText: EditText
    private lateinit var phoneStateListener: PhoneStateListener
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var sharedPreferences: SharedPreferences
    private var isStatusActive = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Contact Status"

        statusEditText = findViewById(R.id.etMessage)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Register a broadcast receiver to monitor phone state changes
        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        registerReceiver(callReceiver, filter)

        // Initialize phone state listener
        phoneStateListener = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                super.onCallStateChanged(state, phoneNumber)
                if (isStatusActive && state == TelephonyManager.CALL_STATE_RINGING) {
                    val status = sharedPreferences.getString("status_message", "")
                    if (!status.isNullOrBlank()) {
                        showToast("Incoming call: $status")
                    }
                }
            }
        }
       // telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        // Check if the status feature is active
        isStatusActive = sharedPreferences.getBoolean("status_active", false)
    }

    fun saveStatus(view: View) {
        val statusMessage = statusEditText.text.toString()
        with(sharedPreferences.edit()) {
            putString("status_message", statusMessage)
            apply()
        }
        showToast("Status message saved: $statusMessage")
    }

    fun toggleStatus(view: View) {
        isStatusActive = !isStatusActive
        with(sharedPreferences.edit()) {
            putBoolean("status_active", isStatusActive)
            apply()
        }
        val status = if (isStatusActive) "activated" else "deactivated"
        showToast("Status feature $status")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val callReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.intent.action.PHONE_STATE") {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                if (state == TelephonyManager.EXTRA_STATE_RINGING && isStatusActive) {
                    val status = sharedPreferences.getString("status_message", "")
                    if (!status.isNullOrBlank()) {
                        showToast("Incoming call: $status")
                    }
                }
            }
        }
    }
}
