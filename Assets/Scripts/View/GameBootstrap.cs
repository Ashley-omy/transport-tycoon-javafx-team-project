using UnityEngine;

public class GameBootstrap : MonoBehaviour
{
    public MapView mapView;
    public InputController inputController;
    public HUDView hudView;
    public TileRenderer tileRenderer;
    public EntityRenderer entityRenderer;

    private void Start()
    {
        if (mapView == null) { Debug.LogError("mapView missing"); return; }
        if (inputController == null) { Debug.LogError("inputController missing"); return; }
        if (hudView == null) { Debug.LogError("hudView missing"); return; }
        if (tileRenderer == null) { Debug.LogError("tileRenderer missing"); return; }
        if (entityRenderer == null) { Debug.LogError("entityRenderer missing"); return; }

        GameMap map = new GameMap(mapView.mapWidth, mapView.mapHeight);
        UIState uiState = new UIState();
        SelectionController selectionController = new SelectionController(uiState);

        mapView.Init(map, uiState);

        tileRenderer.tileSize = mapView.tileSize;
        tileRenderer.Init(map, uiState);

        entityRenderer.tileSize = mapView.tileSize;
        entityRenderer.Init(map, uiState);

        inputController.Init(selectionController, map, uiState);
        hudView.Init(uiState);
    }
}