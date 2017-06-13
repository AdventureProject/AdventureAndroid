package com.darkrockstudios.apps.adventure

import android.content.Context

/**
 * Created by adamw on 2/21/2017.
 */

object PhotoMap
{
	fun getZoomedInMapUrl(location: String, context: Context): String
	{
		val baseUrl = "http://maps.googleapis.com/maps/api/staticmap?center=%2\$s&zoom=15&scale=1&size=400x480&maptype=terrain&key=%1\$s&format=png&visual_refresh=true&markers=size:mid%%7Ccolor:0xff0000%%7Clabel:%%7C%2\$s"

		val API_KEY = context.getString(R.string.GOOGLE_MAPS_API_KEY)
		return String.format(baseUrl, API_KEY, location)
	}

	fun getZoomedOutMapUrl(location: String, context: Context): String
	{
		val baseUrl = "http://maps.googleapis.com/maps/api/staticmap?center=%2\$s&zoom=6&scale=1&size=400x480&maptype=terrain&key=%1\$s&format=png&visual_refresh=true&markers=size:mid%%7Ccolor:0xff0000%%7Clabel:%%7C%2\$s"
		val API_KEY = context.getString(R.string.GOOGLE_MAPS_API_KEY)
		return String.format(baseUrl, API_KEY, location)
	}
}
