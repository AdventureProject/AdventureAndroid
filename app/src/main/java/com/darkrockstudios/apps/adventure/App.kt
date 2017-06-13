package com.darkrockstudios.apps.adventure

import android.app.Application
import android.app.job.JobScheduler
import android.content.Context

/**
 * Created by Adam on 11/16/2015.
 */
class App : Application()
{
	override fun onCreate()
	{
		super.onCreate()

		val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		if (jobScheduler.allPendingJobs.size == 0)
		{
			WallpaperUtils.setupWallpaperJob(this)
		}
	}
}
