using UnityEngine;

namespace View
{
    public class CameraView : MonoBehaviour
    {
        [Header("121: Pan Controls")]
        public float panSpeed = 20f;

        [Header("122: Viewport Logic (Zoom)")]
        public float zoomSpeed = 50f;
        public float minSize = 2f;
        public float maxSize = 20f;

        private UnityEngine.Camera _cam;

        void Start()
        {
            _cam = GetComponent<UnityEngine.Camera>();
            // 122: Setup orthographic viewport
            if (_cam != null) _cam.orthographic = true;
        }

        void Update()
        {
            HandlePan();
            HandleZoom();
        }

        // 121: Pan Logic
        private void HandlePan()
        {
            float x = Input.GetAxis("Horizontal") * panSpeed * Time.deltaTime;
            float y = Input.GetAxis("Vertical") * panSpeed * Time.deltaTime;
            transform.Translate(x, y, 0);
        }

        // 122: Viewport Zoom Logic
        private void HandleZoom()
        {
            float scroll = Input.GetAxis("Mouse ScrollWheel");
            if (scroll != 0 && _cam != null)
            {
                _cam.orthographicSize -= scroll * zoomSpeed * Time.deltaTime;
                _cam.orthographicSize = Mathf.Clamp(_cam.orthographicSize, minSize, maxSize);
            }
        }

        // 123: Tile Conversion
        // converts a screen click into a World Position (Coordinates)
        public Vector3Int GetTargetTile(Vector3 screenPosition)
        {
            Vector3 worldPos = _cam.ScreenToWorldPoint(screenPosition);
            return new Vector3Int(
                Mathf.FloorToInt(worldPos.x + 0.5f),
                Mathf.FloorToInt(worldPos.y + 0.5f),
                0
            );
        }
    }
}
