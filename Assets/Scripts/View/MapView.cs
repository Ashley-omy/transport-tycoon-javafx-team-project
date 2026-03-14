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
        int x = Mathf.FloorToInt(world.x / tileSize);
        int y = Mathf.FloorToInt(world.y / tileSize);
        return new GridPos(x, y);
    }

    public Vector3 TileToWorldCenter(GridPos p)
    {
        return new Vector3(
            p.x * tileSize + tileSize / 2f,
            p.y * tileSize + tileSize / 2f,
            0f
        );
    }

    private void OnDrawGizmos()
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
    }
}