package co.labredes.upd.cliente;

import javax.swing.*;

import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.embed.swing.JFXPanel;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.util.Duration;

public class VideoPlayer extends JFrame {

	public static final String VID_URL = 
			"http://static.clipcanvas.com/sample/clipcanvas_14348_H264_320x180.mp4";

	private static final int VID_WIDTH     = 320;
	private static final int VID_HEIGHT    = 180;
	private static final int PLAYER_WIDTH  = 320;
	private static final int PLAYER_HEIGHT = 265;

	private void play(final String url) {
		final JFXPanel panel = new JFXPanel();
		Platform.runLater(new Runnable() {
			@Override public void run() {
				initFX(panel, url);
			}
		});
		this.add(panel);
		this.setSize(PLAYER_WIDTH, PLAYER_HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void initFX(JFXPanel panel, String url) {
		MediaView mediaView = createMediaView(url);

		final Scene playerScene = new Scene(
				createPlayerLayout(mediaView), 
				PLAYER_WIDTH, 
				PLAYER_HEIGHT
				);	

		setMediaEventHandlers(
				mediaView
				);

		panel.setScene(playerScene);
	}

	private MediaView createMediaView(String url) {
		final Media clip = new Media(url);
		final MediaPlayer player = new MediaPlayer(clip);
		final MediaView view = new MediaView(player);
		view.setFitWidth(VID_WIDTH);
		view.setFitHeight(VID_HEIGHT);
		return view;
	}

	private VBox createPlayerLayout(final MediaView view) {
		final Button button = new Button("Play From Start");
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				view.getMediaPlayer().seek(Duration.ZERO);
				view.getMediaPlayer().play();
			}
		});

		final VBox layout = new VBox(8);
		layout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(
				view,
				button
				);

		layout.setStyle("-fx-background-color: linear-gradient(to bottom, derive(lightseagreen, -20%), lightseagreen);");

		return layout;
	}

	private void setMediaEventHandlers(final MediaView view) {
		final MediaPlayer player = view.getMediaPlayer();

		System.out.println("Initial: " + player.getStatus());
		player.statusProperty().addListener(new ChangeListener<MediaPlayer.Status>() {
			@Override
			public void changed(ObservableValue<? extends MediaPlayer.Status> observable, MediaPlayer.Status oldStatus, MediaPlayer.Status curStatus) {
				System.out.println("Current: " + curStatus);
			}
		});

		if (player.getError() != null) {
			System.out.println("Initial Error: " + player.getError());
		}

		player.setOnError(new Runnable() {
			@Override public void run() {
				System.out.println("Current Error: " + player.getError());
			}
		});
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				VideoPlayer player = new VideoPlayer();
				player.play(VID_URL);
			}
		});
	}
}