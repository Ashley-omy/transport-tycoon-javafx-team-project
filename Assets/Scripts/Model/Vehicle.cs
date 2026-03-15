using UnityEngine;

public class Vehicle
{
    public int id;
    public Vector2 worldPos;   // current world position (for now)
    public GridPos tilePos;    // logical tile
    public Color color;

    public Vehicle(int id, GridPos tilePos, Vector2 worldPos, Color color)
    {
        this.id = id;
        this.tilePos = tilePos;
        this.worldPos = worldPos;
        this.color = color;
    }
}