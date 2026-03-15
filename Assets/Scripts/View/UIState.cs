using System.Collections.Generic;

public class UIState
{
    public GridPos? selectedTile;
    public BuildMode buildMode = BuildMode.NONE;

    public bool isPaused = false;
    public int placementRotation = 0;

    public GridPos? dragStartTile;
    public GridPos? dragEndTile;
    public List<GridPos> dragPreviewTiles = new List<GridPos>();

    // MVP road storage
    public HashSet<string> roadTiles = new HashSet<string>();
}