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
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;

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
				Snackbar.make( view, "Fetching new Wallpaper", Snackbar.LENGTH_LONG ).show();
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
			Snackbar.make( m_scheduledCheckbox, "Starting Wallpaper Job", Snackbar.LENGTH_LONG ).show();
		}
		else
		{
			WallpaperUtils.stopWallpaperJob( MainActivity.this );
			Snackbar.make( m_scheduledCheckbox, "Stopping Wallpaper Job", Snackbar.LENGTH_LONG ).show();
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
	}

	private void showImage( Bitmap image )
	{
		if( m_progressBar != null )
		{
			m_progressBar.setVisibility( View.GONE );
		}

		if( m_imageView != null )
		{
			m_imageView.setImageBitmap( image );
			m_imageView.setVisibility( View.VISIBLE );
		}
	}

	private class LoadImageForPreview extends AsyncTask<Void, Void, Bitmap>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			showLoading();
		}

		@Override
		protected Bitmap doInBackground( Void... params )
		{
			File image = WallpaperUtils.getCurrentWallpaperFile( MainActivity.this );
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			Bitmap bitmap = BitmapFactory.decodeFile( image.getAbsolutePath(), bmOptions );
			if( bitmap != null )
			{
				bitmap = Bitmap.createScaledBitmap( bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false );
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute( Bitmap result )
		{
			showImage( result );

			if( result == null )
			{
				new WallpaperActivityTask().execute( WallpaperTask.URL_TODAY );
			}
		}
	}

	private class WallpaperActivityTask extends AsyncTask<String, Void, Bitmap>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			showLoading();
		}

		@Override
		protected Bitmap doInBackground( String... params )
		{
			final WallpaperTask task = new WallpaperTask( MainActivity.this, params[ 0 ] );
			task.run();
			final Bitmap bitmap = task.getBitmap();

			return bitmap;
		}

		@Override
		protected void onPostExecute( Bitmap bitmap )
		{
			super.onPostExecute( bitmap );

			showImage( bitmap );
		}
	}
}
