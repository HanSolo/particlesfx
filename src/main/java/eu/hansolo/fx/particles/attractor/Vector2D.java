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

/**
 * Created by hansolo on 25.11.16.
 */
public class Vector2D {

    public double x;
    public double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double magnitude() {
        return (double) Math.sqrt(x * x + y * y);
    }

    public void add(Vector2D v) {
        x += v.x;
        y += v.y;
    }

    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void multiply(double n) {
        x *= n;
        y *= n;
    }

    public void div(double n) {
        x /= n;
        y /= n;
    }

    public void normalize() {
        double m = magnitude();
        if (m != 0 && m != 1) {
            div(m);
        }
    }

    public void limit(double max) {
        if (magnitude() > max) {
            normalize();
            multiply(max);
        }
    }

    public double angle() {
        double angle = (double) Math.atan2(-y, x);
        return -1 * angle;
    }

    static public Vector2D subtract(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x - v2.x, v1.y - v2.y);
    }

}