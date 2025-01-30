package com.danwink.tacticshooter.gameobjects;

public class UIDef
{
	public class Create {
		Type type;
		String name;
		Object content;
		
		public Create() {}
		
		public Create( Type type, String name, Object content ) {
			this.type = type;
			this.name = name;
			this.content = content;
		}
	}
	
	public class Event {
		Type type;
		String name;
		Object content;
		
		public Event() {}

		public Event( Type type, String name, Object content ) {
			this.type = type;
			this.name = name;
			this.content = content;
		}
	}
	
	public enum Type {
		BUTTON
	}
}
