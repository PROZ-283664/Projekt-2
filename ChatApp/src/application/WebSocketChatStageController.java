package application;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

public class WebSocketChatStageController {
	@FXML
	TextArea chatTextArea;
	@FXML
	TextField messageTextField;
	@FXML
	Label welcomeLabel;
	@FXML
	WebView messagesView;
	@FXML
	Circle circleImage;

	private String userName = "";
	private WebSocketClient webSocketClient;
	private Document messagesLayout;
	private HashMap<Integer, FileHandler> recievedFiles;
	private Random randomGenerator;

	@FXML
	private void initialize() {
		webSocketClient = new WebSocketClient();

		messagesLayout = Jsoup.parse(
				"<html><head><meta charset='UTF-8'><link href='https://fonts.googleapis.com/css?family=Source+Sans+Pro:400' rel='stylesheet' type='text/css'></head><body><ul></ul></body><script>window.scrollTo(0, document.body.scrollHeight);</script></html>",
				"UTF-16", Parser.xmlParser());
		messagesView.getEngine().loadContent(messagesLayout.html());
		messagesView.getEngine().setUserStyleSheetLocation(getClass().getResource("chat.css").toString());
		recievedFiles = new HashMap<>();
		randomGenerator = new Random();
	}

	@FXML
	private void btnSend_Click() {
		if (messageTextField.getLength() == 0)
			return;

		webSocketClient.sendMessage(messageTextField.getText());
		messageTextField.clear();

	}

	@FXML
	private void btnAttachment_Click() {
		FileChooser fileChooser = new FileChooser();
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Integer uID = randomGenerator.nextInt();
					FileHandler fh = new FileHandler(selectedFile, uID);
					ByteBuffer chunkBytes = fh.getNextChunk();
					webSocketClient.sendMessage(fh.getFileName(), uID);
					while (chunkBytes != null) {
						webSocketClient
								.sendFile(new FileBytes(uID, chunkBytes, (chunkBytes = fh.getNextChunk()) == null));
						System.gc();
					}
				}
			});
			thread.start();
		}
	}

	@FXML
	private void messageTextField_KeyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			btnSend_Click();
		}
	}

	private File fileRecievedDialog(String fileName) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(fileName);

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showSaveDialog(null);

		return file;
	}

	private Boolean fileRecievedConfirm(String fileName) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("ChatApp");
		alert.setHeaderText("Recieved new file: \"" + fileName + "\"");
		alert.setContentText("Do you want to save it?");

		ButtonType buttonTypeYes = new ButtonType("Yes", ButtonData.YES);
		ButtonType buttonTypeNo = new ButtonType("No", ButtonData.NO);

		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get().getButtonData() == ButtonData.YES) {
			return true;
		} else {
			return false;
		}
	}

	private void addMessage(String sender, Element message) {
		Element wrapper = messagesLayout.getElementsByTag("ul").first();
		wrapper.appendChild(message);

		messagesView.getEngine().loadContent(messagesLayout.html());
	}

	private void processFileMessage(FileMessage fMessage) {
		String sender = fMessage.getSender();
		Boolean isSender = sender.equals(userName);
		Integer uID = fMessage.getUID();

		recievedFiles.put(uID, new FileHandler(fMessage));

		addMessage(sender, fMessage.toHTML(isSender));
	}

	private void processFile(FileBytes fileByte) {
		Integer uID = fileByte.getUID();
		FileHandler fh = recievedFiles.get(uID);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				fh.joinMessage(fileByte);
			}
		});
		thread.start();
		if (fileByte.isLast()) {
			if (fileRecievedConfirm(fh.getFileName())) {
				File file = fileRecievedDialog(fh.getFileName());
				fh.saveFile(file.toPath());
			}
		}
		System.gc();
	}

	private void processTextMessage(TextMessage tMessage) {
		String sender = tMessage.getSender();
		Boolean isSender = sender.equals(userName);

		addMessage(sender, tMessage.toHTML(isSender));
	}

	public void closeSession(CloseReason closeReason) {
		try {
			webSocketClient.session.close(closeReason);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setUserInfo(String name) {
		userName = name;
		welcomeLabel.setText("Hello " + name + "!");

		Image myImage = new Image(new File("res/harveyspecter.png").toURI().toString());
		ImagePattern pattern = new ImagePattern(myImage);
		circleImage.setFill(pattern);
	}

	public Boolean isSessionEstablished() {
		return webSocketClient.isSessionEstablished();
	}

	static String memory() {
		final int unit = 1000000; // MB
		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long availableMemory = Runtime.getRuntime().maxMemory() - usedMemory;
		return "Memory: free=" + (Runtime.getRuntime().freeMemory() / unit) + " total="
				+ (Runtime.getRuntime().totalMemory() / unit) + " max=" + (Runtime.getRuntime().maxMemory() / unit
						+ " used=" + usedMemory / unit + " available=" + availableMemory / unit);
	}

	@ClientEndpoint(encoders = { TextMessageEncoder.class, FileMessageEncoder.class }, decoders = {
			MessageTextDecoder.class })
	public class WebSocketClient {
		private Session session;

		public WebSocketClient() {

			connectToWebSocket();
		}

		@OnOpen
		public void onOpen(Session session) {
			System.out.println("Connection is opened.");
			this.session = session;
		}

		@OnClose
		public void onClose(CloseReason closeReason) {
			System.out.println("Connection is closed: " + closeReason.getReasonPhrase());
			Platform.exit();
		}

		@OnError
		public void onError(Throwable throwable) {
			System.out.println("Error occured");
			throwable.printStackTrace();
		}

		@OnMessage
		public void onMessage(Message message, Session session) {
			System.out.println("tekst - ");
			if (message instanceof TextMessage) {
				TextMessage tMessage = (TextMessage) message;
				Platform.runLater(() -> {
					WebSocketChatStageController.this.processTextMessage(tMessage);
				});
			} else if (message instanceof FileMessage) {
				FileMessage fMessage = (FileMessage) message;
				Platform.runLater(() -> {
					WebSocketChatStageController.this.processFileMessage(fMessage);
				});
			}
		}

		@OnMessage
		public void onMessage(ByteBuffer message, Session session) {
			Platform.runLater(() -> {
				WebSocketChatStageController.this.processFile(FileBytesDecoder.decode(message));
			});
		}

		public void sendMessage(String message) {
			try {
				session.getBasicRemote().sendObject(new TextMessage(message, userName));
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String fileName, Integer hash) {
			try {
				session.getBasicRemote().sendObject(new FileMessage(fileName, userName, hash));
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
			}
		}

		public void sendFile(FileBytes fileBytes) {
			try {
				session.getBasicRemote().sendBinary(FileBytesEncoder.encode(fileBytes));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public Boolean isSessionEstablished() {
			return session != null;
		}

		private void connectToWebSocket() {
			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			try {
				URI uri = URI.create("ws://localhost:8080/ChatServer/websocketendpoint");
				webSocketContainer.connectToServer(this, uri);
			} catch (DeploymentException | IOException e) {
				e.printStackTrace();
				Platform.exit();
			}
		}
	}
}
