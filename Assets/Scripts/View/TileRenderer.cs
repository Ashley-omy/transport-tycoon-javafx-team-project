using UnityEngine;

public class TileRenderer : MonoBehaviour
{
    public float tileSize = 1f;
    public Color defaultColor = new Color(0.75f, 0.75f, 0.75f, 1f);
    public Color selectedColor = new Color(1f, 0.9f, 0.2f, 1f);
    public Color previewColor = new Color(1f, 0.7f, 0.1f, 0.85f);

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
        {
            for (int x = 0; x < _map.Width; x++)
            {
                var go = new GameObject($"Tile_{x}_{y}");
                go.transform.SetParent(transform, false);
                go.transform.position = new Vector3(x * tileSize + tileSize / 2f, y * tileSize + tileSize / 2f, 0f);
                go.transform.localScale = new Vector3(tileSize, tileSize, 1f);

                var sr = go.AddComponent<SpriteRenderer>();
                sr.sprite = _sprite;
                sr.color = defaultColor;
                sr.sortingOrder = 0;

                _tileObjects[x, y] = go;
                _renderers[x, y] = sr;
            }
        }
    }

    private void Update()
    {
        if (_map == null || _uiState == null || _renderers == null) return;

        // reset colors
        for (int y = 0; y < _map.Height; y++)
            for (int x = 0; x < _map.Width; x++)
                _renderers[x, y].color = defaultColor;

        // preview
        if (_uiState.dragPreviewTiles != null)
        {
            foreach (var p in _uiState.dragPreviewTiles)
            {
                if (_map.InBounds(p))
                    _renderers[p.x, p.y].color = previewColor;
            }
        }

        // selected tile on top
        if (_uiState.selectedTile.HasValue)
        {
            var s = _uiState.selectedTile.Value;
            if (_map.InBounds(s))
                _renderers[s.x, s.y].color = selectedColor;
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