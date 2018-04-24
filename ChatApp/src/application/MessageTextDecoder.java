package application;

import java.io.ByteArrayOutputStream;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONArray;
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

				JSONArray fileArray = (JSONArray) jsonObject.get("file");
				ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
				for (Integer i = 0; i < fileArray.length(); ++i) {
					fileBytes.write(fileArray.getInt(i));
				}

				return new FileMessage(fileName, fileBytes.toByteArray(), sender);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			throw new DecodeException(string, "[Message] Can't decode.");
		}
		return null;
	}

	@Override
	public boolean willDecode(String string) {
		return true;
	}
}
