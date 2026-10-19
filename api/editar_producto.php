
<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: http://localhost:5173");
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
        throw new InvalidArgumentException("Datos no válidos.");
    }

    $id = filter_var($datos["id"] ?? null, FILTER_VALIDATE_INT);
    $idCategoria = filter_var(
        $datos["id_categoria"] ?? null,
        FILTER_VALIDATE_INT
    );
    $nombre = trim($datos["name"] ?? "");
    $precio = $datos["price"] ?? null;
    $stock = filter_var(
        $datos["stock"] ?? null,
        FILTER_VALIDATE_INT
    );

    if (
        $id === false || $id === null || $id < 1 ||
        $idCategoria === false || $idCategoria === null ||
        $idCategoria < 1 ||
        $nombre === "" || strlen($nombre) > 100 ||
        $precio === null || !is_numeric($precio) ||
        !is_finite((float)$precio) || (float)$precio < 0 ||
        $stock === false || $stock === null || $stock < 0
    ) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "error" => "Verifica los datos ingresados."
        ]);
        exit;
    }

    $categoria = $conexion->prepare(
        "SELECT id_categoria FROM categoria WHERE id_categoria = ?"
    );
    $categoria->execute([$idCategoria]);

    if (!$categoria->fetch()) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "error" => "La categoría no existe."
        ]);
        exit;
    }

    $producto = $conexion->prepare(
        "SELECT id_producto FROM producto
         WHERE id_producto = ? AND activo = 1"
    );
    $producto->execute([$id]);

    if (!$producto->fetch()) {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "error" => "El producto no existe o está inactivo."
        ]);
        exit;
    }

    $sql = "UPDATE producto
            SET nombre = ?,
                id_categoria = ?,
                precio = ?,
                stock = ?
            WHERE id_producto = ?";

    $consulta = $conexion->prepare($sql);
    $consulta->execute([
        $nombre,
        $idCategoria,
        number_format((float)$precio, 2, ".", ""),
        $stock,
        $id
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Producto actualizado correctamente."
    ], JSON_UNESCAPED_UNICODE);

} catch (InvalidArgumentException $e) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "No se pudo actualizar el producto."
    ]);
}
