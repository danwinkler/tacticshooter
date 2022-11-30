package com.phyloa.dlib.dui;

import java.awt.Color;

public class DUITheme
{
	public static DUITheme defaultTheme;
	
	static
	{
		defaultTheme = new DUITheme();
		defaultTheme.borderColor = new Color( 32, 32, 128 );
		defaultTheme.backgroundColor = new Color( 128, 128, 255 );
		defaultTheme.hoverColor = new Color( 96, 96, 200 );
	}
	
	public Color borderColor;
	public Color backgroundColor;
	public Color hoverColor;
	
	public DUITheme()
	{
		
	}
	
	public DUITheme( DUITheme theme )
	{
		this.borderColor = theme.borderColor;
		this.backgroundColor = theme.backgroundColor;
		this.hoverColor = theme.hoverColor;
	}
}
