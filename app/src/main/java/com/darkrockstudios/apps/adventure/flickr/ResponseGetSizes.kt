package com.darkrockstudios.apps.adventure.flickr

/**
 * Created by Adam on 11/11/2015.
 */
data class ResponseGetSizes(var canblog: Int = 0,
                            var canprint: Int = 0,
                            var candownload: Int = 0,
                            var size: Array<Size>? = null)