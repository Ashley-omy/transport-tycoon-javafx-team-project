using System.Collections.Generic;
using UnityEngine;

public class VehicleRenderer : MonoBehaviour
{
    [Header("Refs")]
    public MapView mapView;
    public Camera mainCamera;

    [Header("Visual")]
    public float tileSize = 1f;
    public Vector2 vehicleScale = new Vector2(0.45f, 0.45f);

    private GameMap _map;
    private Sprite _sprite;

    // vehicle id -> GO
    private readonly Dictionary<int, GameObject> _vehicleObjects = new Dictionary<int, GameObject>();

    public void Init(GameMap map)
    {
        _map = map;
        _sprite = Create1x1Sprite();
    }

    private void Update()
    {
        if (_map == null || mapView == null || mainCamera == null) return;

        // visible tile bounds
        mapView.GetVisibleTileBounds(mainCamera, out int minX, out int maxX, out int minY, out int maxY);

        HashSet<int> needed = new HashSet<int>();

        foreach (var v in _map.Vehicles)
        {
            // cull by tile
            if (v.tilePos.x < minX || v.tilePos.x > maxX || v.tilePos.y < minY || v.tilePos.y > maxY)
                continue;

            needed.Add(v.id);

            if (!_vehicleObjects.TryGetValue(v.id, out GameObject go))
            {
                go = CreateVehicleGO(v.id);
                _vehicleObjects[v.id] = go;
            }

            UpdateVehicleGO(go, v);
        }

        // remove hidden/out-of-view vehicle GOs (simple approach)
        List<int> toRemove = new List<int>();
        foreach (var kv in _vehicleObjects)
        {
            if (!needed.Contains(kv.Key))
            {
                Destroy(kv.Value);
                toRemove.Add(kv.Key);
            }
        }
        foreach (var id in toRemove) _vehicleObjects.Remove(id);
    }

    private GameObject CreateVehicleGO(int id)
    {
        GameObject go = new GameObject($"Vehicle_{id}");
        go.transform.SetParent(transform, false);

        SpriteRenderer sr = go.AddComponent<SpriteRenderer>();
        sr.sprite = _sprite;
        sr.sortingOrder = 20; // above entities

        return go;
    }

    private void UpdateVehicleGO(GameObject go, Vehicle v)
    {
        go.name = $"Vehicle_{v.id}";
        go.transform.position = new Vector3(v.worldPos.x * tileSize, v.worldPos.y * tileSize, 0f);
        go.transform.localScale = new Vector3(vehicleScale.x * tileSize, vehicleScale.y * tileSize, 1f);

        var sr = go.GetComponent<SpriteRenderer>();
        if (sr != null) sr.color = v.color;
    }

    private Sprite Create1x1Sprite()
    {
        Texture2D tex = new Texture2D(1, 1);
        tex.SetPixel(0, 0, Color.white);
        tex.Apply();
        return Sprite.Create(tex, new Rect(0, 0, 1, 1), new Vector2(0.5f, 0.5f), 1f);
    }
}