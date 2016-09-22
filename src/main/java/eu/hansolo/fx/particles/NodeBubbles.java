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

package eu.hansolo.fx.particles;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.Random;


/**
 * User: hansolo
 * Date: 06.07.16
 * Time: 09:25
 */
public class NodeBubbles extends Application {
    private static final Random          RND             = new Random();
    private static final double          WIDTH           = 700;
    private static final double          HEIGHT          = 700;
    private static final int             NO_OF_PARTICLES = 5000;
    private static       int             noOfNodes       = 0;
    private              Particle[]      particles;
    private              AnimationTimer  timer;


    @Override public void init() {
        particles = new Particle[NO_OF_PARTICLES];
        timer     = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                draw();
            }
        };
        for (int i = 0 ; i < NO_OF_PARTICLES ; i++) { particles[i] = new Particle(); }
    }


    // ******************** Methods *******************************************
    @Override public void start(Stage stage) {
        Pane pane = new Pane(particles);
        pane.setPrefSize(WIDTH, HEIGHT);
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("NodeBubbles");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");
        System.out.println(NO_OF_PARTICLES + " Particles");

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private void draw() {
        for (int i = 0 ; i < NO_OF_PARTICLES ; i++) {
            Particle p = particles[i];

            p.setLayoutX(p.getLayoutX() + p.vX);
            p.setLayoutY(p.getLayoutY() + p.vY);

            // Reset particle
            if(p.getLayoutY() < -p.getPrefHeight()) {
                particles[i].setLayoutX(RND.nextDouble() * WIDTH);
                particles[i].setLayoutY(HEIGHT + particles[i].getPrefHeight());
            }
        }
    }

    private Node[] createBubble(final double SIZE) {
        final double CENTER = SIZE * 0.5;
        final Node[] NODES  = new Node[3];

        final Circle MAIN = new Circle(CENTER, CENTER, CENTER);
        final Paint MAIN_FILL = new LinearGradient(CENTER, 0.02 * SIZE,
                                                   0.50 * SIZE, 0.98 * SIZE,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.TRANSPARENT),
                                                   new Stop(0.85, Color.rgb(255, 255, 255, 0.2)),
                                                   new Stop(1.0, Color.rgb(255, 255, 255, 0.90)));
        MAIN.setFill(MAIN_FILL);
        MAIN.setStroke(null);

        final Circle FRAME      = new Circle(CENTER, CENTER, CENTER);
        final Paint  FRAME_FILL = new RadialGradient(0, 0,
                                                     CENTER, CENTER,
                                                     0.48 * SIZE,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, Color.TRANSPARENT),
                                                     new Stop(0.92, Color.TRANSPARENT),
                                                     new Stop(1.0, Color.rgb(255, 255, 255, 0.5)));
        FRAME.setFill(FRAME_FILL);
        FRAME.setStroke(null);

        final Ellipse HIGHLIGHT      = new Ellipse(CENTER, 0.27 * SIZE, 0.38 * SIZE, 0.25 * SIZE);
        final Paint   HIGHLIGHT_FILL = new LinearGradient(CENTER, 0.04 * SIZE,
                                                         CENTER, CENTER,
                                                         false, CycleMethod.NO_CYCLE,
                                                         new Stop(0.0, Color.rgb(255, 255, 255, 0.7)),
                                                         new Stop(1.0, Color.TRANSPARENT));
        HIGHLIGHT.setFill(HIGHLIGHT_FILL);
        HIGHLIGHT.setStroke(null);

        NODES[0] = FRAME;
        NODES[1] = MAIN;
        NODES[2] = HIGHLIGHT;
        
        return NODES;
    }


    // ******************** Misc **********************************************
    private static void calcNoOfNodes(Node node) {
        if (node instanceof Parent) {
            if (((Parent) node).getChildrenUnmodifiable().size() != 0) {
                ObservableList<Node> tempChildren = ((Parent) node).getChildrenUnmodifiable();
                noOfNodes += tempChildren.size();
                for (Node n : tempChildren) { calcNoOfNodes(n); }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** InnerClasses **************************************
    private class Particle extends Region {
        public double vX;
        public double vY;

        public Particle() {
            super();

            // Size
            double size = 50 * (RND.nextDouble() * 0.6) + 0.1;
            setPrefSize(size, size);

            //getStyleClass().add("bubble");
            getChildren().addAll(createBubble(size));

            // Position
            setLayoutX(RND.nextDouble() * WIDTH);
            setLayoutY(HEIGHT + size);

            // Velocity
            vX = (RND.nextDouble() * 0.5) - 0.25;
            vY = (-(RND.nextDouble() * 2) - 0.5) * (size / 50);

            // Opacity
            setOpacity((RND.nextDouble() * 0.6) + 0.4);

            // Enable Caching
            setCache(true);
            setCacheHint(CacheHint.SPEED);
        }
    }
}
