package tacticshooter;

public interface ServerInterface 
{
	public abstract void sendToClient( int id, Message m );
	public abstract void sendToAllClients( Message m );
	public abstract Message getNextServerMessage();
	public abstract boolean hasServerMessages();
	public abstract void stop();
}
