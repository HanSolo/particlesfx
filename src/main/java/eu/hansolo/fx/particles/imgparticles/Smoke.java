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

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by hansolo on 07.11.16.
 */
public class Smoke extends Canvas {
    private static final Random              RND             = new Random();
    private static final Image               IMAGE           = new Image(Smoke.class.getResourceAsStream("smoke2.png"));
    private static final double              HALF_WIDTH      = IMAGE.getWidth() * 0.5;
    private static final double              HALF_HEIGHT     = IMAGE.getHeight() * 0.5;
    private static final long                GENERATION_RATE = 1_000_000_000l / 50;
    private static final int                 NO_OF_PARTICLES = 150;
    private static       double              width;
    private static       double              height;
    private static       boolean             running;
    private              GraphicsContext     ctx;
    private              List<ImageParticle> particles;
    private              long                lastTimerCall;
    private              AnimationTimer      timer;


    // ******************** Constructor ***************************************
    public Smoke() {
        running       = false;
        ctx           = getGraphicsContext2D();
        width         = getWidth();
        height        = getHeight();
        particles     = new CopyOnWriteArrayList<>();
        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(final long NOW) {
                if (NOW > lastTimerCall + GENERATION_RATE) {
                    if (running && particles.size() < NO_OF_PARTICLES) particles.add(new ImageParticle());
                    if (particles.isEmpty()) timer.stop();
                    lastTimerCall = NOW;
                }
                draw();
            }
        };

        registerListeners();
    }

    private void registerListeners() {
        widthProperty().addListener((ov, oldWidth, newWidth) -> width = newWidth.doubleValue());
        heightProperty().addListener((ov, oldHeight, newHeight) -> height = newHeight.doubleValue());
    }


    // ******************** Methods *******************************************
    public void start() {
        if (running) return;
        running = true;
        timer.start();
    }

    public void stop() {
        if (!running) return;
        running = false;
    }

    private void draw() {
        //ctx.setGlobalBlendMode(BlendMode.SRC_OVER);
        //ctx.setFill(Color.BLACK);
        //ctx.fillRect(0, 0, width, height);
        ctx.clearRect(0, 0, width, height);

        for (ImageParticle p : particles) {
            p.opacity = p.remainingLife / p.life * 0.5;

            // Draw particle from image
            ctx.save();
            ctx.translate(p.x, p.y);
            ctx.scale(p.size, p.size);
            //ctx.translate(p.image.getWidth() * (-0.5), p.image.getHeight() * (-0.5));
            ctx.translate(-HALF_WIDTH, -HALF_HEIGHT);
            ctx.setGlobalAlpha(p.opacity);
            ctx.drawImage(p.image, 0, 0);
            ctx.restore();

            //p.remainingLife--;
            p.remainingLife *= 0.98;
            //p.size *= 0.99;
            p.x += p.vX;
            p.y += p.vY;

            //regenerate particles
            if (p.remainingLife < 0 || p.size < 0 || p.opacity < 0.01) {
                if (running) {
                    p.reInit();
                } else {
                    particles.remove(p);
                }
            }
        }
    }


    // ******************** InnerClasses **************************************
    private class ImageParticle {
        private double x;
        private double y;
        private double vX;
        private double vY;
        private double opacity;
        private double size;
        private Image  image;
        private double life;
        private double remainingLife;


        // ******************** Constructor ***********************************
        public ImageParticle() {
            // Position
            x = RND.nextDouble() * getWidth();
            y = getHeight() + HALF_HEIGHT;

            // Size
            size = (RND.nextDouble() * 1) + 0.5;

            // Velocity
            vX = (RND.nextDouble() * 0.5) - 0.25;
            vY = -(RND.nextDouble() * 3);

            // Opacity
            opacity = 1.0;

            // Image
            image = IMAGE;

            // Life
            life          = (RND.nextDouble() * 20) + 40;
            remainingLife = life;
        }

        public void reInit() {
            // Position
            x = RND.nextDouble() * getWidth();
            y = getHeight() + HALF_HEIGHT;

            // Size
            size = (RND.nextDouble() * 1) + 0.5;

            // Velocity
            vX = (RND.nextDouble() * 0.5) - 0.25;
            vY = -(RND.nextDouble() * 3);

            // Opacity
            opacity = 1.0;

            // Life
            life          = (RND.nextDouble() * 20) + 40;
            remainingLife = life;
        }
    }
}
