package com.training.connect_four;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
        GridPane rootGridPane = loader.load();

        controller = loader.getController();
        controller.createPlayground();

        MenuBar menuBar = createMenu();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        Pane menuPane = (Pane) rootGridPane.getChildren().get(0);
        menuPane.getChildren().add(menuBar);

        Scene scene = new Scene(rootGridPane);

        Button setNames = controller.setNames;
        TextField textField1 = controller.setPlayerOneName;
        TextField textField2 = controller.setPlayerTwoName;

        setNames.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (controller.get_change()) {
                    String s1 = textField1.getText();
                    String s2 = textField2.getText();
                    if (s1.trim().isEmpty() || s2.trim().isEmpty() || s1.equals(s2)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid name entered");
                        alert.setContentText("Please entered valid, different names in the two text boxes.");
                        alert.show();
                    } else {
                        controller.change();
                        controller.nameChange(s1,s2);
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Cannot change names");
                    alert.setContentText("You cannot change names during a game");
                    alert.show();
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Connect Four");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private MenuBar createMenu() {
        //File menu
        Menu fileMenu = new Menu("File");

        MenuItem newGame = new MenuItem("New Game");
        newGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetGame();
            }
        });
        MenuItem resetGame = new MenuItem("Reset Game");
        resetGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetGame();
            }
        });
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem exitGame = new MenuItem("Exit Game");
        exitGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.exit();
                System.exit(0);
            }
        });

        fileMenu.getItems().addAll(newGame,resetGame,separatorMenuItem,exitGame);

        //Help menu
        Menu helpMenu = new Menu("Help");

        MenuItem aboutGame = new MenuItem("About Connect Four");

        aboutGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                about(1);
            }
        });
        SeparatorMenuItem separator = new SeparatorMenuItem();
        MenuItem aboutCreator = new MenuItem("About Me");
        aboutCreator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                about(0);
            }
        });

        helpMenu.getItems().addAll(aboutGame, separator, aboutCreator);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    private void about(int n) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (n == 1) {
            alert.setTitle("About Connect Four");
            alert.setHeaderText("How to Play?");
            alert.setContentText("Connect Four is a two-player connection game in which the players first choose a color and then take turns dropping colored discs from the top into a seven-column, six-row vertically suspended grid. The pieces fall straight down, occupying the next available space within the column. The objective of the game is to be the first to form a horizontal, vertical, or diagonal line of four of one's own discs. Connect Four is a solved game. The first player can always win by playing the right moves.");
        } else {
            alert.setTitle("About the Developer");
            alert.setHeaderText("Arkadyuti Bandyopadhyay");
            alert.setContentText("Eager developer wishing to get into the gaming industry. Loves fiddling with code and playing video games (and also making them).");
        }
        alert.show();
    }

    private void resetGame() {
        controller.resetGame();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
