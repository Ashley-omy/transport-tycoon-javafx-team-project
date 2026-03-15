using System.Collections.Generic;
using UnityEngine;

public class EntityRenderer : MonoBehaviour
{
    [Header("Refs")]
    public MapView mapView;
    public Camera mainCamera;

    [Header("Visuals")]
    public float tileSize = 1f;
    public Color stopColor = new Color(1f, 0.2f, 0.2f, 1f);
    public Color garageColor = new Color(0.65f, 0.2f, 1f, 1f);

    private GameMap _map;
    private UIState _uiState;
    private Sprite _sprite;

    // one entity object per occupied tile key "x,y"
    private readonly Dictionary<string, GameObject> _entityObjects = new Dictionary<string, GameObject>();

    public void Init(GameMap map, UIState uiState)
    {
        _map = map;
        _uiState = uiState;
        _sprite = Create1x1Sprite();
    }

    private void Update()
    {
        if (_map == null || mapView == null || mainCamera == null) return;

        // Visible bounds optimization
        mapView.GetVisibleTileBounds(mainCamera, out int minX, out int maxX, out int minY, out int maxY);

        // Build a set of currently-needed entities in visible area
        HashSet<string> needed = new HashSet<string>();

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = minX; x <= maxX; x++)
            {
                GridPos p = new GridPos(x, y);
                EntityType e = _map.GetEntity(p);
                if (e == EntityType.NONE) continue;

                string key = Key(x, y);
                needed.Add(key);

                if (!_entityObjects.TryGetValue(key, out GameObject go))
                {
                    go = CreateEntityGO(p, e);
                    _entityObjects[key] = go;
                }
                else
                {
                    // entity might have changed type
                    UpdateEntityGO(go, p, e);
                }
            }
        }

        // Remove objects no longer needed (out of view or removed)
        List<string> toRemove = new List<string>();
        foreach (var kvp in _entityObjects)
        {
            if (!needed.Contains(kvp.Key))
            {
                Destroy(kvp.Value);
                toRemove.Add(kvp.Key);
            }
        }
        foreach (var k in toRemove) _entityObjects.Remove(k);
    }

    private GameObject CreateEntityGO(GridPos p, EntityType type)
    {
        GameObject go = new GameObject($"Entity_{type}_{p.x}_{p.y}");
        go.transform.SetParent(transform, false);

        SpriteRenderer sr = go.AddComponent<SpriteRenderer>();
        sr.sprite = _sprite;
        sr.sortingOrder = 10; // above tile layer

        UpdateEntityGO(go, p, type);
        return go;
    }

    private void UpdateEntityGO(GameObject go, GridPos p, EntityType type)
    {
        go.name = $"Entity_{type}_{p.x}_{p.y}";
        go.transform.position = mapView.TileToWorldCenter(p) + new Vector3(0f, 0f, 0f);
        go.transform.localScale = new Vector3(tileSize * 0.65f, tileSize * 0.65f, 1f);

        SpriteRenderer sr = go.GetComponent<SpriteRenderer>();
        if (sr != null) sr.color = EntityColor(type);
    }

    private Color EntityColor(EntityType e)
    {
        switch (e)
        {
            case EntityType.STOP: return stopColor;
            case EntityType.GARAGE: return garageColor;
            default: return Color.clear;
        }
    }

    private string Key(int x, int y) => $"{x},{y}";

    private Sprite Create1x1Sprite()
    {
        Texture2D tex = new Texture2D(1, 1);
        tex.SetPixel(0, 0, Color.white);
        tex.Apply();

        return Sprite.Create(tex, new Rect(0, 0, 1, 1), new Vector2(0.5f, 0.5f), 1f);
    }
}