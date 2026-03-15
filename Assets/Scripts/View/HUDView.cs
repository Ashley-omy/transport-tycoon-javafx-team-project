using TMPro;
using UnityEngine;

public class HUDView : MonoBehaviour
{
    public TextMeshProUGUI selectedTileText;
    private UIState _uiState;

    public void Init(UIState uiState) => _uiState = uiState;

    private void Update()
    {
        if (_uiState == null || selectedTileText == null) return;

        string selected = _uiState.selectedTile.HasValue ? _uiState.selectedTile.Value.ToString() : "none";
        string drag = (_uiState.dragStartTile.HasValue && _uiState.dragEndTile.HasValue)
            ? $"{_uiState.dragStartTile.Value} -> {_uiState.dragEndTile.Value}"
            : "none";

        selectedTileText.text = $"Selected: {selected} | Mode: {_uiState.buildMode} | Drag: {drag}";
    }
}