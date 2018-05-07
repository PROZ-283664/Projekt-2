package application;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Message implements Serializable {
	private static final long serialVersionUID = 3087800204086869692L;
	private final String sender;
	private final Integer uniqueID;

	Message(String from, Integer uID) {
		sender = from;
		uniqueID = uID;
	}

	protected String getSender() {
		return sender;
	}

	protected Integer getUID() {
		return uniqueID;
	}

	@Override
	public String toString() {
		String jsonString = null;
		try {
			jsonString = new JSONObject().put("sender", sender).put("ID", uniqueID).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}
