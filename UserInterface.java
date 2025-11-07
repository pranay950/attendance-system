package com.yourorg.attendance;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserInterface extends Application {
    @Override
    public void start(Stage stage) {
        Button captureBtn = new Button("Capture Faces (userId=1)");
        Button trainBtn = new Button("Train Model");
        Button recognizeBtn = new Button("Recognize & Mark Attendance");

        // Adjust dataset path, personName, userId as needed
        captureBtn.setOnAction(e -> FaceCapture.capture("dataset", "person1", 1));
        trainBtn.setOnAction(e -> FaceTrainer.train("dataset", "trainer.yml"));
        recognizeBtn.setOnAction(e -> FaceRecognizer.recognize("trainer.yml"));

        VBox v = new VBox(10, captureBtn, trainBtn, recognizeBtn);
        stage.setScene(new Scene(v, 360, 140));
        stage.setTitle("Smart Attendance");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
