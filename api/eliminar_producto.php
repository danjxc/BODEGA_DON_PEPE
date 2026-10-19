
<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(204);
    exit;
}
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(204);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode([
        "success" => false,
        "error" => "Método no permitido."
    ]);
    exit;
}

require_once __DIR__ . "/db.php";

try {
    $datos = json_decode(file_get_contents("php://input"), true);

    if (!is_array($datos)) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "error" => "Los datos enviados no son válidos."
        ]);
        exit;
    }

    $id = filter_var(
        $datos["id"] ?? null,
        FILTER_VALIDATE_INT
    );

    if ($id === false || $id === null || $id < 1) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "error" => "El ID del producto no es válido."
        ]);
        exit;
    }

    // Verificar que el producto exista y esté activo.
    $consulta = $conexion->prepare(
        "SELECT id_producto
         FROM producto
         WHERE id_producto = ? AND activo = 1"
    );

    $consulta->execute([$id]);

    if (!$consulta->fetch()) {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "error" => "El producto no existe o ya fue eliminado."
        ]);
        exit;
    }

    // Eliminación lógica: conserva el registro en la base de datos.
    $consulta = $conexion->prepare(
        "UPDATE producto
         SET activo = 0
         WHERE id_producto = ? AND activo = 1"
    );

    $consulta->execute([$id]);

    echo json_encode([
        "success" => true,
        "message" => "Producto eliminado correctamente."
    ], JSON_UNESCAPED_UNICODE);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "No se pudo eliminar el producto."
    ], JSON_UNESCAPED_UNICODE);
}
