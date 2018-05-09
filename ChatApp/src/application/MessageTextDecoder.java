package application;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageTextDecoder implements Decoder.Text<Message> {
	@Override
	public void init(EndpointConfig ec) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public Message decode(String string) throws DecodeException {
		try {
			JSONObject jsonObject = new JSONObject(string);
			String type = jsonObject.get("type").toString();
			if (type.equals("TextMessage")) {
				String message = jsonObject.get("message").toString();
				String sender = jsonObject.get("sender").toString();

				return new TextMessage(message, sender);
			} else if (type.equals("FileMessage")) {
				String fileName = jsonObject.get("fileName").toString();
				String sender = jsonObject.get("sender").toString();
				Integer uID = (Integer) jsonObject.get("ID");
				long fileSize = jsonObject.getLong("size");

				return new FileMessage(fileName, fileSize, sender, uID);
			} else {
				throw new DecodeException(string, "[Message] Can't decode.");
			}

		} catch (JSONException e) {
			e.printStackTrace();
			throw new DecodeException(string, "[Message] Can't decode.");
		}
	}

	@Override
	public boolean willDecode(String string) {
		return true;
	}
}
