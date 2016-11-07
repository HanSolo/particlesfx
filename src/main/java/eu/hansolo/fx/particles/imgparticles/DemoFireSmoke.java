/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.particles.imgparticles;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


/**
 * Created by hansolo on 07.11.16.
 */
public class DemoFireSmoke extends Application {
    private boolean toggle = false;

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane();

        GridPane grid = new GridPane();
        grid.setPrefSize(200, 200);
        grid.setHgap(5);
        grid.setVgap(10);
        grid.add(new Label("Label1"), 0, 0);
        grid.add(new TextField(), 1, 0);
        grid.add(new Label("Label2"), 0, 1);
        grid.add(new TextField(), 1, 1);
        grid.add(new Label("Label3"), 0, 2);
        grid.add(new TextField(), 1, 2);
        grid.add(new Button("Press me"), 0, 3);
        grid.setTranslateX(60);
        grid.setTranslateY(100);

        Fire fire = new Fire();
        fire.setWidth(400);
        fire.setHeight(400);
        fire.setMouseTransparent(true);
        Smoke smoke = new Smoke();
        smoke.setWidth(400);
        smoke.setHeight(400);
        smoke.setMouseTransparent(true);

        pane.getChildren().addAll(grid, smoke, fire);

        Scene scene = new Scene(pane, 400, 400, Color.WHITE);
        scene.widthProperty().addListener(e -> {
            fire.setWidth(scene.getWidth());
            smoke.setWidth(scene.getWidth());
        });
        scene.heightProperty().addListener(e -> {
            fire.setHeight(scene.getHeight());
            smoke.setHeight(scene.getHeight());
        });

        stage.setScene(scene);
        stage.setTitle("JavaFX Particles");
        stage.show();

        pane.setOnMousePressed(mouseEvent -> {
            toggle ^= true;
            if (toggle) {
                fire.start();
                smoke.start();
            } else {
                fire.stop();
                smoke.stop();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
