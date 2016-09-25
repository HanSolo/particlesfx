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
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created by hansolo on 04.07.16.
 */
public class ConnectedParticles extends Application {
    private static final Random          RND            = new Random();
    private static final double          WIDTH          = 500;
    private static final double          HEIGHT         = 500;
    private static final int             PARTICLE_COUNT = 100;
    private static final double          MIN_DISTANCE   = 70;
    private              Canvas          canvas;
    private              GraphicsContext ctx;
    private              Particle[]      particles;
    private              AnimationTimer  timer;


    // ******************** Constructor ***************************************
    public ConnectedParticles() {
        canvas    = new Canvas(WIDTH, HEIGHT);
        ctx       = canvas.getGraphicsContext2D();
        particles = new Particle[PARTICLE_COUNT];
        timer     = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                draw();
            }
        };
        for (int i = 0 ; i < PARTICLE_COUNT; i++) {
            particles[i] = new Particle();
        }
    }


    // ******************** Methods *******************************************
    private void draw() {
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        update();
        for (int i = 0 ; i < PARTICLE_COUNT; i++) { particles[i].draw(); }
    }

    private void update() {
        for (int i = 0 ; i < PARTICLE_COUNT; i++) {
            Particle p = particles[i];
            p.x += p.vX;
            p.y += p.vY;

            if(p.x + p.radius > WIDTH) {
                p.x = p.radius;
            } else if(p.x - p.radius < 0) {
                p.x = WIDTH - p.radius;
            }

            if(p.y + p.radius > HEIGHT) {
                p.y = p.radius;
            } else if(p.y - p.radius < 0) {
                p.y = HEIGHT - p.radius;
            }

            for(int j = i + 1; j < PARTICLE_COUNT; j++) {
                distance(p, particles[j]);
            }
        }
    }

    private void distance(final Particle P1, final Particle P2) {
        double dx       = P1.x - P2.x;
        double dy       = P1.y - P2.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (MIN_DISTANCE > distance) {
            ctx.setStroke(Color.rgb(255, 255, 255, (1.0 - distance / MIN_DISTANCE)));
            ctx.strokeLine(P1.x, P1.y, P2.x, P2.y);

            double ax = dx / 2000;
            double ay = dy / 2000;
            P1.vX -= ax;
            P1.vY -= ay;
            P2.vX += ax;
            P2.vY += ay;
        }
    }

    @Override public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane();
        pane.getChildren().add(canvas);

        Scene scene = new Scene(pane, Color.BLACK);

        stage.setScene(scene);
        stage.setTitle("Demo JavaFX Canvas Particles");
        stage.show();

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** InnerClasses **************************************
    private class Particle {
        public double x;
        public double y;
        public double vX;
        public double vY;
        public double radius;
        public double size;


        // ******************** Constructor ***********************************
        public Particle() {
            x      = RND.nextDouble() * WIDTH;
            y      = RND.nextDouble() * HEIGHT;
            vX     = -1 + RND.nextDouble() * 2;
            vY     = -1 + RND.nextDouble() * 2;
            radius = 4;
            size   = 2 * radius;
        }

        public void draw() {
            ctx.setFill(Color.WHITE);
            ctx.fillOval(x - radius, y - radius, size, size);

            //ctx.beginPath();
            //ctx.arc(x, y, radius, radius, 0, 360);
            //ctx.fill();
        }
    }
}
