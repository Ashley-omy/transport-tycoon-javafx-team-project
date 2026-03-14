using UnityEngine;

public class SelectionController
{
    private UIState _uiState;

    public SelectionController(UIState uiState)
    {
        _uiState = uiState;
    }

    public void SelectTile(GridPos pos)
    {
        _uiState.selectedTile = pos;
    }
}