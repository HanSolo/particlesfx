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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


/**
 * Created by hansolo on 25.11.16.
 */
public class Settings {

    // scene settings
    // -------------------------------
    private DoubleProperty        sceneWidth  = new SimpleDoubleProperty(1280);
    private DoubleProperty        sceneHeight = new SimpleDoubleProperty(720);
    private ObjectProperty<Color> sceneColor  = new SimpleObjectProperty<>(Color.BLACK);

    private DoubleProperty toolbarWidth = new SimpleDoubleProperty(250);
    private DoubleProperty canvasWidth = new SimpleDoubleProperty(sceneWidth.doubleValue()-toolbarWidth.doubleValue());
    private DoubleProperty canvasHeight = new SimpleDoubleProperty(sceneHeight.doubleValue());

    // forces
    // -------------------------------
    // number of forces
    private IntegerProperty attractorCount = new SimpleIntegerProperty(1);
    private IntegerProperty repellerCount = new SimpleIntegerProperty(1);

    // just some artificial strength value that matches our needs
    private DoubleProperty repellerStrength = new SimpleDoubleProperty( 500);
    private DoubleProperty attractorStrength = new SimpleDoubleProperty( 500);

    // just some artificial strength value that matches our needs.
    private ObjectProperty<Vector2D> forceGravity = new SimpleObjectProperty<>( new Vector2D(0,0));
    private DoubleProperty gravityX = new SimpleDoubleProperty( forceGravity.getValue().x);
    private DoubleProperty gravityY = new SimpleDoubleProperty( forceGravity.getValue().y);

    // emitter
    // -------------------------------
    private IntegerProperty emitterFrequency = new SimpleIntegerProperty(100); // particles per frame
    private DoubleProperty emitterWidth = new SimpleDoubleProperty(canvasWidth.doubleValue());
    private DoubleProperty emitterLocationY = new SimpleDoubleProperty(sceneHeight.doubleValue() / 2.0);

    // particles
    // -------------------------------
    private DoubleProperty particleWidth = new SimpleDoubleProperty( 5);
    private DoubleProperty particleHeight = new SimpleDoubleProperty( particleWidth.doubleValue());
    private DoubleProperty particleLifeSpanMax = new SimpleDoubleProperty( 256);
    private DoubleProperty particleMaxSpeed = new SimpleDoubleProperty( 4);

    // instance handling
    // ----------------------------------------
    private static Settings settings = new Settings();

    private Settings() {
    }

    /**
     * Return the one instance of this class
     */
    public static Settings get() {
        return settings;
    }



    // user interface
    // ----------------------------------------
    public Node createToolbar() {

        GridPane gp = new GridPane();

        // gridpane layout
        gp.setPrefWidth( Settings.get().getToolbarWidth());

        gp.setHgap(1);
        gp.setVgap(1);
        gp.setPadding(new Insets(8));

        // set column size in percent
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(30);
        gp.getColumnConstraints().add(column);

        column = new ColumnConstraints();
        column.setPercentWidth(70);
        gp.getColumnConstraints().add(column);

        // add components for settings to gridpane
        Slider slider;

        int rowIndex = 0;

        // emitter
        gp.addRow(rowIndex++, createSeparator( "Emitter"));

        slider = createNumberSlider( emitterFrequency, 1, 150);
        gp.addRow(rowIndex++, new Label("Frequency"), slider);

        slider = createNumberSlider( emitterWidth, 0, getCanvasWidth());
        gp.addRow(rowIndex++, new Label("Width"), slider);

        slider = createNumberSlider( emitterLocationY, 0, getCanvasHeight());
        gp.addRow(rowIndex++, new Label("Location Y"), slider);

        // particles
        gp.addRow(rowIndex++, createSeparator( "Particles"));

        slider = createNumberSlider( particleWidth, 1, 60);
        gp.addRow(rowIndex++, new Label("Size"), slider);

        slider = createNumberSlider( particleMaxSpeed, 0, 10);
        gp.addRow(rowIndex++, new Label("Max Speed"), slider);

        // attractors
        gp.addRow(rowIndex++, createSeparator( "Attractors"));

        slider = createNumberSlider( attractorStrength, 0, 2000);
        gp.addRow(rowIndex++, new Label("Strength"), slider);

        // repellers
        gp.addRow(rowIndex++, createSeparator( "Repellers"));

        slider = createNumberSlider( repellerStrength, 0, 2000);
        gp.addRow(rowIndex++, new Label("Strength"), slider);

        // forces
        gp.addRow(rowIndex++, createSeparator( "Forces"));

        // gravity
        // update gravity vector value when gravity value changes
        gravityX.addListener( (ChangeListener<Number>) (observable, oldValue, newValue) -> {
            forceGravity.getValue().set(newValue.doubleValue(),gravityY.doubleValue());
        });
        gravityY.addListener( (ChangeListener<Number>) (observable, oldValue, newValue) -> {
            forceGravity.getValue().set(gravityX.doubleValue(), newValue.doubleValue());
        });

        slider = createNumberSlider( gravityX, -0.5, 0.5);
        gp.addRow(rowIndex++, new Label("Gravity X"), slider);

        slider = createNumberSlider( gravityY, -0.5, 0.5);
        gp.addRow(rowIndex++, new Label("Gravity Y"), slider);

        return gp;
    }

    private Node createSeparator( String text) {

        VBox box = new VBox();

        Label label = new Label( text);
        label.setFont(Font.font(null, FontWeight.BOLD, 14));

        Separator separator = new Separator();

        box.getChildren().addAll(separator, label);

        box.setFillWidth(true);

        GridPane.setColumnSpan(box, 2);

        GridPane.setFillWidth(box, true);
        GridPane.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    private Slider createNumberSlider(Property<Number> observable, double min, double max) {

        Slider slider = new Slider( min, max, observable.getValue().doubleValue());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.valueProperty().bindBidirectional(observable);

        return slider;

    }

    // -------------------------------
    // auto-generated begin
    // -------------------------------
    public final DoubleProperty sceneWidthProperty() {
        return this.sceneWidth;
    }
    public final double getSceneWidth() {
        return this.sceneWidthProperty().get();
    }
    public final void setSceneWidth(final double sceneWidth) {
        this.sceneWidthProperty().set(sceneWidth);
    }
    public final DoubleProperty sceneHeightProperty() {
        return this.sceneHeight;
    }
    public final double getSceneHeight() {
        return this.sceneHeightProperty().get();
    }
    public final void setSceneHeight(final double sceneHeight) {
        this.sceneHeightProperty().set(sceneHeight);
    }
    public final ObjectProperty<Color> sceneColorProperty() {
        return this.sceneColor;
    }
    public final javafx.scene.paint.Color getSceneColor() {
        return this.sceneColorProperty().get();
    }
    public final void setSceneColor(final javafx.scene.paint.Color sceneColor) {
        this.sceneColorProperty().set(sceneColor);
    }
    public final IntegerProperty attractorCountProperty() {
        return this.attractorCount;
    }
    public final int getAttractorCount() {
        return this.attractorCountProperty().get();
    }
    public final void setAttractorCount(final int attractorCount) {
        this.attractorCountProperty().set(attractorCount);
    }
    public final IntegerProperty repellerCountProperty() {
        return this.repellerCount;
    }
    public final int getRepellerCount() {
        return this.repellerCountProperty().get();
    }
    public final void setRepellerCount(final int repellerCount) {
        this.repellerCountProperty().set(repellerCount);
    }
    public final DoubleProperty repellerStrengthProperty() {
        return this.repellerStrength;
    }
    public final double getRepellerStrength() {
        return this.repellerStrengthProperty().get();
    }
    public final void setRepellerStrength(final double repellerStrength) {
        this.repellerStrengthProperty().set(repellerStrength);
    }
    public final ObjectProperty<Vector2D> forceGravityProperty() {
        return this.forceGravity;
    }
    public final Vector2D getForceGravity() {
        return this.forceGravityProperty().get();
    }
    public final void setForceGravity(final Vector2D forceGravity) {
        this.forceGravityProperty().set(forceGravity);
    }
    public final IntegerProperty emitterFrequencyProperty() {
        return this.emitterFrequency;
    }
    public final int getEmitterFrequency() {
        return this.emitterFrequencyProperty().get();
    }
    public final void setEmitterFrequency(final int emitterFrequency) {
        this.emitterFrequencyProperty().set(emitterFrequency);
    }
    public final DoubleProperty emitterWidthProperty() {
        return this.emitterWidth;
    }
    public final double getEmitterWidth() {
        return this.emitterWidthProperty().get();
    }
    public final void setEmitterWidth(final double emitterWidth) {
        this.emitterWidthProperty().set(emitterWidth);
    }
    public final DoubleProperty emitterLocationYProperty() {
        return this.emitterLocationY;
    }
    public final double getEmitterLocationY() {
        return this.emitterLocationYProperty().get();
    }
    public final void setEmitterLocationY(final double emitterLocationY) {
        this.emitterLocationYProperty().set(emitterLocationY);
    }
    public final DoubleProperty particleWidthProperty() {
        return this.particleWidth;
    }
    public final double getParticleWidth() {
        return this.particleWidthProperty().get();
    }
    public final void setParticleWidth(final double particleWidth) {
        this.particleWidthProperty().set(particleWidth);
    }
    public final DoubleProperty particleHeightProperty() {
        return this.particleHeight;
    }
    public final double getParticleHeight() {
        return this.particleHeightProperty().get();
    }
    public final void setParticleHeight(final double particleHeight) {
        this.particleHeightProperty().set(particleHeight);
    }
    public final DoubleProperty particleLifeSpanMaxProperty() {
        return this.particleLifeSpanMax;
    }
    public final double getParticleLifeSpanMax() {
        return this.particleLifeSpanMaxProperty().get();
    }
    public final void setParticleLifeSpanMax(final double particleLifeSpanMax) {
        this.particleLifeSpanMaxProperty().set(particleLifeSpanMax);
    }
    public final DoubleProperty particleMaxSpeedProperty() {
        return this.particleMaxSpeed;
    }
    public final double getParticleMaxSpeed() {
        return this.particleMaxSpeedProperty().get();
    }
    public final void setParticleMaxSpeed(final double particleMaxSpeed) {
        this.particleMaxSpeedProperty().set(particleMaxSpeed);
    }

    public final DoubleProperty toolbarWidthProperty() {
        return this.toolbarWidth;
    }

    public final double getToolbarWidth() {
        return this.toolbarWidthProperty().get();
    }

    public final void setToolbarWidth(final double toolbarWidth) {
        this.toolbarWidthProperty().set(toolbarWidth);
    }

    public final DoubleProperty canvasWidthProperty() {
        return this.canvasWidth;
    }

    public final double getCanvasWidth() {
        return this.canvasWidthProperty().get();
    }

    public final void setCanvasWidth(final double canvasWidth) {
        this.canvasWidthProperty().set(canvasWidth);
    }

    public final DoubleProperty canvasHeightProperty() {
        return this.canvasHeight;
    }

    public final double getCanvasHeight() {
        return this.canvasHeightProperty().get();
    }

    public final void setCanvasHeight(final double canvasHeight) {
        this.canvasHeightProperty().set(canvasHeight);
    }

    public final DoubleProperty attractorStrengthProperty() {
        return this.attractorStrength;
    }

    public final double getAttractorStrength() {
        return this.attractorStrengthProperty().get();
    }

    public final void setAttractorStrength(final double attractorStrength) {
        this.attractorStrengthProperty().set(attractorStrength);
    }

    // -------------------------------
    // auto-generated end
    // -------------------------------



}