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

package eu.hansolo.fx.particles.attractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


/**
 * Created by hansolo on 25.11.16.
 */
public class Main extends Application {

    private static Random random = new Random();

    Canvas canvas;
    GraphicsContext graphicsContext;

    /**
     * Container for canvas and other nodes like attractors and repellers
     */
    Pane layerPane;

    List<Attractor> allAttractors = new ArrayList<>();
    List<Repeller> allRepellers = new ArrayList<>();
    List<Particle> allParticles = new ArrayList<>();

    AnimationTimer animationLoop;

    Scene scene;

    MouseGestures mouseGestures = new MouseGestures();

    /**
     * Container for pre-created images which have color and size depending on
     * the particle's lifespan
     */
    Image[] images;

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();

        canvas = new Canvas(Settings.get().getCanvasWidth(), Settings.get().getCanvasHeight());
        graphicsContext = canvas.getGraphicsContext2D();

        layerPane = new Pane();
        layerPane.getChildren().addAll(canvas);

        canvas.widthProperty().bind(layerPane.widthProperty());
        root.setCenter(layerPane);

        Node toolbar = Settings.get().createToolbar();
        root.setRight(toolbar);

        scene = new Scene(root, Settings.get().getSceneWidth(), Settings.get().getSceneHeight(), Settings.get().getSceneColor());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Particles");
        primaryStage.show();

        // initialize content
        preCreateImages();

        // add content
        prepareObjects();

        // listeners for settings
        addSettingsListeners();

        // add mouse location listener
        addInputListeners();

        // add context menus
        addContextMenu( canvas);

        // run animation loop
        startAnimation();

    }

    private void preCreateImages() {
        this.images = Utils.preCreateImages();
    }

    private void prepareObjects() {

        // add attractors
        for (int i = 0; i < Settings.get().getAttractorCount(); i++) {
            addAttractor();
        }

        // add repellers
        for (int i = 0; i < Settings.get().getRepellerCount(); i++) {
            addRepeller();
        }

    }

    private void startAnimation() {

        // start game
        animationLoop = new AnimationTimer() {

            FpsCounter fpsCounter = new FpsCounter();

            @Override
            public void handle(long now) {

                // update fps
                fpsCounter.update( now);

                // add new particles
                for (int i = 0; i < Settings.get().getEmitterFrequency(); i++) {
                    addParticle();
                }

                // apply force: gravity
                Vector2D forceGravity = Settings.get().getForceGravity();
                allParticles.forEach(sprite -> {
                    sprite.applyForce(forceGravity);
                });

                // apply force: attractor
                for (Attractor attractor: allAttractors) {
                    allParticles.stream().parallel().forEach(sprite -> {
                        Vector2D force = attractor.getForce(sprite);
                        sprite.applyForce(force);
                    });
                }

                // apply force: repeller
                for (Repeller repeller : allRepellers) {
                    allParticles.stream().parallel().forEach(sprite -> {
                        Vector2D force = repeller.getForce(sprite);
                        sprite.applyForce(force);
                    });
                }

                // move sprite: apply acceleration, calculate velocity and location
                allParticles.stream().parallel().forEach(Sprite::move);

                // update in fx scene
                allAttractors.forEach(Sprite::display);
                allRepellers.forEach(Sprite::display);

                // draw all particles on canvas
                // -----------------------------------------
                // graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // TODO: parallel?
                double particleSizeHalf = Settings.get().getParticleWidth() / 2;
                allParticles.stream().forEach(particle -> {

                    Image img = images[particle.getLifeSpan()];
                    graphicsContext.drawImage(img, particle.getLocation().x - particleSizeHalf, particle.getLocation().y - particleSizeHalf);

                });

                // life span of particle
                allParticles.stream().parallel().forEach(Sprite::decreaseLifeSpan);

                // remove all particles that aren't visible anymore
                removeDeadParticles();

                // show number of particles
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.fillText("Particles: " + allParticles.size() + ", fps: " + fpsCounter.getFrameRate(), 1, 10);

            }
        };

        animationLoop.start();

    }

    private void removeDeadParticles() {

        Iterator<Particle> iter = allParticles.iterator();
        while (iter.hasNext()) {

            Particle particle = iter.next();
            if (particle.isDead()) {

                // remove from particle list
                iter.remove();
            }

        }

    }

    private void addParticle() {

        // random location
        double x = Settings.get().getCanvasWidth() / 2 + random.nextDouble() * Settings.get().getEmitterWidth() - Settings.get().getEmitterWidth() / 2;
        double y = Settings.get().getEmitterLocationY();

        // dimensions
        double width = Settings.get().getParticleWidth();
        double height = Settings.get().getParticleHeight();

        // create motion data
        Vector2D location = new Vector2D(x, y);

        double vx = random.nextGaussian() * 0.3;
        double vy = random.nextGaussian() * 0.3 - 1.0;
        Vector2D velocity = new Vector2D(vx, vy);

        Vector2D acceleration = new Vector2D(0, 0);

        // create sprite and add to layer
        Particle sprite = new Particle(location, velocity, acceleration, width, height);

        // register sprite
        allParticles.add(sprite);

    }

    private void addAttractor() {

        // center node
        double x = Settings.get().getCanvasWidth() / 2;
        double y = Settings.get().getCanvasHeight() - Settings.get().getCanvasHeight() / 4;

        // dimensions
        double width = 100;
        double height = 100;

        // create motion data
        Vector2D location = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(0, 0);
        Vector2D acceleration = new Vector2D(0, 0);

        // create sprite and add to layer
        Attractor attractor = new Attractor(location, velocity, acceleration, width, height);

        // register sprite
        allAttractors.add(attractor);

        layerPane.getChildren().add(attractor);

        // allow moving via mouse
        mouseGestures.makeDraggable(attractor);
    }

    private void addRepeller() {

        // center node
        double x = Settings.get().getCanvasWidth() / 2;
        double y = Settings.get().getCanvasHeight() - Settings.get().getCanvasHeight() / 4 + 110;

        // dimensions
        double width = 100;
        double height = 100;

        // create motion data
        Vector2D location = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(0, 0);
        Vector2D acceleration = new Vector2D(0, 0);

        // create sprite and add to layer
        Repeller repeller = new Repeller(location, velocity, acceleration, width, height);

        // register sprite
        allRepellers.add(repeller);

        layerPane.getChildren().add(repeller);

        // allow moving via mouse
        mouseGestures.makeDraggable(repeller);

    }

    private void addInputListeners() {
    }

    private void addSettingsListeners() {

        // particle size
        Settings.get().particleWidthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                preCreateImages();
            }

        });
    }

    public void addContextMenu( Node node) {

        MenuItem menuItem;

        // create context menu
        ContextMenu contextMenu = new ContextMenu();

        // add attractor
        menuItem = new MenuItem("Add Attractor");
        menuItem.setOnAction(e -> addAttractor());
        contextMenu.getItems().add( menuItem);

        // add repeller
        menuItem = new MenuItem("Add Repeller");
        menuItem.setOnAction(e -> addRepeller());
        contextMenu.getItems().add( menuItem);

        // context menu listener
        node.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(node, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Helper class for frame rate calculation
     */
    private static class FpsCounter {

        final long[] frameTimes = new long[100];
        int frameTimeIndex = 0;
        boolean arrayFilled = false;
        double frameRate;

        double decimalsFactor = 1000; // we want 3 decimals

        public void update(long now) {

            long oldFrameTime = frameTimes[frameTimeIndex];
            frameTimes[frameTimeIndex] = now;
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;

            if (frameTimeIndex == 0) {
                arrayFilled = true;
            }

            if (arrayFilled) {

                long elapsedNanos = now - oldFrameTime;
                long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;

            }
        }

        public double getFrameRate() {
            // return frameRate;
            return ((int) (frameRate * decimalsFactor)) / decimalsFactor; // reduce to n decimals
        }
    }
}