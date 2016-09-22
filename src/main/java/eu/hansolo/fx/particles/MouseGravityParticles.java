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
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;


/**
 * Created with IntelliJ IDEA.
 * User: hansolo
 * Date: 19.09.12
 * Time: 09:21
 * To change this template use File | Settings | File Templates.
 */
public class MouseGravityParticles extends Application {
    private static final Random   RND               = new Random();
    private static final boolean  REFLECT_ON_BORDER = true;
    private static final double   WIDTH             = 700;
    private static final double   HEIGHT            = 700;
    private static final int      NO_OF_PARTICLES   = 20000;
    private static final double   MIN_DISTANCE      = 100;   // 100
    private static final double   MAX_DISTANCE      = 3000; // 3000
    private static final int      X                 = 0;
    private static final int      Y                 = 1;
    private static final int      VX                = 2;
    private static final int      VY                = 3;
    private static final int      AX                = 4;
    private static final int      AY                = 5;
    private static final int      MOUSE_GRAVITY     = 18;    // 9
    private static final int      SPEED             = 6;
    private static final int      NO_OF_FIELDS      = 7;    // x, y, vx, vy, ax, ay, speed
    private static final int      ARRAY_LENGTH      = NO_OF_PARTICLES * NO_OF_FIELDS;
    private static final double   SPEED_LIMIT       = 20.5;    // 2
    private static final double   DAMPING           = 0.025;  // 0.5
    private final Canvas          CANVAS;
    private final GraphicsContext CTX;
    private double[]              mousePos;
    private int                   currentIndex;
    private double[]              particles;
    private AnimationTimer        timer;


    // ******************** Constructor ***************************************
    public MouseGravityParticles() {
        CANVAS        = new Canvas(WIDTH, HEIGHT);
        CTX           = CANVAS.getGraphicsContext2D();
        mousePos      = new double[2];
        timer         = new AnimationTimer() {
            @Override public void handle(final long NOW) { draw(); }
        };

        // Initialize particles
        particles             = new double[ARRAY_LENGTH];
        double initialSpeed   = 1;
        int nextParticleIndex = 0; // next position to insert new particle
        for (int i = 0 ; i < NO_OF_PARTICLES; i++) {
            nextParticleIndex = (nextParticleIndex + NO_OF_FIELDS) % ARRAY_LENGTH;
            particles[nextParticleIndex + X]     = RND.nextDouble() * WIDTH;
            particles[nextParticleIndex + Y]     = RND.nextDouble() * HEIGHT;
            particles[nextParticleIndex + VX]    = RND.nextDouble() * initialSpeed - initialSpeed * 0.5;
            particles[nextParticleIndex + VY]    = RND.nextDouble() * initialSpeed - initialSpeed * 0.5;
            particles[nextParticleIndex + AX]    = 0;
            particles[nextParticleIndex + AY]    = 0;
            particles[nextParticleIndex + SPEED] = 0;
        }

        CANVAS.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent EVENT) {
                mousePos[0] = EVENT.getX();
                mousePos[1] = EVENT.getY();
            }
        });
    }


    // ******************** Methods *******************************************
    private void draw() {
        CTX.clearRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0 ; i < NO_OF_PARTICLES; i++) {
            currentIndex = i * NO_OF_FIELDS;
            checkGravity(currentIndex);
            update((i * NO_OF_FIELDS));
            drawParticle(currentIndex, CTX);
        }
    }

    private void checkGravity(final int INDEX) {
        double dX = particles[INDEX + X] - mousePos[0];
        double dY = particles[INDEX + Y] - mousePos[1];

        //double distance = Math.sqrt(dX * dX + dY * dY);
        double distance = 1 / invSqrt(dX * dX + dY * dY);

        // scale the vector to the inverse square distance
        dX /= (Math.pow(distance, 2.5) / MOUSE_GRAVITY); // bigger values increase gravity of mouseposition
        dY /= (Math.pow(distance, 2.5) / MOUSE_GRAVITY);

        if(distance > MIN_DISTANCE){
            particles[INDEX + AX] -= dX * 2;
            particles[INDEX + AY] -= dY * 2;
            CTX.setFill(Color.WHITE);
        } else {
            double saturation = 1 / MAX_DISTANCE * distance * 32;
            saturation = saturation < 0 ? 0 : (saturation > 1 ? 1 : saturation);
            CTX.setFill(Color.hsb(1, 1 - saturation, 1));
        }
    }

    private void update(int pos) {
        // Calculate speed
        particles[pos + SPEED] = 1 / invSqrt(particles[pos + VX] * particles[pos + VX] + particles[pos + VY] * particles[pos + VY]);
        //particles[pos + SPEED] = Math.sqrt(particles[pos + VX] * particles[pos + VX] + particles[pos + VY] * particles[pos + VY]);
        if(particles[pos + SPEED] > SPEED_LIMIT) {
            particles[pos + VX] /= (particles[pos + SPEED] / SPEED_LIMIT);
            particles[pos + VY] /= (particles[pos + SPEED] / SPEED_LIMIT);
        }

        // Calculate velocity and new pos
        particles[pos + X] += (particles[pos + VX] += particles[pos + AX]);
        particles[pos + Y] += (particles[pos + VY] += particles[pos + AY]);

        // Reset acceleration
        particles[pos + AX] = 0;
        particles[pos + AY] = 0;

        if (REFLECT_ON_BORDER) {
            // Reflection on borders
            if(particles[pos + X] < 0){
                particles[pos + X]  = 0;
                particles[pos + VX] *= -DAMPING;
            } else if(particles[pos + X] > WIDTH){
                particles[pos + X]  = WIDTH;
                particles[pos + VX] *= -DAMPING;
            }

            if(particles[pos + Y] < 0){
                particles[pos + Y]  = 0;
                particles[pos + VY] *= -DAMPING;
            } else if(particles[pos + Y] > HEIGHT){
                particles[pos + Y] = HEIGHT;
                particles[pos + VY] *= -DAMPING;
            }
        } else {
            // No reflection on borders
            if (particles[pos + X] + 1 > WIDTH) {
                particles[pos + X] = 1;
            } else if (particles[pos + X] - 1 < 0) {
                particles[pos + X] = WIDTH - 1;
            }
            if (particles[pos + Y] + 1 > HEIGHT) {
                particles[pos + Y] = 1;
            } else if (particles[pos + Y] - 1 < 0) {
                particles[pos + Y] = HEIGHT - 1;
            }
        }
    }

    private void drawParticle(int pos, final GraphicsContext CTX) {
        //CTX.setFill(Color.WHITE);
        CTX.fillRect(particles[pos], particles[pos + 1], 1, 1);
    }

    private double invSqrt(double x) {
        double xhalf = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i>>1);
        x = Double.longBitsToDouble(i);
        x = x * (1.5d - xhalf * x * x);
        return x;
    }

    @Override public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane();
        pane.getChildren().add(CANVAS);

        Scene scene = new Scene(pane, Color.BLACK);

        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}