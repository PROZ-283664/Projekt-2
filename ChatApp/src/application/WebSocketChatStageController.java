package application;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
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

	@FXML
	private void initialize() {
		webSocketClient = new WebSocketClient();

		messagesLayout = Jsoup.parse(
				"<html><head><meta charset='UTF-8'><link href='https://fonts.googleapis.com/css?family=Source+Sans+Pro:400' rel='stylesheet' type='text/css'></head><body><ul></ul></body><script>window.scrollTo(0, document.body.scrollHeight);</script></html>",
				"UTF-16", Parser.xmlParser());
		messagesView.getEngine().loadContent(messagesLayout.html());
		messagesView.getEngine().setUserStyleSheetLocation(getClass().getResource("chat.css").toString());
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
			webSocketClient.sendMessage(selectedFile);
		}
	}

	@FXML
	private void messageTextField_KeyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			btnSend_Click();
		}
	}

	private void fileRecieved(String fileName, byte[] fileBytes) {

		if (!fileRecievedConfirm(fileName))
			return;

		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(fileName);

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showSaveDialog(null);

		if (file != null) {
			try {
				Files.write(file.toPath(), fileBytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	public void closeSession(CloseReason closeReason) {
		try {
			webSocketClient.session.close(closeReason);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setUserName(String name) {
		userName = name;
		welcomeLabel.setText("Hello " + name + "!");

		Image myImage = new Image(new File("res/harveyspecter.png").toURI().toString());
		ImagePattern pattern = new ImagePattern(myImage);
		circleImage.setFill(pattern);
	}

	public Boolean isSessionEstablished() {
		return webSocketClient.isSessionEstablished();
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
			String sender = message.getSender();
			Boolean isSender = sender.equals(userName);

			if (message instanceof TextMessage) {
				System.out.println("Message was received");

				TextMessage tMessage = (TextMessage) message;

				addMessage(sender, tMessage.toHTML(isSender));
			} else if (message instanceof FileMessage) {
				System.out.println("File was received");

				FileMessage fMessage = (FileMessage) message;
				String fileName = fMessage.getFileName();
				byte[] fileBytes = fMessage.getFile();

				addMessage(sender, fMessage.toHTML(isSender));

				if (isSender)
					return;
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						WebSocketChatStageController.this.fileRecieved(fileName, fileBytes);
					}
				});
			}
		}

		public void sendMessage(String message) {
			System.out.println("Message was sent");

			session.getAsyncRemote().sendObject(new TextMessage(message, userName));
		}

		public void sendMessage(File file) {
			try {
				System.out.println("File was sent");

				String fileName = file.getName();
				byte[] fileBytes = Files.readAllBytes(file.toPath());

				session.getAsyncRemote().sendObject(new FileMessage(fileName, fileBytes, userName));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		public Boolean isSessionEstablished() {
			return session != null;
		}

		private void addMessage(String sender, Element message) {
			Element wrapper = messagesLayout.getElementsByTag("ul").first();
			wrapper.appendChild(message);

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					messagesView.getEngine().loadContent(messagesLayout.html());
				}
			});
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
