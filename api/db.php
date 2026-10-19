
<?php
header("Content-Type: application/json; charset=UTF-8");

$host = getenv("DB_HOST") ?: "localhost";
$dbname = getenv("DB_NAME") ?: "bodega_don_pepe";
$username = getenv("DB_USER") ?: "root";
$password = getenv("DB_PASSWORD") ?: "";

try {
    $conexion = new PDO(
        "mysql:host=$host;dbname=$dbname;charset=utf8mb4",
        $username,
        $password,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]
    );
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "error" => "No se pudo conectar con la base de datos."
    ]);
    exit;
}
