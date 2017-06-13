package com.darkrockstudios.apps.adventure

object FlickrBaseEncoder
{
	private val alphabetString = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ"
	private val alphabet = alphabetString.toCharArray()
	private val base_count = alphabet.size

	fun encode(num: Long): String
	{
		var num = num
		var result = ""
		var div: Long
		var mod = 0

		while (num >= base_count)
		{
			div = num / base_count
			mod = (num - base_count * div.toLong()).toInt()
			result = alphabet[mod] + result
			num = div.toLong()
		}
		if (num > 0)
		{
			result = alphabet[num.toInt()] + result
		}
		return result
	}

	fun decode(link: String): Long
	{
		var link = link
		var result: Long = 0
		var multi: Long = 1
		while (link.length > 0)
		{
			val digit = link.substring(link.length - 1)
			result = result + multi * alphabetString.lastIndexOf(digit)
			multi = multi * base_count
			link = link.substring(0, link.length - 1)
		}
		return result
	}
}