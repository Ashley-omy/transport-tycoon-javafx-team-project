using UnityEngine;

public class GameMap
{
    public int Width { get; private set; }
    public int Height { get; private set; }

    private TerrainType[,] _terrain;
    private EntityType[,] _entities;

    public GameMap(int width, int height)
    {
        Width = width;
        Height = height;

        _terrain = new TerrainType[Width, Height];
        _entities = new EntityType[Width, Height];

        GenerateSimpleTerrain();
    }

    public bool InBounds(GridPos p)
    {
        return p.x >= 0 && p.y >= 0 && p.x < Width && p.y < Height;
    }

    public TerrainType GetTerrain(GridPos p) => _terrain[p.x, p.y];
    public EntityType GetEntity(GridPos p) => _entities[p.x, p.y];

    public void SetEntity(GridPos p, EntityType entity)
    {
        if (!InBounds(p)) return;
        _entities[p.x, p.y] = entity;
    }

    private void GenerateSimpleTerrain()
    {
        // Simple deterministic pattern for now:
        // left band = water, center = land, some forest stripes
        for (int y = 0; y < Height; y++)
        {
            for (int x = 0; x < Width; x++)
            {
                if (x < Width * 0.2f) _terrain[x, y] = TerrainType.WATER;
                else if ((x + y) % 7 == 0) _terrain[x, y] = TerrainType.FOREST;
                else _terrain[x, y] = TerrainType.LAND;

                _entities[x, y] = EntityType.NONE;
            }
        }
    }
}