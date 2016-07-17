package com.darkrockstudios.apps.adventure;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Adam on 11/16/2015.
 */
public class WallpaperTask implements Runnable
{
	private static final String TAG        = WallpaperTask.class.getSimpleName();
	public static final  String URL_TODAY  = "http://wethinkadventure.rocks/todayswallpaper";
	public static final  String URL_RANDOM = "http://wethinkadventure.rocks/random";

	private final Context m_context;
	private final String  m_url;
	private       Bitmap  m_bitmap;
	private       Photo   m_photo;

	public WallpaperTask( final Context context, final String url )
	{
		m_context = context;
		m_url = url;
	}

	public Photo getPhoto()
	{
		return m_photo;
	}

	public Bitmap getBitmap()
	{
		return m_bitmap;
	}

	@Override
	public void run()
	{
		try
		{
			Gson gson = new Gson();

			InputStream jsonInput = new java.net.URL( m_url ).openStream();
			String json = IOUtils.toString( jsonInput, "UTF-8" );
			m_photo = gson.fromJson( json, Photo.class );

			Log.d( TAG, "Image URL: " + m_photo.image );
			InputStream input = new java.net.URL( m_photo.image ).openStream();
			m_bitmap = BitmapFactory.decodeStream( input );

			write( m_bitmap );

			setHomeScreenWallpaper( m_bitmap );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private void write( @Nullable Bitmap bitmap )
	{
		if( bitmap != null )
		{
			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream( WallpaperUtils.getCurrentWallpaperFile( m_context ) );
				// PNG is a lossless format, the compression factor (100) is ignored
				bitmap.compress( Bitmap.CompressFormat.PNG, 100, out );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if( out != null )
					{
						out.close();
					}
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void setHomeScreenWallpaper( Bitmap bitmap )
	{
		DisplayMetrics displayMetrics = m_context.getResources().getDisplayMetrics();

		final int maxWidth = displayMetrics.heightPixels * 2;

		final Bounds wallpaperSize = new Bounds( maxWidth, displayMetrics.heightPixels );

		final Bounds imageSize = new Bounds( bitmap.getWidth(), bitmap.getHeight() );
		final Bounds scaledSize = getScaledBounds( imageSize, wallpaperSize );

		Log.d( TAG, "Image Size: " + imageSize.m_width + " x " + imageSize.m_height );
		Log.d( TAG, "Wallpaper Size: " + wallpaperSize.m_width + " x " + wallpaperSize.m_height );
		Log.d( TAG, "Scaled Size: " + scaledSize.m_width + " x " + scaledSize.m_height );

		Bitmap scaledBitmap = Bitmap.createScaledBitmap( bitmap, (int) scaledSize.m_width, (int) scaledSize.m_height, false );

		WallpaperManager wallpaperManager = WallpaperManager.getInstance( m_context );
		try
		{
			wallpaperManager.setBitmap( scaledBitmap );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	private Bounds getScaledBounds( @NonNull final Bounds imageSize, @NonNull final Bounds boundary )
	{
		double widthRatio = boundary.m_width / imageSize.m_width;
		double heightRatio = boundary.m_height / imageSize.m_height;
		double ratio = Math.min( widthRatio, heightRatio );
		Log.d( TAG, "ratio: " + ratio );
		return new Bounds( (int) (imageSize.m_width * ratio), (int) (imageSize.m_height * ratio) );
	}
}
