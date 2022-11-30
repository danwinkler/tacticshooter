package com.phyloa.dlib.particle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class ParticleSystem<R>
{
	public LinkedList<Particle<R>> particles = new LinkedList<Particle<R>>();
	
	public void update( float d )
	{
		Iterator<Particle<R>> i = particles.iterator();
		
		while( i.hasNext() )
		{
			Particle<R> p = i.next();
			p.update( d );
			if( !p.alive )
			{
				i.remove();
			}
		}
	}
	
	public void render( R r )
	{
		Iterator<Particle<R>> i = particles.iterator();
		while( i.hasNext() )
		{
			Particle<R> p = i.next();
			p.render( r );
		}
	}
	
	public void add( Particle<R> p )
	{
		particles.add( p );
	}
	
	public void sort( Comparator<Particle<R>> c )
	{
		Collections.sort( particles, c );
	}
}
