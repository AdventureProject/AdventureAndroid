package com.darkrockstudios.apps.adventure;

import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity
		implements CompoundButton.OnCheckedChangeListener
{
	private CustomTabsServiceConnection m_connection = new CustomTabsServiceConnection()
	{
		@Override
		public void onCustomTabsServiceConnected( ComponentName name, CustomTabsClient client )
		{
			m_customTabsClient = client;
			m_customTabsClient.warmup( 0 );
		}

		@Override
		public void onServiceDisconnected( ComponentName name )
		{

		}
	};
	private CustomTabsClient m_customTabsClient;

	private static final String TAG = MainActivity.class.getSimpleName();

	@BindView(R.id.toolbar)
	Toolbar m_toolbar;

	@BindView(R.id.image)
	ImageView m_imageView;

	@BindView(R.id.map_zoomed_out)
	ImageView m_mapZoomedOutView;

	@BindView(R.id.map_zoomed_in)
	ImageView m_mapZoomedInView;

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

	@BindView(R.id.photo_info_button)
	TextView m_photoInfoButton;

	@BindView(R.id.photo_date)
	TextView m_photoDateView;

	private Unbinder m_viewUnbinder;

	private Photo m_currentPhoto;

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

		boolean ok = CustomTabsClient.bindCustomTabsService( this, "com.android.chrome", m_connection );
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

		if( m_customTabsClient != null )
		{
			m_customTabsClient = null;
			unbindService( m_connection );
		}

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

	@OnClick(R.id.photo_info_button)
	public void onPhotoInfo()
	{
		if( m_currentPhoto != null )
		{
			//startActivity( new Intent( Intent.ACTION_VIEW ).setData( Uri.parse( m_currentPhoto.url ) ) );
			CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
			builder.setShowTitle( true );
			builder.setToolbarColor( ContextCompat.getColor( this, R.color.colorPrimary ) );
			CustomTabsIntent customTabsIntent = builder.build();
			customTabsIntent.launchUrl( this, Uri.parse( m_currentPhoto.url ) );
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

		if( m_photoDateView != null )
		{
			m_photoDateView.setText( "" );
		}

		if( m_photoDescriptionView != null )
		{
			m_photoDescriptionView.setText( "" );
		}

		if( m_photoInfoButton != null )
		{
			m_photoInfoButton.setVisibility( View.GONE );
		}

		if( m_mapZoomedOutView != null )
		{
			m_mapZoomedOutView.setImageDrawable( null );
			m_mapZoomedOutView.setVisibility( View.GONE );
		}

		if( m_mapZoomedInView != null )
		{
			m_mapZoomedInView.setImageDrawable( null );
			m_mapZoomedInView.setVisibility( View.GONE );
		}
	}

	private void showImage( PhotoData data )
	{
		m_currentPhoto = data.m_photo;

		if( m_progressBar != null )
		{
			m_progressBar.setVisibility( View.GONE );
		}

		if( m_photoInfoButton != null )
		{
			m_photoInfoButton.setVisibility( View.VISIBLE );
		}

		if( m_imageView != null )
		{
			m_imageView.setImageBitmap( data.m_bitmap );
			m_imageView.setVisibility( View.VISIBLE );
		}

		String title = "";
		String description = "";
		String date = "";
		String location = "";
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

			if( !TextUtils.isEmpty( data.m_photo.date ) )
			{
				date = data.m_photo.date;
			}

			if( !TextUtils.isEmpty( data.m_photo.location ) )
			{
				location = data.m_photo.location;
			}
		}

		if( m_mapZoomedInView != null )
		{
			m_mapZoomedInView.setVisibility( View.VISIBLE );

			String url = PhotoMap.getZoomedInMapUrl( location, this );
			Picasso.with( this )
			       .load( url )
			       .into( m_mapZoomedInView, new ImageCallback() );
		}

		if( m_mapZoomedOutView != null )
		{
			m_mapZoomedOutView.setVisibility( View.VISIBLE );

			String url = PhotoMap.getZoomedOutMapUrl( location, this );
			Picasso.with( this )
			       .load( url )
			       .into( m_mapZoomedOutView, new ImageCallback() );
		}

		if( m_photoTitleView != null )
		{
			m_photoTitleView.setText( title );
		}

		if( m_photoDateView != null )
		{
			m_photoDateView.setText( date );
		}

		if( m_photoDescriptionView != null )
		{
			m_photoDescriptionView.setText( description );
		}
	}

	private class ImageCallback implements Callback
	{
		@Override
		public void onSuccess()
		{

		}

		@Override
		public void onError()
		{

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

			Photo photo;
			try
			{
				File photoFile = WallpaperUtils.getCurrentPhotoFile( MainActivity.this );
				FileInputStream fin = new FileInputStream( photoFile );
				ObjectInputStream ois = new ObjectInputStream( fin );
				photo = (Photo) ois.readObject();
			}
			catch( IOException | ClassNotFoundException e )
			{
				photo = null;
				e.printStackTrace();
			}

			if( photo != null )
			{
				return new PhotoData( photo, bitmap );
			}
			else
			{
				return null;
			}
		}

		@Override
		protected void onPostExecute( PhotoData result )
		{
			if( result == null )
			{
				new WallpaperActivityTask().execute( WallpaperTask.URL_TODAY );
			}
			else
			{
				showImage( result );
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
		final Photo  m_photo;
		final Bitmap m_bitmap;

		private PhotoData( Photo photo, Bitmap bitmap )
		{
			m_photo = photo;
			m_bitmap = bitmap;
		}
	}
}
