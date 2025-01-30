package com.phyloa.dlib.renderer;

import java.awt.Image;

public interface IRenderer extends Renderer2D<Image> {
	/**
	 * Returns the Image being rendered to. Thread safe.
	 * 
	 * @return Image the image being rendered to.
	 */
	public Image getImage();
}
