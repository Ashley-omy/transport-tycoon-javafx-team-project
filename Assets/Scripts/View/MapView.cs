using UnityEngine;

public class MapView : MonoBehaviour
{
    public int mapWidth = 30;
    public int mapHeight = 20;
    public float tileSize = 1f;

    private GameMap _map;
    private UIState _uiState;

    public void Init(GameMap map, UIState uiState)
    {
        _map = map;
        _uiState = uiState;
    }

    public GridPos ScreenToTile(Vector3 screenPos, Camera cam)
    {
        Vector3 world = cam.ScreenToWorldPoint(screenPos);
        return WorldToTile(world);
    }

    public GridPos WorldToTile(Vector3 worldPos)
    {
        return new GridPos(
            Mathf.FloorToInt(worldPos.x / tileSize),
            Mathf.FloorToInt(worldPos.y / tileSize)
        );
    }

    public Vector3 TileToWorldCenter(GridPos tile)
    {
        return new Vector3(
            tile.x * tileSize + tileSize * 0.5f,
            tile.y * tileSize + tileSize * 0.5f,
            0f
        );
    }

    public void GetVisibleTileBounds(Camera cam, out int minX, out int maxX, out int minY, out int maxY)
    {
        Vector3 bl = cam.ViewportToWorldPoint(new Vector3(0f, 0f, 0f)); // bottom-left
        Vector3 tr = cam.ViewportToWorldPoint(new Vector3(1f, 1f, 0f)); // top-right

        minX = Mathf.FloorToInt(bl.x / tileSize);
        minY = Mathf.FloorToInt(bl.y / tileSize);
        maxX = Mathf.FloorToInt(tr.x / tileSize);
        maxY = Mathf.FloorToInt(tr.y / tileSize);

        if (_map != null)
        {
            minX = Mathf.Clamp(minX, 0, _map.Width - 1);
            minY = Mathf.Clamp(minY, 0, _map.Height - 1);
            maxX = Mathf.Clamp(maxX, 0, _map.Width - 1);
            maxY = Mathf.Clamp(maxY, 0, _map.Height - 1);
        }
    }
    /* private void OnDrawGizmos()
     {
         if (_map == null) return;

         // draw grid
         Gizmos.color = Color.gray;
         for (int y = 0; y < _map.Height; y++)
         {
             for (int x = 0; x < _map.Width; x++)
             {
                 Vector3 center = new Vector3(x * tileSize + tileSize / 2f, y * tileSize + tileSize / 2f, 0f);
                 Gizmos.DrawWireCube(center, new Vector3(tileSize, tileSize, 0f));
             }
         }

         // draw selected tile highlight
         if (_uiState != null && _uiState.selectedTile.HasValue)
         {
             Gizmos.color = Color.yellow;
             GridPos p = _uiState.selectedTile.Value;
             Gizmos.DrawCube(TileToWorldCenter(p), new Vector3(tileSize * 0.9f, tileSize * 0.9f, 0.01f));
         }

         // Drag preview tiles
         if (_uiState != null && _uiState.dragPreviewTiles != null && _uiState.dragPreviewTiles.Count > 0)
         {
             Gizmos.color = new Color(1f, 0.8f, 0.1f, 0.7f); // yellow/orange
             foreach (var p in _uiState.dragPreviewTiles)
             {
                 Vector3 c = new Vector3(p.x * tileSize + tileSize / 2f, p.y * tileSize + tileSize / 2f, 0f);
                 Gizmos.DrawWireCube(c, new Vector3(tileSize * 0.95f, tileSize * 0.95f, 0f));
             }
         }
     }*/
}