using TMPro;
using UnityEngine;

public class HUDView : MonoBehaviour
{
    public TextMeshProUGUI selectedTileText;

    private UIState _uiState;

    public void Init(UIState uiState)
    {
        _uiState = uiState;
    }

    private void Update()
    {
        if (_uiState == null) return;

        if (_uiState.selectedTile.HasValue)
            selectedTileText.text = "Selected: " + _uiState.selectedTile.Value;
        else
            selectedTileText.text = "Selected: none";
    }
}