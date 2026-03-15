
using UnityEngine;

public class GameBootstrap : MonoBehaviour
{
    public MapView mapView;
    public InputController inputController;
    public HUDView hudView;

    private void Start()
    {
        if (mapView == null || inputController == null || hudView == null)
        {
            Debug.LogError("GameBootstrap refs missing. Assign in Inspector.");
            return;
        }

        GameMap map = new GameMap(mapView.mapWidth, mapView.mapHeight);
        UIState uiState = new UIState();
        SelectionController selectionController = new SelectionController(uiState);

        mapView.Init(map, uiState);
        inputController.Init(selectionController, map, uiState); // changed
        hudView.Init(uiState);

        Debug.Log("Bootstrap OK");
    }
}