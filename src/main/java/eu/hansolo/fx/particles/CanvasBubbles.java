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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created by hansolo on 06.07.16.
 */
public class CanvasBubbles extends Application {
    private static final Random RND             = new Random();
    private static final double WIDTH           = 700;
    private static final double HEIGHT          = 700;
    private static final int    NO_OF_PARTICLES = 5000;
    private static       int    noOfNodes       = 0;
    private Image               image;
    private Canvas              canvas;
    private GraphicsContext     ctx;
    private ImageParticle[]     particles;
    private AnimationTimer      timer;


    // ******************** Constructor ***************************************
    public CanvasBubbles() {
        canvas    = new Canvas(WIDTH, HEIGHT);
        ctx       = canvas.getGraphicsContext2D();
        image     = new Image(getClass().getResourceAsStream("bubble.png"));
        particles = new ImageParticle[NO_OF_PARTICLES];
        timer     = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                draw();
            }
        };
        for (int i = 0 ; i < NO_OF_PARTICLES ; i++) {
            particles[i] = new ImageParticle(image);
        }
    }


    // ******************** Methods *******************************************
    @Override public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane(canvas);
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("Canvas Bubbles");
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
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0 ; i < NO_OF_PARTICLES ; i++) {
            ImageParticle p = particles[i];

            ctx.save();
            ctx.translate(p.x, p.y);
            ctx.scale(p.size, p.size);
            ctx.translate(p.image.getWidth() * (-0.5), p.image.getHeight() * (-0.5));
            ctx.setGlobalAlpha(p.opacity);
            ctx.drawImage(p.image, 0, 0);
            ctx.restore();

            p.x += p.vX;
            p.y += p.vY;

            // Reset particle
            if(p.y < -p.image.getHeight()) {
                particles[i].x = RND.nextDouble() * WIDTH;
                particles[i].y = HEIGHT + image.getHeight();
            }
        }
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
    private class ImageParticle {
        private final Random RND = new Random();
        private double       x;
        private double       y;
        private double       vX;
        private double       vY;
        private double       opacity;
        private double       size;
        private Image        image;


        // ******************** Constructor ***********************************
        public ImageParticle(final Image IMAGE) {
            // Position
            x = RND.nextDouble() * WIDTH;
            y = HEIGHT + IMAGE.getHeight();

            // Size
            size = (RND.nextDouble() * 0.6) + 0.1;

            // Velocity
            vX = (RND.nextDouble() * 0.5) - 0.25;
            vY = (-(RND.nextDouble() * 2) - 0.5) * size;

            // Opacity
            opacity = (RND.nextDouble() * 0.6) + 0.4;

            // Image
            image = IMAGE;
        }
    }
}