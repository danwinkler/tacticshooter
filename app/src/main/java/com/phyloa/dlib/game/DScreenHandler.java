package com.phyloa.dlib.game;

import java.util.HashMap;

import com.phyloa.dlib.renderer.DScreenTransition;

public class DScreenHandler<E, F>
{
	HashMap<String, DScreen<E, F>> screens = new HashMap<String, DScreen<E, F>>();
	
	DScreen<E, F> d = null;
	
	DScreenTransition<E, F> out;
	DScreenTransition<E, F> in;
	
	DScreen<E, F> nextScreen;
	
	public void register( String s, DScreen<E, F> d )
	{
		screens.put( s, d );
	}
	
	public void activate( String s, E e )
	{
		if( d != null )
		{
			d.onExit();
		}
		d = screens.get( s );
		synchronized( d )
		{
			d.dsh = this;
			d.gc = e;
			d.onActivate( e, this );
		}
	}
	
	public void activate( String s, E e, DScreenTransition<E, F> out, DScreenTransition<E, F> in )
	{
		this.out = out;
		this.in = in;
		
		nextScreen = get( s );
		
		if( out != null )
		{
			out.init( e );
		}
	}
	
	public DScreen<E, F> get( String s )
	{
		return screens.get( s );
	}
	
	public DScreen<E, F> get()
	{
		return d;
	}
	
	public void update( E e, float delta )
	{
		synchronized( d )
		{
			if( nextScreen != null )
			{
				if( out != null )
				{
					if( out.isFinished() )
					{
						if( d != null )
						{
							d.onExit();
						}
						d = nextScreen;
						d.gc = e;
						d.dsh = this;
						d.onActivate( e, this );
						nextScreen = null;
						out = null;
						if( in != null )
						{
							in.init( e );
						}
					}
					else
					{
						out.update( e, delta );
					}
				}
			}
			else
			{
				if( in != null )
				{
					in.update( e, delta );
					if( in.isFinished() )
					{
						in = null;
					}
				}
				if( d != null )
				{
					d.update( e, delta );
				}
			}
		}
	}
	
	public void render( E e, F f )
	{
		synchronized( d )
		{
			if( nextScreen != null )
			{
				if( out != null )
				{
					out.renderPre( e, f );
				}
			}
			else if( in != null )
			{
				in.renderPre( e, f );
			}
			
			if( d != null )
			{
				d.render( e, f );
			}
			
			if( nextScreen != null )
			{
				if( out != null )
				{
					out.renderPost( e, f );
				}
			}
			else if( in != null )
			{
				in.renderPost( e, f );
			}
		}
	}

	public void message( String string, Object o )
	{
		get( string ).message( o );
	}
}
