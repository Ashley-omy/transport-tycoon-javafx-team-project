using UnityEngine;

public class GameBootstrap : MonoBehaviour
{
    public MapView mapView;
    public InputController inputController;
    public HUDView hudView;

    private void Start()
    {
        UnityEngine.Debug.Log("GameBootstrap Start called");

        GameMap map = new GameMap(mapView.mapWidth, mapView.mapHeight);
        UIState uiState = new UIState();
        SelectionController selectionController = new SelectionController(uiState);

        mapView.Init(map, uiState);
        inputController.Init(selectionController, map);
        hudView.Init(uiState);
    }
}