package com.darkrockstudios.apps.adventure

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream

/**
 * Created by Adam on 11/16/2015.
 */
open class WallpaperTask(private val m_context: Context, private val m_url: String) : Runnable
{
	var bitmap: Bitmap? = null
		private set
	var photo: Photo? = null
		private set

	override fun run()
	{
		try
		{
			val gson = Gson()

			val jsonInput = java.net.URL(m_url).openStream()
			val json = IOUtils.toString(jsonInput, "UTF-8")
			photo = gson.fromJson(json, Photo::class.java)

			Log.d(TAG, "Image URL: " + photo!!.image!!)
			val input = java.net.URL(photo!!.image).openStream()
			bitmap = BitmapFactory.decodeStream(input)

			write(photo)
			write(bitmap)

			setHomeScreenWallpaper(bitmap!!)
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}

	}

	private fun write(photo: Photo?)
	{
		if (photo != null)
		{
			var out: FileOutputStream? = null
			try
			{
				val file = WallpaperUtils.getCurrentPhotoFile(m_context)
				if (file.exists())
				{
					file.delete()
				}
				file.createNewFile()
				out = FileOutputStream(file)
				val oos = ObjectOutputStream(out)
				oos.writeObject(photo)
			}
			catch (e: IOException)
			{
				e.printStackTrace()
			}
			finally
			{
				if (out != null)
				{
					try
					{
						out.close()
					}
					catch (e: IOException)
					{
						e.printStackTrace()
					}

				}
			}
		}
	}

	private fun write(bitmap: Bitmap?)
	{
		if (bitmap != null)
		{
			var out: FileOutputStream? = null
			try
			{
				out = FileOutputStream(WallpaperUtils.getCurrentWallpaperFile(m_context))
				// PNG is a lossless format, the compression factor (100) is ignored
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
			finally
			{
				try
				{
					if (out != null)
					{
						out.close()
					}
				}
				catch (e: IOException)
				{
					e.printStackTrace()
				}

			}
		}
	}

	private fun setHomeScreenWallpaper(bitmap: Bitmap)
	{
		val displayMetrics = m_context.resources.displayMetrics

		val maxWidth = displayMetrics.heightPixels * 2

		val wallpaperSize = Bounds(maxWidth.toDouble(), displayMetrics.heightPixels.toDouble())

		val imageSize = Bounds(bitmap.width.toDouble(), bitmap.height.toDouble())
		val (m_width, m_height) = getScaledBounds(imageSize, wallpaperSize)

		Log.d(TAG, "Image Size: " + imageSize.m_width + " x " + imageSize.m_height)
		Log.d(TAG, "Wallpaper Size: " + wallpaperSize.m_width + " x " + wallpaperSize.m_height)
		Log.d(TAG, "Scaled Size: $m_width x $m_height")

		val scaledBitmap = Bitmap.createScaledBitmap(bitmap, m_width.toInt(), m_height.toInt(), false)

		val wallpaperManager = WallpaperManager.getInstance(m_context)
		try
		{
			wallpaperManager.setBitmap(scaledBitmap)
		}
		catch (e: IOException)
		{
			e.printStackTrace()
		}

	}

	private fun getScaledBounds(imageSize: Bounds, boundary: Bounds): Bounds
	{
		val widthRatio = boundary.m_width / imageSize.m_width
		val heightRatio = boundary.m_height / imageSize.m_height
		val ratio = Math.min(widthRatio, heightRatio)
		Log.d(TAG, "ratio: " + ratio)
		return Bounds((imageSize.m_width * ratio).toInt().toDouble(), (imageSize.m_height * ratio).toInt().toDouble())
	}

	companion object
	{
		private val TAG = WallpaperTask::class.java.simpleName
		val URL_TODAY = "http://wethinkadventure.rocks/todayswallpaper"
		val URL_RANDOM = "http://wethinkadventure.rocks/random"
	}
}
