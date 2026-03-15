using UnityEngine;

public class AnimationEngine : MonoBehaviour
{
    public float tickDuration = 0.6f; // seconds per logic step
    public float moveDuration = 0.5f; // seconds spent interpolating each step

    private GameMap _map;
    private UIState _uiState;
    private MapView _mapView;

    private float _tickTimer = 0f;
    private float _moveTimer = 0f;
    private bool _isAnimatingMove = false;

    public void Init(GameMap map, UIState uiState, MapView mapView)
    {
        _map = map;
        _uiState = uiState;
        _mapView = mapView;
    }

    private void Update()
    {
        if (_map == null || _uiState == null || _mapView == null) return;
        if (_uiState.isPaused) return;

        float dt = Time.deltaTime * Mathf.Max(0.01f, _uiState.gameSpeed);

        _tickTimer += dt;

        // start next logic step
        if (_tickTimer >= tickDuration)
        {
            _tickTimer -= tickDuration;
            BeginNextMoveStep();
        }

        // animate current step
        if (_isAnimatingMove)
        {
            _moveTimer += dt;
            float t = Mathf.Clamp01(_moveTimer / moveDuration);

            foreach (var v in _map.Vehicles)
            {
                v.worldPos = Vector2.Lerp(v.worldFrom, v.worldTo, t);
            }

            if (t >= 1f)
                _isAnimatingMove = false;
        }
    }

    private void BeginNextMoveStep()
    {
        // demo movement: move every vehicle one tile to the right, wrap at map edge
        foreach (var v in _map.Vehicles)
        {
            // from = current rendered position
            v.worldFrom = v.worldPos;

            int nextX = v.tilePos.x + 1;
            if (nextX >= _map.Width) nextX = 0;

            GridPos nextTile = new GridPos(nextX, v.tilePos.y);
            v.tilePos = nextTile;

            // to = center of next tile in "tile units"
            // map uses tile center = x+0.5,y+0.5 (before scale by tileSize in renderer)
            v.worldTo = new Vector2(nextTile.x + 0.5f, nextTile.y + 0.5f);
        }

        _moveTimer = 0f;
        _isAnimatingMove = true;
    }
}