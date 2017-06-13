package com.darkrockstudios.apps.adventure

import java.io.Serializable

/**
 * Created by Adam on 11/12/2015.
 */
data class Photo
(
		var title: String? = null,
		var description: String? = null,
		var date: String? = null,
		var location: String? = null,
		var image: String? = null,
		var url: String? = null,
		var id: String? = null
) : Serializable