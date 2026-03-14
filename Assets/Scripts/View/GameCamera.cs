using UnityEngine;

public class GameCamera : MonoBehaviour
{
    public float wasdSpeed = 12f;
    public float dragSpeed = 0.02f;

    private Vector3 _lastMousePos;

    private void Update()
    {
        float h = Input.GetAxisRaw("Horizontal"); // A/D
        float v = Input.GetAxisRaw("Vertical");   // W/S
        transform.position += new Vector3(h, v, 0f) * wasdSpeed * Time.deltaTime;

        if (Input.GetMouseButtonDown(2))
            _lastMousePos = Input.mousePosition;

        if (Input.GetMouseButton(2))
        {
            Vector3 delta = Input.mousePosition - _lastMousePos;
            transform.position -= new Vector3(delta.x, delta.y, 0f) * dragSpeed;
            _lastMousePos = Input.mousePosition;
        }
    }
}