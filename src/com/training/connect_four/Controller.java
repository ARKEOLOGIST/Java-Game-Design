package com.training.connect_four;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";

	private static String PLAYER_ONE = "Player One";
	private static String PLAYER_TWO = "Player Two";

	private boolean isPlayerOneTurn = true;
	private boolean isAllowedtoInsert = false; //to fix animation issues

	private boolean isAllowedtoChange = true; //to fix naming change during game

	private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS]; //For handling structural changes

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscsPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	public Button setNames;

	@FXML
	public TextField setPlayerOneName;

	@FXML
	public TextField setPlayerTwoName;

	public void reverse() {
		isAllowedtoChange = true;
		isAllowedtoInsert = false;
	}

	public void change() {
		isAllowedtoChange = false;
		isAllowedtoInsert = true;
	}

	public void nameChange(String s1,String s2) {
		PLAYER_ONE = s1;
		PLAYER_TWO = s2;
		playerNameLabel.setText(s1);
	}

	public boolean get_change() {
		return isAllowedtoChange;
	}

	private Shape createGameStructuralGrid() {
		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

		for (int row = 0 ; row < ROWS ; row++)
		{
			for (int column = 0 ; column < COLUMNS; column++)
			{
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER / 2);
				circle.setCenterY(CIRCLE_DIAMETER / 2);
				circle.setSmooth(true);

				circle.setTranslateX(column * (CIRCLE_DIAMETER+5) + (CIRCLE_DIAMETER / 4));
				circle.setTranslateY(row * (CIRCLE_DIAMETER+5)+ (CIRCLE_DIAMETER / 4));

				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);

		return rectangleWithHoles;
	}

	public void createPlayground() {

		Shape rectangleWithHoles = createGameStructuralGrid();

		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList = createClickableColumns();

		for (Rectangle rectangle : rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}

	}

	public List<Rectangle> createClickableColumns() {

		List<Rectangle> rectangleList = new ArrayList<>();

		for (int col = 0 ; col < COLUMNS ; col++) {
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col *  (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));

			rectangle.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					rectangle.setFill(Color.valueOf("#eeeeee26"));
				}
			});
			rectangle.setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					rectangle.setFill(Color.TRANSPARENT);
				}
			});

			final int column = col;

			rectangle.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					if (isAllowedtoInsert)
					{
						isAllowedtoInsert = false; //Multiple discs cannot be inserted at once by multiple fast clicks
						insertDisc(new Disc(isPlayerOneTurn),column);
					}
				}
			});

			rectangleList.add(rectangle);
		}


		return rectangleList;
	}

	private void insertDisc(Disc disc, int column) {

		int row = ROWS - 1;

		while (row >= 0) {
			if (getDiscIfPresent(row,column) == null) {
				break;
			}
			row--;
		}

		//if column is full, we cannot insert any more discs
		if (row < 0) {
			return;
		}

		insertedDiscsArray[row][column] = disc; //For structural changes; for developers only
		insertedDiscsPane.getChildren().add(disc);

		disc.setTranslateX(column *  (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));

		final int currentRow = row;

		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row *  (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));
		translateTransition.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {

				isAllowedtoInsert = true; //After animation is finished, another disc can be inserted again
				if (gameEnded(currentRow,column)) {
					gameOver();
				}

				isPlayerOneTurn = !isPlayerOneTurn;

				playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
			}
		});

		translateTransition.play();

		disc.setTranslateY(5 *  (CIRCLE_DIAMETER + 5) + (CIRCLE_DIAMETER / 4));
	}

	//check if a player has won on the turn or not
	private boolean gameEnded(int row, int column) {
		//trying out the new lambda expressions
		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3,row + 3).mapToObj(r -> new Point2D(r,column)).collect(Collectors.toList());
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3,column + 3).mapToObj(c -> new Point2D(row,c)).collect(Collectors.toList());

		Point2D startPoint1 = new Point2D(row - 3, column + 3);

		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6).mapToObj(i -> startPoint1.add(i,-i)).collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row - 3, column - 3);

		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6).mapToObj(i -> startPoint2.add(i,i)).collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints) || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0;
		for (Point2D point: points) {

			int rowIndexforArray = (int) point.getX();
			int columnIndexforArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexforArray,columnIndexforArray);

			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {

				chain++;
				if (chain == 4) {
					return true;
				}
			} else {
					chain = 0;
				}
		}
		return false;
	}

	//to prevent ArrayIndexOutOfBoundException
	private Disc getDiscIfPresent(int row, int column) {
		if (row >= ROWS || row < 0 || column >= COLUMNS || column < 0) {
			return null;
		}
		return insertedDiscsArray[row][column];
	}

	private void gameOver() {

		String winner = isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO;
		//System.out.println(winner + " has won!");

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The winner is " +winner);
		alert.setContentText("Want to play again? ");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Quit");
		alert.getButtonTypes().setAll(yesBtn, noBtn);

		Platform.runLater(() -> {
			Optional<ButtonType> btnClicked = alert.showAndWait();
			if (btnClicked.isPresent() && btnClicked.get() == yesBtn) {
				resetGame();
			} else {
				Platform.exit();
				System.exit(0);
			}
		});

		//alert.show();

	}

	public void resetGame() {
		setPlayerOneName.setText("");
		setPlayerTwoName.setText("");
		insertedDiscsPane.getChildren().clear(); // Remove all inserted discs from the pane

		for (int row = 0; row < insertedDiscsArray.length; row++) {
			for (int col = 0; col < insertedDiscsArray[row].length; col++) {
				insertedDiscsArray[row][col] = null;
			}
		}

		reverse(); //allow game restart

		PLAYER_ONE = "Player One";
		PLAYER_TWO = "Player Two";

		playerNameLabel.setText(PLAYER_ONE);

		createPlayground();
	}

	//New class for handling the circle to be inserted
	private static class Disc extends Circle {

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove) {

			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}
}
