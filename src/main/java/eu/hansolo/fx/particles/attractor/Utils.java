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
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


/**
 * Created by hansolo on 25.11.16.
 */
public class Utils {

    /**
     * Clamp value between min and max
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static double clamp(double value, double min, double max) {

        if (value < min)
            return min;

        if (value > max)
            return max;

        return value;
    }

    /**
     * Map value of a given range to a target range
     * @param value
     * @param currentRangeStart
     * @param currentRangeStop
     * @param targetRangeStart
     * @param targetRangeStop
     * @return
     */
    public static double map(double value, double currentRangeStart, double currentRangeStop, double targetRangeStart, double targetRangeStop) {
        return targetRangeStart + (targetRangeStop - targetRangeStart) * ((value - currentRangeStart) / (currentRangeStop - currentRangeStart));
    }

    /**
     * Snapshot an image out of a node, consider transparency.
     *
     * @param node
     * @return
     */
    public static Image createImage(Node node) {

        WritableImage wi;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;

    }

    /**
     * Pre-create images with various gradient colors and sizes.
     *
     * @return
     */
    public static Image[] preCreateImages() {

        // get number of images
        int count = (int) Settings.get().getParticleLifeSpanMax();

        // create linear gradient lookup image: lifespan 0 -> lifespan max
        double         width          = count;
        Stop[]         stops          = new Stop[] { new Stop(0, Color.BLACK.deriveColor(1, 1, 1, 0.0)), new Stop(0.3, Color.RED), new Stop(0.9, Color.YELLOW), new Stop(1, Color.WHITE)};
        LinearGradient linearGradient = new LinearGradient(0, 0, width, 0, false, CycleMethod.NO_CYCLE, stops);

        Rectangle rectangle = new Rectangle(width, 1);
        rectangle.setFill( linearGradient);

        Image lookupImage = createImage(rectangle);

        // pre-create images
        Image[] list = new Image[count];

        double radius = Settings.get().getParticleWidth();

        for (int i = 0; i < count; i++) {

            // get color depending on lifespan
            Color color = lookupImage.getPixelReader().getColor( i, 0);

            // create gradient image with given color
            Circle ball = new Circle(radius);

            RadialGradient gradient1 = new RadialGradient(0, 0, 0, 0, radius, false, CycleMethod.NO_CYCLE, new Stop(0, color.deriveColor(1, 1, 1, 1)), new Stop(1, color.deriveColor(1, 1, 1, 0)));

            ball.setFill(gradient1);

            // create image
            list[i] = Utils.createImage(ball);
        }

        return list;
    }
}