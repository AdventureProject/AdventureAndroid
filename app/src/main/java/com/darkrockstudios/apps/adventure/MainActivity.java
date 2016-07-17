package com.darkrockstudios.apps.adventure;

import android.app.job.JobScheduler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity
		implements CompoundButton.OnCheckedChangeListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	@BindView(R.id.toolbar)
	Toolbar m_toolbar;

	@BindView(R.id.image)
	ImageView m_imageView;

	@BindView(R.id.scheduledCheckbox)
	CheckBox m_scheduledCheckbox;

	@BindView(R.id.fab)
	FloatingActionButton m_fab;

	@BindView(R.id.progressBar)
	ProgressBar m_progressBar;

	@BindView(R.id.photo_title)
	TextView m_photoTitleView;

	@BindView(R.id.photo_description)
	TextView m_photoDescriptionView;

	private Unbinder m_viewUnbinder;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		m_viewUnbinder = ButterKnife.bind( this );

		setSupportActionBar( m_toolbar );

		m_fab.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				new WallpaperActivityTask().execute( WallpaperTask.URL_RANDOM );
				Snackbar.make( view, R.string.toast_getting_wallpaper, Snackbar.LENGTH_LONG ).show();
			}
		} );

		new LoadImageForPreview().execute();
	}

	private void populateCheckbox()
	{
		JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
		boolean jobScheduled = (jobScheduler.getAllPendingJobs().size() > 0);
		m_scheduledCheckbox.setChecked( jobScheduled );
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		m_scheduledCheckbox.setOnCheckedChangeListener( null );
		populateCheckbox();
		m_scheduledCheckbox.setOnCheckedChangeListener( this );
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if( m_viewUnbinder != null )
		{
			m_viewUnbinder.unbind();
			m_viewUnbinder = null;
		}
	}

	@Override
	public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
	{
		if( isChecked )
		{
			WallpaperUtils.setupWallpaperJob( this );
			Snackbar.make( m_scheduledCheckbox, R.string.toast_wallpaper_service_start_scheduling, Snackbar.LENGTH_LONG )
			        .show();
		}
		else
		{
			WallpaperUtils.stopWallpaperJob( MainActivity.this );
			Snackbar.make( m_scheduledCheckbox, R.string.toast_wallpaper_service_stop_scheduling, Snackbar.LENGTH_LONG ).show();
		}
	}

	private void showLoading()
	{
		if( m_progressBar != null )
		{
			m_progressBar.setVisibility( View.VISIBLE );
		}

		if( m_imageView != null )
		{
			m_imageView.setVisibility( View.GONE );
			m_imageView.setImageBitmap( null );
		}

		if( m_photoTitleView != null )
		{
			m_photoTitleView.setText( "" );
		}

		if( m_photoDescriptionView != null )
		{
			m_photoDescriptionView.setText( "" );
		}
	}

	private void showImage( PhotoData data )
	{
		if( m_progressBar != null )
		{
			m_progressBar.setVisibility( View.GONE );
		}

		if( m_imageView != null )
		{
			m_imageView.setImageBitmap( data.m_bitmap );
			m_imageView.setVisibility( View.VISIBLE );
		}

		String title = "";
		String description = "";
		if( data.m_photo != null )
		{
			if( !TextUtils.isEmpty( data.m_photo.title ) )
			{
				title = data.m_photo.title;
			}

			if( !TextUtils.isEmpty( data.m_photo.description ) )
			{
				description = data.m_photo.description;
			}
		}

		if( m_photoTitleView != null )
		{
			m_photoTitleView.setText( title );
		}

		if( m_photoDescriptionView != null )
		{
			m_photoDescriptionView.setText( description );
		}
	}

	private class LoadImageForPreview extends AsyncTask<Void, Void, PhotoData>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			showLoading();
		}

		@Override
		protected PhotoData doInBackground( Void... params )
		{
			File imageFile = WallpaperUtils.getCurrentWallpaperFile( MainActivity.this );
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			Bitmap bitmap = BitmapFactory.decodeFile( imageFile.getAbsolutePath(), bmOptions );
			if( bitmap != null )
			{
				bitmap = Bitmap.createScaledBitmap( bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false );
			}

			Photo photo = null;
			try
			{
				File photoFile = WallpaperUtils.getCurrentPhotoFile( MainActivity.this );
				FileInputStream fin = new FileInputStream( photoFile );
				ObjectInputStream ois = new ObjectInputStream( fin );
				photo = (Photo) ois.readObject();
			}
			catch( IOException | ClassNotFoundException e )
			{
				e.printStackTrace();
			}


			return new PhotoData( photo, bitmap );
		}

		@Override
		protected void onPostExecute( PhotoData result )
		{
			showImage( result );

			if( result == null )
			{
				new WallpaperActivityTask().execute( WallpaperTask.URL_TODAY );
			}
		}
	}

	private class WallpaperActivityTask extends AsyncTask<String, Void, PhotoData>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			showLoading();
		}

		@Override
		protected PhotoData doInBackground( String... params )
		{
			final WallpaperTask task = new WallpaperTask( MainActivity.this, params[ 0 ] );
			task.run();

			return new PhotoData( task.getPhoto(), task.getBitmap() );
		}

		@Override
		protected void onPostExecute( PhotoData data )
		{
			super.onPostExecute( data );

			showImage( data );
		}
	}

	private static class PhotoData
	{
		public final Photo  m_photo;
		public final Bitmap m_bitmap;

		private PhotoData( Photo photo, Bitmap bitmap )
		{
			m_photo = photo;
			m_bitmap = bitmap;
		}
	}
}
