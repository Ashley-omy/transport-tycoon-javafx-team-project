
using UnityEngine;

public class InputController : MonoBehaviour
{
    public Camera mainCamera;
    public MapView mapView;

    private SelectionController _selectionController;
    private GameMap _map;
    private UIState _uiState;

    // drag state
    private bool _isDragging = false;
    private GridPos _dragStartTile;
    private GridPos _dragCurrentTile;

    public void Init(SelectionController selectionController, GameMap map, UIState uiState)
    {
        _selectionController = selectionController;
        _map = map;
        _uiState = uiState;
        Debug.Log("InputController Init done");
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
    }

    private void HandleLeftClickAndDrag()
    {
        // mouse down = click + possible drag start
        if (Input.GetMouseButtonDown(0))
        {
            GridPos tile = mapView.ScreenToTile(Input.mousePosition, mainCamera);
            if (!_map.InBounds(tile)) return;

            _selectionController.SelectTile(tile);

            _isDragging = true;
            _dragStartTile = tile;
            _dragCurrentTile = tile;

            InputEvent ev = new InputEvent("DragStart");
            ev.mousePos = Input.mousePosition;
            ev.tilePos = tile;
            ev.mouseButton = 0;
            ev.dragStartTile = tile;
            ev.dragEndTile = tile;

            Debug.Log($"DragStart at {tile}");
        }

        // while held = track drag
        if (_isDragging && Input.GetMouseButton(0))
        {
            GridPos tile = mapView.ScreenToTile(Input.mousePosition, mainCamera);
            if (_map.InBounds(tile))
                _dragCurrentTile = tile;
        }

        // mouse up = drag end
        if (_isDragging && Input.GetMouseButtonUp(0))
        {
            _isDragging = false;

            InputEvent ev = new InputEvent("DragEnd");
            ev.mousePos = Input.mousePosition;
            ev.tilePos = _dragCurrentTile;
            ev.mouseButton = 0;
            ev.dragStartTile = _dragStartTile;
            ev.dragEndTile = _dragCurrentTile;

            Debug.Log($"Drag from {_dragStartTile} to {_dragCurrentTile}");
        }
    }
}