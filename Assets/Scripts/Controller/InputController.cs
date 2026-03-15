using System.Collections.Generic;
using UnityEngine;

public class InputController : MonoBehaviour
{
    public Camera mainCamera;
    public MapView mapView;

    private SelectionController _selectionController;
    private GameMap _map;
    private UIState _uiState;

    private bool _isDragging = false;
    private GridPos _dragStartTile;
    private GridPos _dragCurrentTile;
    private string TileKey(GridPos p) => $"{p.x},{p.y}";

    public void Init(SelectionController selectionController, GameMap map, UIState uiState)
    {
        _selectionController = selectionController;
        _map = map;
        _uiState = uiState;
    }

    private void Update()
    {
        if (mainCamera == null || mapView == null || _selectionController == null || _map == null || _uiState == null)
            return;

        HandleBuildModeHotkeys();
        HandleLeftClickAndDrag();
    }

    private void HandleBuildModeHotkeys()
    {
        if (Input.GetKeyDown(KeyCode.Alpha1)) _uiState.buildMode = BuildMode.ROAD;
        if (Input.GetKeyDown(KeyCode.Alpha2)) _uiState.buildMode = BuildMode.BRIDGE;
        if (Input.GetKeyDown(KeyCode.Alpha3)) _uiState.buildMode = BuildMode.STOP;
        if (Input.GetKeyDown(KeyCode.Alpha4)) _uiState.buildMode = BuildMode.GARAGE;
        if (Input.GetKeyDown(KeyCode.Escape)) _uiState.buildMode = BuildMode.NONE;
        if (Input.GetKeyDown(KeyCode.Delete)) _uiState.selectedTile = null;
        if (Input.GetKeyDown(KeyCode.Space)) _uiState.isPaused = !_uiState.isPaused;
        if (Input.GetKeyDown(KeyCode.Q)) _uiState.placementRotation = (_uiState.placementRotation + 270) % 360; // -90
        if (Input.GetKeyDown(KeyCode.E)) _uiState.placementRotation = (_uiState.placementRotation + 90) % 360;
    }

    private void HandleLeftClickAndDrag()
    {
        if (Input.GetMouseButtonDown(0))
        {
            GridPos tile = mapView.ScreenToTile(Input.mousePosition, mainCamera);
            if (!_map.InBounds(tile)) return;

            _selectionController.SelectTile(tile);

            _isDragging = true;
            _dragStartTile = tile;
            _dragCurrentTile = tile;

            _uiState.dragStartTile = tile;
            _uiState.dragEndTile = tile;
            _uiState.dragPreviewTiles.Clear();
            _uiState.dragPreviewTiles.Add(tile);

            Debug.Log($"DragStart at {tile}");
        }

        if (_isDragging && Input.GetMouseButton(0))
        {
            GridPos tile = mapView.ScreenToTile(Input.mousePosition, mainCamera);
            if (!_map.InBounds(tile)) return;

            if (tile.x != _dragCurrentTile.x || tile.y != _dragCurrentTile.y)
            {
                _dragCurrentTile = tile;
                _uiState.dragEndTile = tile;
                _uiState.dragPreviewTiles = BuildStraightPath(_dragStartTile, _dragCurrentTile);
            }
        }

        if (_isDragging && Input.GetMouseButtonUp(0))
        {
            _isDragging = false;
            Debug.Log($"Drag from {_dragStartTile} to {_dragCurrentTile}");

            if (_uiState.buildMode == BuildMode.ROAD)
            {
                foreach (var p in _uiState.dragPreviewTiles)
                    _uiState.roadTiles.Add($"{p.x},{p.y}");

                Debug.Log($"Placed road tiles: {_uiState.dragPreviewTiles.Count}");
            }
            else if (_uiState.buildMode == BuildMode.STOP)
            {
                _map.SetEntity(_dragCurrentTile, EntityType.STOP);
                Debug.Log($"Placed STOP at {_dragCurrentTile}");
            }
            else if (_uiState.buildMode == BuildMode.GARAGE)
            {
                _map.SetEntity(_dragCurrentTile, EntityType.GARAGE);
                Debug.Log($"Placed GARAGE at {_dragCurrentTile}");
            }
        }
    }

    private List<GridPos> BuildStraightPath(GridPos a, GridPos b)
    {
        List<GridPos> path = new List<GridPos>();

        int dx = Mathf.Abs(b.x - a.x);
        int dy = Mathf.Abs(b.y - a.y);

        // force axis-aligned line (for early road/bridge prototype)
        if (dx >= dy)
        {
            int step = a.x <= b.x ? 1 : -1;
            for (int x = a.x; x != b.x + step; x += step)
                path.Add(new GridPos(x, a.y));
        }
        else
        {
            int step = a.y <= b.y ? 1 : -1;
            for (int y = a.y; y != b.y + step; y += step)
                path.Add(new GridPos(a.x, y));
        }

        return path;
    }
}