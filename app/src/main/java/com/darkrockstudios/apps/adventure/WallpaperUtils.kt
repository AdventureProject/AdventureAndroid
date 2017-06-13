package com.darkrockstudios.apps.adventure

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log

import org.joda.time.DateTime
import org.joda.time.Interval

import java.io.File

/**
 * Created by Adam on 11/16/2015.
 */
object WallpaperUtils
{
	private val TAG = WallpaperUtils::class.java.simpleName

	val PHOTO_FILE_NAME = "current_photo"
	val WALLPAPER_FILE_NAME = "current_wallpaper"
	val WALLPAPER_UPDATE_JOB_ID = 1

	fun getCurrentWallpaperFile(context: Context): File
	{
		val filesDir = context.filesDir
		return File(filesDir, WallpaperUtils.WALLPAPER_FILE_NAME)
	}

	fun getCurrentPhotoFile(context: Context): File
	{
		val filesDir = context.filesDir
		return File(filesDir, WallpaperUtils.PHOTO_FILE_NAME)
	}

	fun setupWallpaperJob(context: Context)
	{
		Log.d(TAG, "setupWallpaperJob")

		val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

		Log.d(TAG, "Scheduling job")
		val builder = JobInfo.Builder(WALLPAPER_UPDATE_JOB_ID,
		                              ComponentName(context.packageName,
		                                            WallpaperService::class.java.name))

		val now = DateTime.now()
		val startOfTomorrow = now.withTimeAtStartOfDay().plusDays(1)
		val oneAmTomorrow = startOfTomorrow.plusHours(1)
		val timeTillOneAm = Interval(now, oneAmTomorrow)
		val timeTillOneAmMills = timeTillOneAm.toDurationMillis()

		builder.setMinimumLatency(timeTillOneAmMills)
		builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
		builder.setPersisted(true)

		jobScheduler.schedule(builder.build())
	}

	fun stopWallpaperJob(context: Context)
	{
		val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		jobScheduler.cancel(WALLPAPER_UPDATE_JOB_ID)
	}
}
