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
 * Created by hansolo on 09.10.16.
 */
public class GravityParticles extends Application {
    private static final double          G                 = 6.673e-11; //m^3 / kg * s^2
    private static final Random          RND               = new Random();
    private static final double          WIDTH             = 500;
    private static final double          HEIGHT            = 500;
    private static final double          METER_PER_PIXEL_X = 623_333_333.333333; // 240px == 149.6e6 (distance sun earth)
    private static final double          METER_PER_PIXEL_Y = 623_333_333.333333;
    private static final double          CENTER_X          = WIDTH * 0.5;
    private static final double          CENTER_Y          = HEIGHT * 0.5;
    private static final int             PARTICLE_COUNT    = 2;
    private static final Canvas          CANVAS            = new Canvas(WIDTH, HEIGHT);
    private static final GraphicsContext CTX               = CANVAS.getGraphicsContext2D();
    private static final double          TIME_STEP         = 3600 * 24 * 365;
    private              Particle[]      particles;
    private              long            lastTimerCall;
    private              AnimationTimer  timer;


    // ******************** Constructor ***************************************
    public GravityParticles() {
        particles     = new Particle[PARTICLE_COUNT];
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                if (NOW > lastTimerCall + 1_000) {
                    draw();
                    lastTimerCall = NOW;
                }
            }
        };

        // Initialize with given particles
        Particle earth = new Particle(240, 0, 0, -10, 4, 5.97E24);
        Particle sun   = new Particle(0, 0, 0, 0, 4, 1.989E30);

        particles[0] = sun;
        particles[1] = earth;
    }


    // ******************** Methods *******************************************
    private void draw() {
        CTX.clearRect(0, 0, WIDTH, HEIGHT);
        update();
        for (int i = 0 ; i < PARTICLE_COUNT; i++) { particles[i].draw(); }
        CTX.setFill(Color.WHITE);
    }

    private void update() {
        for (int i = 0 ; i < PARTICLE_COUNT; i++) {
            Particle p = particles[i];

            for(int j = i + 1; j < PARTICLE_COUNT; j++) {
                update(p, particles[j]);
            }
        }
    }

    private void update(final Particle P1, final Particle P2) {
        double r  = getDistanceBetween(P1, P2);                                          // distance between P1 and P2 [m]
        double F  = G * P1.mass * P2.mass / (r * r);                                     // force between P1 and P2    [N == m*kg*s*s]
        double Fx = F * (P2.x - P1.x) / r;                                               // force between P1 and P2 in x-direction [N]
        double Fy = F * (P2.y - P1.y) / r;                                               // force between P1 and P2 in y-direction [N]

        P1.aX = P1.x > CENTER_X ? P1.aX + Fx / P1.mass : P1.aX - Fx / P1.mass;           // acceleration of P2 in x-direction
        P1.aY = P1.y > CENTER_Y ? P1.aY + Fy / P1.mass : P1.aY - Fy / P1.mass;           // acceleration of P2 in y-direction

        P1.vX = P1.x > CENTER_X ? P1.vX - TIME_STEP * P1.aX : P1.vX + TIME_STEP * P1.aX; // velocity of P1 in x-direction
        P1.vY = P1.y > CENTER_Y ? P1.vY - TIME_STEP * P1.aY : P1.vY + TIME_STEP * P1.aY; // velocity of P1 in y-direction

        P1.x  += TIME_STEP * P1.vX / METER_PER_PIXEL_X;                                  // position x of P1
        P1.y  += TIME_STEP * P1.vY / METER_PER_PIXEL_Y;                                  // position y of P1

        P2.aX = P2.x > CENTER_X ? P2.aX + Fx / P2.mass : P2.aX - Fx / P2.mass;           // acceleration of P2 in x-direction
        P2.aY = P2.y > CENTER_Y ? P2.aY + Fy / P2.mass : P2.aY - Fy / P2.mass;           // acceleration of P2 in y-direction

        P2.vX = P2.x > CENTER_X ? P2.vX - TIME_STEP * P2.aX : P2.vX + TIME_STEP * P2.aX; // velocity of P2 in x-direction
        P2.vY = P2.y > CENTER_Y ? P2.vY - TIME_STEP * P2.aY : P2.vY + TIME_STEP * P2.aY; // velocity of P2 in y-direction

        P2.x  += TIME_STEP * P2.vX / METER_PER_PIXEL_X;                                  // position x of P2
        P2.y  += TIME_STEP * P2.vY / METER_PER_PIXEL_Y;                                  // position y of P2
    }

    private double getDistanceBetween(final Particle P1, final Particle P2) {
        double dx       = (P1.x - P2.x) * METER_PER_PIXEL_X;
        double dy       = (P1.y - P2.y) * METER_PER_PIXEL_Y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    @Override public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane(CANVAS);

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
        public double aX;       // [m*s*s]
        public double aY;       // [m*s*s]
        public double vX;       // [m*s]
        public double vY;       // [m*s]
        public double radius;   // [m]
        public double mass;     // [kg]
        public double diameter;
        public Color  color;


        // ******************** Constructor ***********************************
        public Particle() {
            this(RND.nextDouble() * WIDTH, RND.nextDouble() * HEIGHT, 0, 0, 4, RND.nextDouble() * 1_000_000_000);
        }
        public Particle(final double X, final double Y, final double V_X, final double V_Y, final double RADIUS, final double MASS) {
            x        = CENTER_X + X;
            y        = CENTER_Y + Y;
            vX       = V_X;
            vY       = V_Y;
            radius   = RADIUS;     // m
            mass     = MASS;       // kg
            diameter = 2 * radius; // m
            if (mass > 1e30) {
                color = Color.RED;
            } else if (mass > 1e28) {
                color = Color.ORANGE;
            } else if (mass > 1e26) {
                color = Color.YELLOW;
            } else if (mass > 1e24) {
                color = Color.LIME;
            } else if (mass > 1e22) {
                color = Color.CYAN;
            } else if (mass > 1e20) {
                color = Color.BLUE;
            } else {
                color = Color.WHITE;
            }
        }

        public void draw() {
            CTX.setFill(color);
            CTX.fillOval(x - radius, y - radius, diameter, diameter);
        }
    }
}