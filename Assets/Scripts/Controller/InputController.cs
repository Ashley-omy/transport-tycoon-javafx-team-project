
using UnityEngine;

public class InputController : MonoBehaviour
{
    public Camera mainCamera;
    public MapView mapView;

    private SelectionController _selectionController;
    private GameMap _map;
    private UIState _uiState;

    public void Init(SelectionController selectionController, GameMap map, UIState uiState)
    {
        _selectionController = selectionController;
        _map = map;
        _uiState = uiState;
        Debug.Log("InputController Init done");
    }

    private void Update()
    {
        // safety guard
        if (mainCamera == null || mapView == null || _selectionController == null || _map == null || _uiState == null)
            return;

        // Build mode shortcuts
        if (Input.GetKeyDown(KeyCode.Alpha1))
        {
            _uiState.buildMode = BuildMode.ROAD;
            Debug.Log("BuildMode = ROAD");
        }
        if (Input.GetKeyDown(KeyCode.Alpha2))
        {
            _uiState.buildMode = BuildMode.BRIDGE;
            Debug.Log("BuildMode = BRIDGE");
        }
        if (Input.GetKeyDown(KeyCode.Alpha3))
        {
            _uiState.buildMode = BuildMode.STOP;
            Debug.Log("BuildMode = STOP");
        }
        if (Input.GetKeyDown(KeyCode.Alpha4))
        {
            _uiState.buildMode = BuildMode.GARAGE;
            Debug.Log("BuildMode = GARAGE");
        }
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            _uiState.buildMode = BuildMode.NONE;
            Debug.Log("BuildMode = NONE");
        }

        // Tile selection
        if (Input.GetMouseButtonDown(0))
        {
            GridPos tile = mapView.ScreenToTile(Input.mousePosition, mainCamera);

            if (_map.InBounds(tile))
            {
                _selectionController.SelectTile(tile);
                Debug.Log($"Selected tile: {tile}");
            }
        }
    }
}