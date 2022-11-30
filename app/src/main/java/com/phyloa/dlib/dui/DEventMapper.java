package com.phyloa.dlib.dui;

public interface DEventMapper
{
	public void addDKeyListener( DKeyListener l );
	public void addDMouseListener( DMouseListener l );
	
	public void removeDKeyListener( DKeyListener l );
	public void removeDMouseListener( DMouseListener l );
	
	public void setEnabled( boolean enabled );
}
