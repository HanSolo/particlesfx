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

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;


/**
 * Created by hansolo on 07.11.16.
 */
public class Smoke1 extends Canvas {
    private static final Random RND             = new Random();
    private static final Image  IMAGE           = new Image(Smoke.class.getResourceAsStream("smoke2.png"));
    private static final double HALF_WIDTH      = IMAGE.getWidth() * 0.5;
    private static final double HALF_HEIGHT     = IMAGE.getHeight() * 0.5;
    private static final long   GENERATION_RATE = 1_000_000_000l / 100;
    private static final int    NO_OF_PARTICLES = 150;
    private static double          width;
    private static double          height;
    private static boolean         running;
    private        GraphicsContext ctx;
    private        long            lastTimerCall;
    private        AnimationTimer  timer;
    // Parameters for array based particles
    private static final int      NO_OF_FIELDS   = 9; // x, y, vx, vy, opacity, size, life, remaining life, active
    private static final int      ARRAY_LENGTH   = NO_OF_PARTICLES * NO_OF_FIELDS;
    private static final int      X              = 0;
    private static final int      Y              = 1;
    private static final int      VX             = 2;
    private static final int      VY             = 3;
    private static final int      OPACITY        = 4;
    private static final int      SIZE           = 5;
    private static final int      LIFE           = 6;
    private static final int      REMAINING_LIFE = 7;
    private static final int      ACTIVE         = 8;
    private boolean               particlesVisible;
    private boolean               initialized;
    private double[]              particles;


    // ******************** Constructor ***************************************
    public Smoke1() {
        running       = false;
        ctx           = getGraphicsContext2D();
        width         = getWidth();
        height        = getHeight();
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                drawFast();
            }
        };
        widthProperty().addListener((ov, oldWidth, newWidth) -> width = newWidth.doubleValue());
        heightProperty().addListener((ov, oldHeight, newHeight) -> height = newHeight.doubleValue());
        particlesVisible = true;
        initialized        = false;
    }

    public void init() {
        // Initialize particles
        particles = new double[ARRAY_LENGTH];
        int pos   = 0; // next position to insert new particle
        for (int i = 0 ; i < NO_OF_PARTICLES - NO_OF_FIELDS; i++) {
            initParticle(pos);
            pos += NO_OF_FIELDS;
        }
    }


    // ******************** Methods *******************************************
    public void start() {
        if (running) return;
        running = true;
        if (!initialized) init();
        timer.start();
    }

    public void stop() {
        if (!running) return;
        running = false;
        timer.stop();
    }

    private void initParticle(int pos) {
        particles[pos + X]              = RND.nextDouble() * width;
        particles[pos + Y]              = height + HALF_HEIGHT;
        particles[pos + VX]             = (RND.nextDouble() * 2.0) - 1.0;
        particles[pos + VY]             = -(RND.nextDouble() * 3);
        particles[pos + OPACITY]        = 1.0;
        particles[pos + SIZE]           = (RND.nextDouble() * 1.0) + 0.5;
        particles[pos + LIFE]           = (RND.nextDouble() * 20) + 40;
        particles[pos + REMAINING_LIFE] = particles[pos + LIFE];
        particles[pos + ACTIVE]         = 1;
    }

    private void update(int pos) {
        // Update only active particles
        if (particles[pos + ACTIVE] > 0) {
            // Calculate opacity
            particles[pos + OPACITY] = (particles[pos + REMAINING_LIFE] / particles[pos + LIFE] * 0.5);

            // Calculate new pos
            particles[pos + X] += particles[pos + VX];
            particles[pos + Y] += particles[pos + VY];

            // Calculate remaining life
            particles[pos + REMAINING_LIFE]--;

            //regenerate particles
            if(particles[pos + REMAINING_LIFE] < 0 || particles[pos + SIZE] < 0 || particles[pos + OPACITY] < 0.01) {
                if (running) {
                    initParticle(pos);
                } else {
                    if (particles[pos + OPACITY] < 0) {
                        particles[pos + ACTIVE]  = 0;
                    }
                }
            }
        }
    }

    private void drawFast() {
        ctx.clearRect(0, 0, width, height);
        particlesVisible = false;
        for (int pos = 0 ; pos < NO_OF_PARTICLES; pos += NO_OF_FIELDS) {
            // Update particle data
            update(pos);
            if (particles[pos + OPACITY] > 0.01) particlesVisible = true;

            // Draw particle from image
            ctx.save();
            ctx.translate(particles[pos + X], particles[pos + Y]);
            //ctx.scale(particles[pos + SIZE], particles[pos + SIZE]);
            ctx.translate(-HALF_WIDTH, -HALF_HEIGHT);
            ctx.setGlobalAlpha(particles[pos + OPACITY]);
            ctx.drawImage(IMAGE, 0, 0);
            ctx.restore();
        }
        if (!particlesVisible) timer.stop();
    }
}
