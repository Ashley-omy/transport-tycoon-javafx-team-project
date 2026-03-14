using UnityEngine;

public class InputController : MonoBehaviour
{
    public Camera mainCamera;
    public MapView mapView;

    private SelectionController _selectionController;
    private GameMap _map;

    public void Init(SelectionController selectionController, GameMap map)
    {
        _selectionController = selectionController;
        _map = map;
    }

    private void Update()
    {
        if (Input.GetMouseButtonDown(0))
        {
            GridPos clickedTile = mapView.ScreenToTile(Input.mousePosition, mainCamera);

            if (_map.InBounds(clickedTile))
            {
                _selectionController.SelectTile(clickedTile);
            }
        }
    }
}