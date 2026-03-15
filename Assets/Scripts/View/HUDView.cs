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
        if (_uiState == null || selectedTileText == null) return;

        string tileText = _uiState.selectedTile.HasValue
            ? _uiState.selectedTile.Value.ToString()
            : "none";

        selectedTileText.text = $"Selected: {tileText} | Mode: {_uiState.buildMode}";
    }
}