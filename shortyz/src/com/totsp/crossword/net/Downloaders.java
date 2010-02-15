package com.totsp.crossword.net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.totsp.crossword.PlayActivity;
import com.totsp.crossword.puz.IO;
import com.totsp.crossword.puz.Puzzle;

public class Downloaders {
	private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
	private ArrayList<Downloader> downloaders = new ArrayList<Downloader>();
	private NotificationManager notificationManager;
	private Context context;
	
	public Downloaders(SharedPreferences prefs,
			NotificationManager notificationManager,
			Context context) {
		this.notificationManager = notificationManager;
		this.context = context;
		
		if (prefs.getBoolean("downloadGlobe", true)) {
			downloaders.add(new BostonGlobeDownloader());
		}
		if(prefs.getBoolean("downloadThinks", true)){
			downloaders.add(new ThinksDownloader());
		}
		if(prefs.getBoolean("downloadChron", true)){
			downloaders.add(new ChronDownloader() );
		}
		if(prefs.getBoolean("downloadWsj", true)){
			downloaders.add(new WSJDownloader() );
		}
	}

	public void download(Date date){
		int i=1;
		String contentTitle = "Downloading Puzzles";
		
		Notification not = new Notification(android.R.drawable.stat_sys_download, contentTitle, System.currentTimeMillis());
		
		for(Downloader d : downloaders){
			String contentText = "Downloading from "+d.getName();
			Intent notificationIntent = new Intent(context, PlayActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			not.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			this.notificationManager.notify(0, not);
			File downloaded = d.download(date);
			if(downloaded != null){
				
				try{
					Puzzle puz = IO.load(downloaded);
					puz.setDate(date);
					puz.setSource(d.getName());
					IO.save(puz, downloaded);
					this.postDownloadedNotification(i,d.getName(), downloaded);
					
				} catch(IOException ioe){
					LOG.log(Level.WARNING, "Exception reading "+downloaded, ioe);
					downloaded.delete();
				}
				
			}
			i++;
		}
		this.notificationManager.cancel(0);
	}
	
	private void postDownloadedNotification(int i, String name, File puzFile){
		String contentTitle = "Downloaded "+name;
		Notification not = new Notification(android.R.drawable.stat_sys_download_done, contentTitle, System.currentTimeMillis());
		Intent notificationIntent = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), context, PlayActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		not.setLatestEventInfo(context, contentTitle, puzFile.getName(), contentIntent);
		this.notificationManager.notify(i, not);
	}
}
