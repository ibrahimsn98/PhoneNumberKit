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

```kotlin
val phoneNumberKit = PhoneNumberKit.Builder(this)
    .setIconEnabled(true)
    .admitCountries(listOf("tr", "ca", "de")) // List only those county formats
    .excludeCountries(listOf("tr", "ca")) // Exclude those county formats
    .build()

phoneNumberKit.attachToInput(textField, "tr")
// OR
phoneNumberKit.attachToInput(textField, 1)
```

To setup with country code selection bottom sheet

```kotlin
phoneNumberKit.setupCountryPicker(this) // Requires activity context
```

To get an example phone number for given **iso2 code**

```kotlin
val exampleNumber = phoneNumberKit.getExampleNumber("tr")
```

To parse raw text to phone number and receive country code, national number

```kotlin
val parsedNumber = phoneNumberKit.parsePhoneNumber(
    number = "1266120000",
    defaultRegion = "us"
)

parsedNumber?.nationalNumber
parsedNumber?.countryCode
parsedNumber?.numberOfLeadingZeros
```

To convert raw text to formatted phone number string

```kotlin
val formattedNumber = phoneNumberKit.formatPhoneNumber(
    number = "1266120000",
    defaultRegion = "us"
)
```

To receive a country **flag icon** for given iso2 code

```kotlin
val flag = phoneNumberKit.getFlagIcon("ca")
```

## Usage with Custom Item Layout

Add your custom item layout resource as a parameter

```kotlin
phoneNumberKit.setupCountryPicker(this, R.layout.my_item_layout, searchEnabled = true)
```

You need to use below view ids in your layout file

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:paddingVertical="12dp"
    android:paddingHorizontal="18dp"
    android:clickable="true"
    android:focusable="true"
    android:background="?android:attr/selectableItemBackground">

    <ImageView
        android:id="@+id/imageViewFlag"
        android:layout_width="22dp"
        android:layout_height="22dp" />

    <TextView
        android:id="@+id/textViewName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"
        android:singleLine="true"
        android:maxLines="1"
        android:ellipsize="end"
        android:textSize="16sp"
        android:textColor="#232425" />

    <TextView
        android:id="@+id/textViewCode"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:textColor="#838383" />

</LinearLayout>
```


## Demo
<table>
	<tr>
		<th>Country Code Picker</th>
		<th>Format Example</th>
		<th>Format Example</th>
 	</tr>
 	<tr>
  		<td><img src="https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/art/ss1.jpg" width="250" /></td>
   		<td><img src="https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/art/ss3.jpg" width="250" /></td>
		<td><img src="https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/art/ss2.jpg" width="250" /></td>
 	</tr>
</table>

## Installation

> Follow me on Twitter [@ibrahimsn98](https://twitter.com/ibrahimsn98)

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
    implementation 'com.github.ibrahimsn98:PhoneNumberKit:2.0.0'
}
```

## Checklist
- [x] Search for country codes
- [x] Custom list item layout support
- [x] Better performance with coroutines
- [ ] Phone number validation indicator
- [x] Dark theme
- [ ] Tests

## Conception
- This library is based on Google's lilPhoneNumber library (https://github.com/google/libphonenumber)
- Inspired from PhoneNumberKit Swift library by [marmelloy](https://github.com/marmelroy) (https://github.com/marmelroy/PhoneNumberKit)
- Flag images from [region-flags](https://github.com/behdad/region-flags)

## License
PhoneNumberKit is available under the Apache license. See the [LICENSE](https://github.com/ibrahimsn98/PhoneNumberKit/blob/master/LICENSE) file for more info.




