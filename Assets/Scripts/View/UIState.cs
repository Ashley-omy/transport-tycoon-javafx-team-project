using System.Collections.Generic;
using UnityEngine;

public class UIState
{
    public GridPos? selectedTile;
    public BuildMode buildMode = BuildMode.NONE;

    public GridPos? dragStartTile;
    public GridPos? dragEndTile;

    public List<GridPos> dragPreviewTiles = new List<GridPos>();
}