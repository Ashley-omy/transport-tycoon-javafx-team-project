using System.Collections.Generic;

public class UIState
{
    public GridPos? selectedTile;
    public BuildMode buildMode = BuildMode.NONE;

    public GridPos? dragStartTile;
    public GridPos? dragEndTile;
    public List<GridPos> dragPreviewTiles = new List<GridPos>();
    public HashSet<string> roadTiles = new HashSet<string>();

    // New UI/game window state
    public int money = 1000;
    public float gameSpeed = 1f; // 0.5x .. 4x
    public bool isPaused = false;
    public int placementRotation = 0; // for Q/E
}