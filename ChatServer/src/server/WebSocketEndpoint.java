package server;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/websocketendpoint")
public class WebSocketEndpoint {

	@OnOpen
	public void onOpen(Session session) {
		try {
			session.getBasicRemote().sendText(
					"{\"sender\":\"SERVER\",\"type\":\"TextMessage\",\"message\":\"Nice to see you there! This is a welcome message. Say hello to other users and try to send files.\"}");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		for (Session oneSession : session.getOpenSessions()) {
			if (session.getId() == oneSession.getId())
				continue;
			try {
				if (oneSession.isOpen()) {
					oneSession.getBasicRemote().sendText(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@OnMessage
	public void onMessage(ByteBuffer content, Session session) {
		for (Session oneSession : session.getOpenSessions()) {
			if (session.getId() == oneSession.getId())
				continue;
			if (oneSession.isOpen()) {
				try {
					oneSession.getBasicRemote().sendBinary(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

} // public class WebSocketEndpoint
