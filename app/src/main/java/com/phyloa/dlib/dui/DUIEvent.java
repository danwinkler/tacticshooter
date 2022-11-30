package com.phyloa.dlib.dui;

public class DUIEvent 
{
	DUIElement element;
	int type;
	
	public DUIEvent( DUIElement e )
	{
		this.element = e;
	}
	
	public DUIEvent( DUIElement e, int type )
	{
		this.element = e;
		this.type = type;
	}

	public DUIElement getElement()
	{
		return element;
	}
	
	public int getType()
	{
		return type;
	}
}
