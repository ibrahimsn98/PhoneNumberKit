package me.ibrahimsn.phonenumberkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import me.ibrahimsn.lib.PhoneNumberKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val phoneNumberKit = PhoneNumberKit(this)

        // To attach an editTextLayout
        phoneNumberKit.attachToInput(textField, 1)

        // Setup country code picker optionally
        phoneNumberKit.setupCountryPicker(this)

        // Provides example phone number for given country iso2 code
        val exampleNumber = phoneNumberKit.getExampleNumber("tr")

        // Parses raw text to phone number object
        val phoneNumber = phoneNumberKit.parsePhoneNumber("05066120000")

        // Converts phone number object to formatted phone number
        // Ex: +90 506 606 00 00
        val formattedNumber = phoneNumberKit.formatPhoneNumber(phoneNumber)

        // Provides country flag icon for given iso2 code
        val flag = phoneNumberKit.getFlagIcon("tr")

        // Provides country name, iso2 for given country code
        val country = phoneNumberKit.getCountry(90)
    }
}