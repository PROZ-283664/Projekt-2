package application;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class FileMessageEncoder implements Encoder.Text<FileMessage> {

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig arg0) {
	}

	@Override
	public String encode(FileMessage msg) throws EncodeException {
		return msg.toString();
	}

}
