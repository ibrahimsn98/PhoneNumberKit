# PhoneNumberKit
Android **Kotlin** library to parse and format international phone numbers. Based on Google's libphonenumber library.

[![](https://jitpack.io/v/ibrahimsn98/PhoneNumberKit.svg)](https://jitpack.io/#ibrahimsn98/PhoneNumberKit)


## Features

| |Features |
--------------------------|------------------------------------------------------------
:phone: | Validate, normalize and extract the elements of any phone number string.
:dart: | Convert raw phone number to formatted phone number string.
:mag: | Automatically detects country flag of the phone number. 
:bookmark: | Country code selection bottom sheet.
:pushpin: | Convert country codes to country names and vice versa.
:tr: | Get country flag icon for given iso2 code.


## Usage

Create a phoneNumberKit instance and attach it to an editTextLayout. That's all you have to do.
```
val phoneNumberKit = PhoneNumberKit(this) // Requires context
```
To setup with country code selection bottom sheet
```
phoneNumberKit.setupCountryPicker(this) // Requires activity
```
To get an example phone number for given **iso2 code**
```
val exampleNumber = phoneNumberKit.getExampleNumber("tr")
```
To parse raw text to phone number and receive country code, national number
```
val phoneNumber = phoneNumberKit.parsePhoneNumber("+905066120000")
```
To convert phone number object into formatted phone number string
```
val formattedNumber = phoneNumberKit.formatPhoneNumber(phoneNumber)
```
To receive a country **flag icon** for given iso2 code
```
val flag = phoneNumberKit.getFlagIcon("tr")
```
To receive country name or iso2 code from given **country code**
```
val country = phoneNumberKit.getCountry(90)
```

## Demo
<table>
	<tr>
		<th>Country Code Picker</th>
		<th>Format Example</th>
		<th>Format Example</th>
 	</tr>
 	<tr>
  		<td><img src="https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/art/ss3.jpg" width="250" /></td>
   		<td><img src="https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/art/ss1.jpg" width="250" /></td>
		<td><img src="https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/art/ss2.jpg" width="250" /></td>
 	</tr>
</table>

## Installation
Step 1. Add the JitPack repository to your build file
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
Step 2. Add the dependency
```
dependencies {
    implementation 'com.github.ibrahimsn98:PhoneNumberKit:1.0'
}
```

## Checklist
- [ ] Search for country codes
- [ ] Dark theme for country code selector
- [ ] Phone number validation indicator
- [ ] Better performance with coroutines

## Conception
This library is based on Google's lilPhoneNumber library (https://github.com/google/libphonenumber).

Inspired from PhoneNumberKit Swift library by [marmelloy](https://github.com/marmelroy) (https://github.com/marmelroy/PhoneNumberKit).

## License
PhoneNumberKit is available under the Apache license. See the [LICENSE](https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/LICENSE) file for more info.




