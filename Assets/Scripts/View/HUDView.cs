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

        string selected = _uiState.selectedTile.HasValue ? _uiState.selectedTile.Value.ToString() : "none";
        string drag = (_uiState.dragStartTile.HasValue && _uiState.dragEndTile.HasValue)
            ? $"{_uiState.dragStartTile.Value} -> {_uiState.dragEndTile.Value}"
            : "none";

        string paused = _uiState.isPaused ? "Paused" : "Running";

        selectedTileText.text =
            $"Selected: {selected} | Drag: {drag}\n" +
            $"Mode: {_uiState.buildMode} | Rot: {_uiState.placementRotation}\n" +
            $"Money: ${_uiState.money} | Speed: x{_uiState.gameSpeed:0.0} ({paused})";
    }
}