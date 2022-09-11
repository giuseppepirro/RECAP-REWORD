package eu.gpirro.recap.controllers;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainLoader extends Application {

	public static String screen1ID = "main";
	public static String screen1File = "MainPanel.fxml";
	public static String screen2ID = "explanation";
	public static String screen2File = "Explanation.fxml";
	public static String screen3ID = "querying";
	public static String screen3File = "Querying.fxml";

	@Override
	public void start(Stage primaryStage) {

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"MainPanel.fxml"));

		Parent root = null;
		try {
			root = (Parent) fxmlLoader.load();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Scene scene = new Scene(root);

		primaryStage
				.setTitle("RECAP - Explaining Relatedness in Knowledge Graphs");

		// root.autosize();
		scene.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldSceneWidth, Number newSceneWidth) {
				// System.out.println("Width: " + newSceneWidth);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						/*
						 * Screen screen = Screen.getPrimary(); Rectangle2D
						 * bounds = screen.getVisualBounds();
						 * primaryStage.setX(bounds.getMinX());
						 * primaryStage.setY(bounds.getMinY());
						 * primaryStage.setWidth(bounds.getWidth());
						 * primaryStage.setHeight(bounds.getHeight());
						 */

						// /primaryStage.setMaximized(true);

						// primaryStage.setWidth(primaryStage.widthProperty().doubleValue());

						// primaryStage.sizeToScene();

					}
				});

			}

		});
		scene.heightProperty().addListener(new ChangeListener<Number>() 
				{
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldSceneHeight, Number newSceneHeight) {
				// System.out.println("Height: " + newSceneHeight);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						/*
						 * Screen screen = Screen.getPrimary(); Rectangle2D
						 * bounds = screen.getVisualBounds();
						 * primaryStage.setX(bounds.getMinX());
						 * primaryStage.setY(bounds.getMinY());
						 * primaryStage.setWidth(bounds.getWidth());
						 * primaryStage.setHeight(bounds.getHeight());
						 */
						// primaryStage.setHeight(primaryStage.widthProperty().doubleValue());

						// primaryStage.sizeToScene();

					}
				});

			}
		});

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/*
	 * @Override public void start(Stage primaryStage) {
	 * 
	 * try {
	 * 
	 * FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
	 * "/eu/gpirro/recap/gui/components/MainPanel.fxml"));
	 * 
	 * MainPanelController controller=new MainPanelController();
	 * fxmlLoader.setController(controller);
	 * 
	 * TabPane mainPanel = (TabPane) fxmlLoader.load();
	 * 
	 * Scene scene = new Scene(mainPanel);
	 * 
	 * controller.setStage(scene);
	 * 
	 * primaryStage.setScene(scene); // % //
	 * primaryStage.initStyle(StageStyle.TRANSPARENT);
	 * 
	 * //% primaryStage.show();
	 * 
	 * 
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace();
	 * Logger.getLogger(MainLoader.class.getName()).log(Level.SEVERE, null, e);
	 * 
	 * } }
	 */

	public static void main(String[] args) {
		Application.launch(MainLoader.class, (java.lang.String[]) null);
	}
}
