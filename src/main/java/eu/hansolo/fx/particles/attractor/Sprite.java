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

import javafx.scene.Node;
import javafx.scene.layout.Region;


/**
 * Created by hansolo on 25.11.16.
 */
public abstract class Sprite extends Region {

    Vector2D location;
    Vector2D velocity;
    Vector2D acceleration;

    double maxSpeed = Settings.get().getParticleMaxSpeed();
    double radius;

    Node view;

    double width;
    double height;
    double centerX;
    double centerY;

    double angle;

    double lifeSpanMax = Settings.get().getParticleLifeSpanMax() - 1; // -1 because we want [0..255] for an amount of 256; otherwise we'd get DivByZero in the logic; will fix it later
    double lifeSpan = lifeSpanMax;

    public Sprite( Vector2D location, Vector2D velocity, Vector2D acceleration, double width, double height) {


        this.location = location;
        this.velocity = velocity;
        this.acceleration = acceleration;

        this.width = width;
        this.height = height;
        this.centerX = width / 2;
        this.centerY = height / 2;

        this.radius = width / 2;

        this.view = createView();

        setPrefSize(width, height);

        if( this.view != null) {
            getChildren().add( view);
        }

    }

    public abstract Node createView();

    public void applyForce(Vector2D force) {

        acceleration.add(force);

    }

    /**
     * Standard movement method: calculate valocity depending on accumulated acceleration force, then calculate the location.
     * Reset acceleration so that it can be recalculated in the next animation step.
     */
    public void move() {

        // set velocity depending on acceleration
        velocity.add(acceleration);

        // limit velocity to max speed
        velocity.limit(maxSpeed);

        // change location depending on velocity
        location.add(velocity);

        // angle: towards velocity (ie target)
        angle = velocity.angle();

        // clear acceleration
        acceleration.multiply(0);
    }

    /**
     * Update node position
     */
    public void display() {

        // location
        relocate(location.x - centerX, location.y - centerY);

        // rotation
        setRotate(Math.toDegrees( angle));

    }


    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getLocation() {
        return location;
    }

    public void setLocation( double x, double y) {
        location.x = x;
        location.y = y;
    }

    public void setLocationOffset( double x, double y) {
        location.x += x;
        location.y += y;
    }

    public void decreaseLifeSpan() {
    }

    public boolean isDead() {

        if (lifeSpan <= 0.0) {
            return true;
        } else {
            return false;
        }

    }

    public int getLifeSpan() {
        return (int) lifeSpan;
    }

}