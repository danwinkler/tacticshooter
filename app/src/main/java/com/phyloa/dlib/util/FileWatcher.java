package com.phyloa.dlib.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;

public class FileWatcher
{
	Thread t;
	File f;
	
	ArrayList<FileWatcherListener> listeners = new ArrayList<FileWatcherListener>();
	
	public FileWatcher( File f )
	{
		this.f = f;
	}
	
	public void start()
	{
		t = new Thread( new FileWatcherThread( f ) );
		t.start();
	}
	
	public void addListener( FileWatcherListener fwl )
	{
		listeners.add( fwl );
	}
	
	public class FileWatcherThread implements Runnable
	{
		WatchService watcher;
		Path path;
		WatchKey regKey;
		boolean terminate = false;
		
		public FileWatcherThread( File f )
		{
			path = f.toPath().getParent();
			try
			{
				watcher = FileSystems.getDefault().newWatchService();
				regKey = path.register( watcher, StandardWatchEventKinds.ENTRY_MODIFY );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		
		public void run()
		{
			while( !terminate )
			{
				WatchKey key;
                try
				{
					key = watcher.take();
				}
				catch( InterruptedException e )
				{
					 break;
				}
                for( WatchEvent<?> event: key.pollEvents() ) 
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    System.out.println( kind );
                    
                    if( kind == StandardWatchEventKinds.OVERFLOW ) 
                    {
                        continue;
                    }
                    
					try
					{
						for( int i = 0; i < listeners.size(); i++ )
						{
							listeners.get( i ).changed( f );
						}
					} 
					catch( Exception ex )
					{
						ex.printStackTrace();
					}
                }
                
                boolean valid = key.reset();
                if( !valid )
                {
                    break;
                }
			}
		}
	}
	
	public interface FileWatcherListener
	{
		public void changed( File f );
	}
}
