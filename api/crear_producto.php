
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

    $nombre = trim($datos["name"] ?? "");
    $idCategoria = filter_var(
        $datos["id_categoria"] ?? null,
        FILTER_VALIDATE_INT
    );
    $precio = $datos["price"] ?? null;
    $stock = filter_var(
        $datos["stock"] ?? null,
        FILTER_VALIDATE_INT
    );

    if (
        $nombre === "" ||
        strlen($nombre) > 100 ||
        $idCategoria === false ||
        $idCategoria === null ||
        $precio === null ||
        !is_numeric($precio) ||
        !is_finite((float)$precio) ||
        (float)$precio < 0 ||
        $stock === false ||
        $stock === null ||
        $stock < 0
    ) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "error" => "Verifica el nombre, la categoría, el precio y el stock."
        ]);
        exit;
    }

    $verificar = $conexion->prepare(
        "SELECT id_categoria FROM categoria WHERE id_categoria = ?"
    );
    $verificar->execute([$idCategoria]);

    if (!$verificar->fetch()) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "error" => "La categoría seleccionada no existe."
        ]);
        exit;
    }

    $sql = "INSERT INTO producto
            (id_categoria, nombre, precio, stock, activo)
            VALUES (?, ?, ?, ?, 1)";

    $consulta = $conexion->prepare($sql);
    $consulta->execute([
        $idCategoria,
        $nombre,
        number_format((float)$precio, 2, ".", ""),
        $stock
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Producto registrado correctamente.",
        "id" => (int)$conexion->lastInsertId()
    ], JSON_UNESCAPED_UNICODE);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "No se pudo registrar el producto."
    ]);
}
