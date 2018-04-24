package application;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Message implements Serializable {
	private static final long serialVersionUID = 3087800204086869692L;
	private final String sender;
	private final Date date;
	
	Message(String from){
		sender = from;
		date = new Date();
	}
	
	protected String getSender() {
		return sender;
	}
	
	protected Date getDate() {
		return date;
	}
	
	@Override
	public String toString() {
		String jsonString = null;
		try {
			jsonString = new JSONObject()
			        .put("sender", sender)
			        .put("date", date)
			        .toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}
