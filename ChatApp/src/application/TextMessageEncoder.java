package application;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class TextMessageEncoder implements Encoder.Text<TextMessage> {

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig arg0) {
	}

	@Override
	public String encode(TextMessage msg) throws EncodeException {
		return msg.toString();
	}

}
