using UnityEngine;

public class Vehicle
{
    public int id;

    // Logical tile position (target tile)
    public GridPos tilePos;

    // Animation world positions
    public Vector2 worldFrom;
    public Vector2 worldTo;
    public Vector2 worldPos; // interpolated current render pos

    public Color color;

    public Vehicle(int id, GridPos tilePos, Vector2 worldPos, Color color)
    {
        this.id = id;
        this.tilePos = tilePos;
        this.worldPos = worldPos;
        this.worldFrom = worldPos;
        this.worldTo = worldPos;
        this.color = color;
    }
}