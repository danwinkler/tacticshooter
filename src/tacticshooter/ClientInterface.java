package tacticshooter;

public interface ClientInterface 
{
	public abstract void sendToServer( Message m );
	public abstract Message getNextClientMessage();
	public abstract boolean hasClientMessages();
}
