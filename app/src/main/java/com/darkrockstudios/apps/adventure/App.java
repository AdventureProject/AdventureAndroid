package com.darkrockstudios.apps.adventure;

import android.app.Application;

/**
 * Created by Adam on 11/16/2015.
 */
public class App extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();

		WallpaperUtils.setupWallpaperJob( this );
	}
}
