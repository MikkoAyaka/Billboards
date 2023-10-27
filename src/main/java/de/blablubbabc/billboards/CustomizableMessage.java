package de.blablubbabc.billboards;

public class CustomizableMessage {

	Message id;
	String text;
	String notes;

	CustomizableMessage(Message id, String text, String notes) {
		this.id = id;
		this.text = text;
		this.notes = notes;
	}
}
