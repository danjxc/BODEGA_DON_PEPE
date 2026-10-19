
<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(204);
    exit;
}
header("Access-Control-Allow-Methods: GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(204);
    exit;
}

require_once __DIR__ . "/db.php";

try {
    $sql = "SELECT
                p.id_producto AS id,
                p.nombre AS name,
                c.nombre AS category,
                p.precio AS price,
                p.stock AS stock
            FROM producto p
            INNER JOIN categoria c
                ON p.id_categoria = c.id_categoria
            WHERE p.activo = 1
            ORDER BY p.id_producto ASC";

    $consulta = $conexion->query($sql);
    $productos = $consulta->fetchAll();

    foreach ($productos as &$producto) {
        $producto["price"] = (float) $producto["price"];
        $producto["stock"] = (int) $producto["stock"];
        $producto["id"] = (int) $producto["id"];
    }
    unset($producto);

    echo json_encode([
        "success" => true,
        "total" => count($productos),
        "productos" => $productos
    ], JSON_UNESCAPED_UNICODE);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "No se pudieron consultar los productos."
    ]);
}
