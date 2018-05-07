package application;

import java.util.Optional;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import application.WebSocketChatStageController;

public class Main extends Application {
	private final String appName = "ChatApp";

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ChatStage.fxml"));
			AnchorPane root = fxmlLoader.load();

			if (!((WebSocketChatStageController) fxmlLoader.getController()).isSessionEstablished()) {
				serverUnavalibleAlert();
				return;
			}

			if (!showUserNameDialog(fxmlLoader))
				return;

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle(appName);
			primaryStage.setOnHiding(e -> primaryStage_Hiding(e, fxmlLoader));
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	private Boolean showUserNameDialog(FXMLLoader fxmlLoader) {
		while (true) {
			Integer res = userNameDialog(fxmlLoader);
			if (res == 2)
				return true;
			else if (res == 1)
				continue;
			else
				return false;
		}
	}

	private Integer userNameDialog(FXMLLoader fxmlLoader) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(appName);
		dialog.setHeaderText("Hi! To enter the chat select your username");
		dialog.setContentText("Username:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && result.get().length() > 0 && result.get().matches("[a-zA-Z0-9]*")) {
			((WebSocketChatStageController) fxmlLoader.getController()).setUserInfo(result.get());
			return 2;
		} else if (result.isPresent()) {
			incorrectUserName();
			return 1;
		}
		return 0;
	}

	private void incorrectUserName() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(appName);
		alert.setHeaderText("Incorrect username");
		alert.setContentText("The username was not given or it was inncorrect");

		alert.showAndWait();
	}

	private void serverUnavalibleAlert() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(appName);
		alert.setHeaderText("Server unavailable");
		alert.setContentText("The app couldn't connect to the server. Maybe it's offline.");

		alert.showAndWait();
	}

	private void primaryStage_Hiding(WindowEvent e, FXMLLoader fxmlLoader) {
		if (e == null)
			System.out.println("e is null");
		if (fxmlLoader == null)
			System.out.println("fxmlLoader is null");
		if (fxmlLoader.getController() == null)
			System.out.println("fxmlLoader.getController() is null");
		((WebSocketChatStageController) fxmlLoader.getController())
				.closeSession(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Stage is hiding"));
	}
}
