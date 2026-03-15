using UnityEngine;

public class GameBootstrap : MonoBehaviour
{
    public MapView mapView;
    public InputController inputController;
    public HUDView hudView;
    public TileRenderer tileRenderer;

    private void Start()
    {
        if (mapView == null || inputController == null || hudView == null || tileRenderer == null)
        {
            Debug.LogError("Missing refs in GameBootstrap");
            return;
        }

        GameMap map = new GameMap(mapView.mapWidth, mapView.mapHeight);
        UIState uiState = new UIState();
        SelectionController selectionController = new SelectionController(uiState);

        mapView.Init(map, uiState);
        tileRenderer.tileSize = mapView.tileSize;
        tileRenderer.Init(map, uiState);

        inputController.Init(selectionController, map, uiState);
        hudView.Init(uiState);
    }
}