using UnityEngine;

public class InputEvent
{
    public string type; // "Click", "DragStart", "DragEnd"
    public Vector2 mousePos;
    public GridPos tilePos;
    public int mouseButton;
    public GridPos dragStartTile;
    public GridPos dragEndTile;

    public InputEvent(string type)
    {
        this.type = type;
    }
}