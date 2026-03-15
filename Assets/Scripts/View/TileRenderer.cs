using UnityEngine;

public class TileRenderer : MonoBehaviour
{
    public float tileSize = 1f;

    public Color landColor = new Color(0.55f, 0.8f, 0.45f, 1f);
    public Color waterColor = new Color(0.35f, 0.6f, 0.9f, 1f);
    public Color forestColor = new Color(0.2f, 0.55f, 0.25f, 1f);

    public Color roadColor = new Color(0.2f, 0.2f, 0.2f, 1f);
    public Color previewColor = new Color(1f, 0.7f, 0.1f, 0.9f);
    public Color selectedColor = new Color(1f, 0.9f, 0.2f, 1f);

    public Color stopColor = new Color(1f, 0.2f, 0.2f, 1f);
    public Color garageColor = new Color(0.65f, 0.2f, 1f, 1f);

    private GameObject[,] _tileObjects;
    private SpriteRenderer[,] _renderers;
    private GameMap _map;
    private UIState _uiState;
    private Sprite _sprite;

    public void Init(GameMap map, UIState uiState)
    {
        _map = map;
        _uiState = uiState;
        _sprite = Create1x1Sprite();
        BuildTiles();
    }

    private void BuildTiles()
    {
        _tileObjects = new GameObject[_map.Width, _map.Height];
        _renderers = new SpriteRenderer[_map.Width, _map.Height];

        for (int y = 0; y < _map.Height; y++)
            for (int x = 0; x < _map.Width; x++)
            {
                var go = new GameObject($"Tile_{x}_{y}");
                go.transform.SetParent(transform, false);
                go.transform.position = new Vector3(x * tileSize + tileSize / 2f, y * tileSize + tileSize / 2f, 0f);
                go.transform.localScale = new Vector3(tileSize, tileSize, 1f);

                var sr = go.AddComponent<SpriteRenderer>();
                sr.sprite = _sprite;
                sr.sortingOrder = 0;

                _tileObjects[x, y] = go;
                _renderers[x, y] = sr;
            }
    }

    private void Update()
    {
        if (_map == null || _uiState == null || _renderers == null) return;

        // 1) Terrain base
        for (int y = 0; y < _map.Height; y++)
            for (int x = 0; x < _map.Width; x++)
            {
                var p = new GridPos(x, y);
                _renderers[x, y].color = TerrainColor(_map.GetTerrain(p));
            }

        // 2) Roads overlay
        for (int y = 0; y < _map.Height; y++)
            for (int x = 0; x < _map.Width; x++)
            {
                if (_uiState.roadTiles.Contains($"{x},{y}"))
                    _renderers[x, y].color = roadColor;
            }

        // 3) Drag preview overlay
        if (_uiState.dragPreviewTiles != null)
        {
            foreach (var p in _uiState.dragPreviewTiles)
            {
                if (_map.InBounds(p))
                    _renderers[p.x, p.y].color = previewColor;
            }
        }

        // 4) Entities overlay
        for (int y = 0; y < _map.Height; y++)
            for (int x = 0; x < _map.Width; x++)
            {
                var p = new GridPos(x, y);
                var entity = _map.GetEntity(p);
                if (entity == EntityType.STOP) _renderers[x, y].color = stopColor;
                else if (entity == EntityType.GARAGE) _renderers[x, y].color = garageColor;
            }

        // 5) Selected tile top
        if (_uiState.selectedTile.HasValue)
        {
            var s = _uiState.selectedTile.Value;
            if (_map.InBounds(s))
                _renderers[s.x, s.y].color = selectedColor;
        }
    }

    private Color TerrainColor(TerrainType t)
    {
        switch (t)
        {
            case TerrainType.WATER: return waterColor;
            case TerrainType.FOREST: return forestColor;
            default: return landColor;
        }
    }

    private Sprite Create1x1Sprite()
    {
        Texture2D tex = new Texture2D(1, 1);
        tex.SetPixel(0, 0, Color.white);
        tex.Apply();
        return Sprite.Create(tex, new Rect(0, 0, 1, 1), new Vector2(0.5f, 0.5f), 1f);
    }
}