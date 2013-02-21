package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Level;
import tacticshooter.StaticFiles;
import tacticshooter.UserInfo;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DFile;

public class LoginScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DText usernameText;
	DText passwordText;
	DText errorText;
	DTextBox username;
	DTextBox password;
	DCheckBox remember;
	DButton back;
	DButton okay;
	
	GdxRenderer r;
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new GdxEventMapper( gc.getInput() ) );
			usernameText = new DText( "Username:", gc.getWidth()/2 - 150, gc.getHeight()/2-100 );
			usernameText.setCentered( true );
			passwordText = new DText( "Password:", gc.getWidth()/2 - 150, gc.getHeight()/2 );
			passwordText.setCentered( true );
			errorText = new DText( "", gc.getWidth()/2, gc.getHeight()/2 - 200 );
			errorText.setCentered( true );
			username = new DTextBox( gc.getWidth()/2 - 100, gc.getHeight()/2-150, 200, 100 );
			password = new DTextBox( gc.getWidth()/2 - 100, gc.getHeight()/2-50, 200, 100 );
			password.setPasswordInput( true );
			back = new DButton( "Back", gc.getWidth()/2 - 100, gc.getHeight()/2 + 50, 100, 100 );
			okay = new DButton( "Okay", gc.getWidth()/2, gc.getHeight()/2 + 50, 100, 100 );
			remember = new DCheckBox( gc.getWidth()/2 + 80, gc.getHeight()/2 + 160, 20, 20 );
			
			dui.add( usernameText );
			dui.add( passwordText );
			dui.add( errorText );
			dui.add( username );
			dui.add( password );
			dui.add( back );
			dui.add( okay );
			dui.add( remember );
			
			DText line1 = new DText( "Save login information", gc.getWidth()/2-110, gc.getHeight()/2 + 165 );
			DText line2 = new DText( "(Password is saved as plaintext)", gc.getWidth()/2, gc.getHeight()/2 + 185 );
			line2.setCentered( true );
			
			dui.add( line1 );
			dui.add( line2 );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
		r = new GdxRenderer();
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		dui.render( r.renderTo( g ) );
	}

	public void onExit()
	{
		errorText.setText( "" );
		username.setText( "" );
		password.setText( "" );
		dui.setEnabled( false );
	}
	
	public void message( Object o )
	{
		
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		{
			if( e == back )
			{
				dsh.activate( "home", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			}
			else if( e == okay )
			{
				boolean success = StaticFiles.login( username.getText(), password.getText() );
				
				if( !success )
				{
					errorText.setText( "Username/Password combination not found." );
				}
				else 
				{
					if( remember.checked )
					{
						try
						{
							DFile.saveObject( "data" + File.separator + "l.tmp", new String[] { username.getText().trim(), password.getText() } );
						} catch ( IOException e1 )
						{
							e1.printStackTrace();
						}
					}
					
					dsh.activate( "home", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
				}			
			}
		}
		else if( e instanceof DTextBox )
		{
			if( e == username && event.getType() == KeyEvent.VK_TAB )
			{
				dui.setFocus( password );
			}
		}
	}
}
