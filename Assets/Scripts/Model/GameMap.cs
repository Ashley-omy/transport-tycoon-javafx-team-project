using System.Collections.Generic;
using UnityEngine;

public class GameMap
{
    public int Width { get; private set; }
    public int Height { get; private set; }

    private TerrainType[,] _terrain;
    private EntityType[,] _entities;

    public List<Vehicle> Vehicles { get; private set; } = new List<Vehicle>();

    public GameMap(int width, int height)
    {
        Width = width;
        Height = height;

        _terrain = new TerrainType[Width, Height];
        _entities = new EntityType[Width, Height];

        GenerateSimpleTerrain();

        // demo vehicle
        var start = new GridPos(Mathf.Max(1, Width / 2), Mathf.Max(1, Height / 2));
        AddVehicle(new Vehicle(
            id: 1,
            tilePos: start,
            worldPos: new Vector2(start.x + 0.5f, start.y + 0.5f),
            color: Color.cyan
        ));
    }

    public void AddVehicle(Vehicle v)
    {
        Vehicles.Add(v);
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