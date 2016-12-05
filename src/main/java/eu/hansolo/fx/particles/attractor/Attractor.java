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

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;


/**
 * Created by hansolo on 25.11.16.
 */
public class Attractor extends Sprite {

    double factor = 1.0; // similar to repeller, but with +1 factor

    public Attractor( Vector2D location, Vector2D velocity, Vector2D acceleration, double width, double height) {

        super( location, velocity, acceleration, width, height);

    }

    /**
     * Circle with a label
     */
    @Override
    public Node createView() {

        Group group = new Group();

        double radius = width / 2;

        Circle circle = new Circle(radius);

        circle.setCenterX(radius);
        circle.setCenterY(radius);

        circle.setStroke(Color.RED);
        circle.setFill(Color.RED.deriveColor(1, 1, 1, 0.3));

        group.getChildren().add( circle);

        Text text = new Text("Attractor");
        text.setStroke(Color.RED);
        text.setFill(Color.RED);
        text.setBoundsType(TextBoundsType.VISUAL);

        text.relocate(radius - text.getLayoutBounds().getWidth() / 2, radius - text.getLayoutBounds().getHeight() / 2);

        group.getChildren().add( text);

        return group;
    }

    /**
     * Attraction force
     */
    public Vector2D getForce(Particle particle) {

        // calculate direction of force
        Vector2D dir = Vector2D.subtract(location, particle.location);

        // get distance (constrain distance)
        double distance = dir.magnitude(); // distance between objects
        dir.normalize(); // normalize vector (distance doesn't matter here, we just want this vector for direction)
        distance = Utils.clamp(distance, 5, 1000); // keep distance within a reasonable range

        // calculate magnitude
        double force = factor * Settings.get().getAttractorStrength() / (distance * distance); // repelling force is inversely proportional to distance

        // make a vector out of direction and magnitude
        dir.multiply(force); // get force vector => magnitude * direction

        return dir;

    }

}