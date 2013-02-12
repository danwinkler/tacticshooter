package com.danwink.tacticshooter.screens;

import java.io.BufferedReader;
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
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LoginScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DText usernameText;
	DText passwordText;
	DTextBox username;
	DTextBox password;
	DButton back;
	DButton okay;
	
	Slick2DRenderer r;
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			usernameText = new DText( "Username:", gc.getWidth()/2 - 150, gc.getHeight()/2-100 );
			usernameText.setCentered( true );
			passwordText = new DText( "Password:", gc.getWidth()/2 - 150, gc.getHeight()/2 );
			passwordText.setCentered( true );
			username = new DTextBox( gc.getWidth()/2 - 100, gc.getHeight()/2-150, 200, 100 );
			password = new DTextBox( gc.getWidth()/2 - 100, gc.getHeight()/2-50, 200, 100 );
			password.setPasswordInput( true );
			back = new DButton( "Back", gc.getWidth()/2 - 100, gc.getHeight()/2 + 50, 100, 100 );
			okay = new DButton( "Okay", gc.getWidth()/2, gc.getHeight()/2 + 50, 100, 100 );
			
			dui.add( usernameText );
			dui.add( passwordText );
			dui.add( username );
			dui.add( password );
			dui.add( back );
			dui.add( okay );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
		r = new Slick2DRenderer();
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
				try
				{
					HttpClient client = new DefaultHttpClient();
					HttpPost httppost = new HttpPost("http://www.tacticshooter.com/user/checkLogin");

					ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

					list.add( new BasicNameValuePair( "username", username.getText().trim() ) );
					list.add( new BasicNameValuePair( "password", password.getText() ) );

					httppost.setEntity( new UrlEncodedFormEntity(list) );

					HttpResponse r = client.execute(httppost);
					
					String s = EntityUtils.toString( r.getEntity() );
					
					dsh.message( "message", s );
					dsh.activate( "message", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			 
				} catch( MalformedURLException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch( IOException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
