using UnityEngine;

public class GameMap
{
    public int Width { get; private set; }
    public int Height { get; private set; }

    public GameMap(int width, int height)
    {
        Width = width;
        Height = height;
    }

    public bool InBounds(GridPos p)
    {
        return p.x >= 0 && p.y >= 0 && p.x < Width && p.y < Height;
    }
}
