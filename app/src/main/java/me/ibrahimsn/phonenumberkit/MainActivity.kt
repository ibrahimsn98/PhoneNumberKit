package me.ibrahimsn.phonenumberkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import me.ibrahimsn.lib.PhoneNumberKit
import me.ibrahimsn.phonenumberkit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phoneNumberKit = PhoneNumberKit.Builder(this)
            .build()

        // To attach an editTextLayout
        phoneNumberKit.attachToInput(binding.textField, "tr")

        // Setup country code picker optionally
        phoneNumberKit.setupCountryPicker(
            activity = this,
            searchEnabled = true
        )

        // Provides example phone number for given country iso2 code
        val exampleNumber = phoneNumberKit.getExampleNumber("tr")
        Log.d(TAG, "Example Number: $exampleNumber")

        // Parses raw phone number to phone object
        val parsedNumber = phoneNumberKit.parsePhoneNumber(
            number = "05066120000",
            defaultRegion = "us"
        )
        Log.d(TAG, "Parsed Number: $parsedNumber")

        // Converts raw phone number to international formatted phone number
        // Ex: +90 506 606 00 00
        val formattedNumber = phoneNumberKit.formatPhoneNumber(
            number = "05066120000",
            defaultRegion = "tr"
        )
        Log.d(TAG, "Formatted Number: $formattedNumber")
    }

    companion object {
        private const val TAG = "###"
    }
}
