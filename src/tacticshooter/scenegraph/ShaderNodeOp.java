package tacticshooter.scenegraph;

import org.newdawn.slick.opengl.shader.ShaderProgram;

public interface ShaderNodeOp
{
	public void execute( ShaderProgram shader, Node n );
}
