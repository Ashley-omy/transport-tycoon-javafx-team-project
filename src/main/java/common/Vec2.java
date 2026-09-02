/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

/**
 *
 * @author asuna
 */

public class Vec2 {

    public double x;
    public double y;

    // Constructor
    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Add another vector
    public Vec2 add(Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    // Subtract another vector
    public Vec2 sub(Vec2 other) {
        return new Vec2(this.x - other.x, this.y - other.y);
    }

    // Multiply by scalar
    public Vec2 mul(double scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }

    // Linear interpolation (for animation)
    public static Vec2 lerp(Vec2 a, Vec2 b, double t) {
        return new Vec2(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t
        );
    }

    @Override
    public String toString() {
        return "Vec2(" + x + ", " + y + ")";
    }
}