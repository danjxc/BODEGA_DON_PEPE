
import { useEffect, useState } from "react";
import "./App.css";

const API = `http://${window.location.hostname}/bodega-api/`;

export default function App() {
  const [vista, setVista] = useState("Inicio");
  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [editando, setEditando] = useState(null);
  const [busqueda, setBusqueda] = useState("");
const [filtroCategoria, setFiltroCategoria] = useState("Todas");

  const formularioInicial = {
    name: "",
    id_categoria: "",
    price: "",
    stock: "",
  };

  const [formulario, setFormulario] = useState(formularioInicial);

  async function cargarProductos() {
    try {
      const respuesta = await fetch(`${API}productos.php`);
      const datos = await respuesta.json();

      if (!respuesta.ok || !datos.success) {
        throw new Error(datos.error || "No se pudieron cargar los productos.");
      }

      setProductos(datos.productos || []);
    } catch (e) {
      setError(e.message || "No se pudo conectar con el servidor.");
    } finally {
      setCargando(false);
    }
  }
  useEffect(() => {
    let activo = true;
  
    async function cargarDatosIniciales() {
      try {
        const [respuestaProductos, respuestaCategorias] = await Promise.all([
          fetch(`${API}productos.php`),
          fetch(`${API}categorias.php`),
        ]);
  
        const [datosProductos, datosCategorias] = await Promise.all([
          respuestaProductos.json(),
          respuestaCategorias.json(),
        ]);
  
        if (!respuestaProductos.ok || !datosProductos.success) {
          throw new Error(
            datosProductos.error || "No se pudieron cargar los productos."
          );
        }
  
        if (!respuestaCategorias.ok || !datosCategorias.success) {
          throw new Error(
            datosCategorias.error || "No se pudieron cargar las categorías."
          );
        }
  
        if (activo) {
          setProductos(datosProductos.productos || []);
          setCategorias(datosCategorias.categorias || []);
          setError("");
        }
      } catch (e) {
        if (activo) {
          setError(e.message || "No se pudieron cargar los datos iniciales.");
        }
      } finally {
        if (activo) {
          setCargando(false);
        }
      }
    }
  
    cargarDatosIniciales();
  
    return () => {
      activo = false;
    };
  }, []);

  function cambiarCampo(e) {
    const { name, value } = e.target;
    setFormulario((anterior) => ({ ...anterior, [name]: value }));
  }

  function limpiarFormulario() {
    setFormulario(formularioInicial);
    setEditando(null);
  }

  function abrirEdicion(producto) {
    const categoria = categorias.find(
      (c) => c.nombre === producto.category
    );

    setEditando(producto);
    setFormulario({
      name: producto.name || "",
      id_categoria: categoria ? String(categoria.id_categoria) : "",
      price: String(producto.price ?? ""),
      stock: String(producto.stock ?? ""),
    });
    setMensaje("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function guardarProducto(e) {
    e.preventDefault();
    setMensaje("");
    setError("");

    if (!formulario.id_categoria) {
      setError("Selecciona una categoría.");
      return;
    }

    if (
      !formulario.name.trim() ||
      formulario.price === "" ||
      formulario.stock === "" ||
      Number(formulario.price) < 0 ||
      Number(formulario.stock) < 0 ||
      !Number.isFinite(Number(formulario.price)) ||
      !Number.isInteger(Number(formulario.stock))
    ) {
      setError("Verifica el nombre, el precio y el stock.");
      return;
    }

    setGuardando(true);

    try {
      const editandoProducto = Boolean(editando);
      const archivo = editandoProducto
        ? "editar_producto.php"
        : "crear_producto.php";

      const cuerpo = {
        ...formulario,
        name: formulario.name.trim(),
        id_categoria: Number(formulario.id_categoria),
        price: Number(formulario.price),
        stock: Number(formulario.stock),
      };

      if (editandoProducto) {
        cuerpo.id = editando.id;
      }

      const respuesta = await fetch(`${API}${archivo}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cuerpo),
      });

      const datos = await respuesta.json();

      if (!respuesta.ok || !datos.success) {
        throw new Error(datos.error || "No se pudo guardar el producto.");
      }

      limpiarFormulario();
      await cargarProductos();
      setMensaje(
        editandoProducto
          ? "Producto actualizado correctamente."
          : "Producto registrado correctamente."
      );
    } catch (e) {
      setError(e.message || "Ocurrió un error al guardar.");
    } finally {
      setGuardando(false);
    }
  }

  async function eliminarProducto(producto) {
    const confirmar = window.confirm(
      `¿Estás segura de que deseas eliminar "${producto.name}" del catálogo?\\n\\nEl producto dejará de mostrarse, pero su registro se conservará para proteger el historial de ventas.`
    );

    if (!confirmar) return;

    setMensaje("");
    setError("");

    try {
      const respuesta = await fetch(`${API}eliminar_producto.php`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id: producto.id }),
      });

      const datos = await respuesta.json();

      if (!respuesta.ok || !datos.success) {
        throw new Error(datos.error || "No se pudo eliminar el producto.");
      }

      if (editando?.id === producto.id) {
        limpiarFormulario();
      }

      await cargarProductos();
      setMensaje(`"${producto.name}" se eliminó del catálogo correctamente.`);
    } catch (e) {
      setError(e.message || "Ocurrió un error al eliminar el producto.");
    }
  }

  const totalUnidades = productos.reduce(
    (total, producto) => total + Number(producto.stock || 0),
    0
  );
  const productosFiltrados = productos.filter((producto) => {
    const nombre = (producto.name || "").toLowerCase();
    const termino = busqueda.trim().toLowerCase();
  
    const coincideNombre = nombre.includes(termino);
    const coincideCategoria =
      filtroCategoria === "Todas" ||
      producto.category === filtroCategoria;
  
    return coincideNombre && coincideCategoria;
  });

  return (
    <div className="app-container">
      <aside className="sidebar">
        <h2>BODEGA</h2>
        <p className="sidebar-subtitle">DON PEPE</p>

        {["Inicio", "Productos", "Categorías", "Perfil"].map((item) => (
          <button
            key={item}
            className={`nav-button ${vista === item ? "active" : ""}`}
            onClick={() => {
              setVista(item);
              setMensaje("");
              setError("");
              if (item !== "Productos") limpiarFormulario();
            }}
          >
            {item === "Inicio" && "⌂"}
            {item === "Productos" && "▣"}
            {item === "Categorías" && "▤"}
            {item === "Perfil" && "◉"}
            {"  "}{item}
          </button>
        ))}
      </aside>

      <main className="main-content">
        <header className="page-header">
          <div>
            <h1>{vista}</h1>
            <p>Panel de administración de Bodega Don Pepe</p>
          </div>
        </header>

        {mensaje && <div className="success-message">{mensaje}</div>}
        {error && <div className="error-message">{error}</div>}

        
{vista === "Inicio" && (
  <section className="home-dashboard">
    <div className="welcome-hero">
      <div className="welcome-copy">
        <span className="eyebrow">
          <span className="status-dot"></span>
          PANEL DE CONTROL
        </span>

        <h2>Calidad y confianza<br />en cada compra.</h2>

        <p>
          Administra tus productos y mantén el inventario de
          Bodega Don Pepe bajo control.
        </p>

        <button
          className="hero-button"
          onClick={() => setVista("Productos")}
        >
          Administrar inventario <span>↗</span>
        </button>
      </div>

      <div className="hero-decoration" aria-hidden="true">
        <div className="hero-circle circle-one"></div>
        <div className="hero-circle circle-two"></div>
        <div className="hero-emblem">
          <span className="emblem-leaf">✳</span>
          <span className="emblem-name">DON PEPE</span>
          <span className="emblem-caption">CALIDAD Y CONFIANZA</span>
        </div>
        <span className="hero-spark spark-one">✦</span>
        <span className="hero-spark spark-two">✧</span>
      </div>
    </div>

    <div className="dashboard-section-title">
      <div>
        <span className="section-kicker">VISTA GENERAL</span>
        <h2>Resumen del inventario</h2>
        <p>El estado actual de tu bodega, de un vistazo.</p>
      </div>
      <span className="live-label">
        <span className="status-dot"></span>
        Datos actuales
      </span>
    </div>

    <div className="dashboard-cards premium-stats">
      <div className="dashboard-card premium-stat">
        <div className="stat-top">
          <span className="stat-icon green-icon">▤</span>
          <span className="stat-caption">CATÁLOGO</span>
        </div>
        <p>Productos activos</p>
        <div className="stat-number-row">
          <h2>{productos.length}</h2>
          <span className="stat-unit">productos</span>
        </div>
        <div className="stat-footer">
          <span className="mini-indicator"></span>
          Productos disponibles en el catálogo
        </div>
      </div>

      <div className="dashboard-card premium-stat">
        <div className="stat-top">
          <span className="stat-icon sand-icon">▦</span>
          <span className="stat-caption">ORGANIZACIÓN</span>
        </div>
        <p>Categorías</p>
        <div className="stat-number-row">
          <h2>{categorias.length}</h2>
          <span className="stat-unit">categorías</span>
        </div>
        <div className="stat-footer">
          <span className="mini-indicator sand-indicator"></span>
          Grupos para organizar tus productos
        </div>
      </div>

      <div className="dashboard-card premium-stat">
        <div className="stat-top">
          <span className="stat-icon blue-icon">▥</span>
          <span className="stat-caption">EXISTENCIAS</span>
        </div>
        <p>Unidades en stock</p>
        <div className="stat-number-row">
          <h2>{totalUnidades.toLocaleString("es-PE")}</h2>
          <span className="stat-unit">unidades</span>
        </div>
        <div className="stat-footer">
          <span className="mini-indicator blue-indicator"></span>
          Suma de existencias registradas
        </div>
      </div>
    </div>

    <div className="dashboard-lower">
      <div className="dashboard-panel inventory-panel">
        <div className="panel-heading">
          <div>
            <span className="section-kicker">SEGUIMIENTO</span>
            <h3>Estado del inventario</h3>
          </div>
          <span className="panel-symbol">⌁</span>
        </div>

        <div className="inventory-highlight">
          <div>
            <span className="inventory-label">Unidades registradas</span>
            <strong>{totalUnidades.toLocaleString("es-PE")}</strong>
            <span className="inventory-note">
              Distribuidas en {productos.length} productos activos
            </span>
          </div>
          <div className="inventory-illustration" aria-hidden="true">
            <span>▤</span>
          </div>
        </div>

        <div className="inventory-breakdown">
          <div className="breakdown-item">
            <span className="breakdown-dot available-dot"></span>
            <span>Productos activos</span>
            <strong>{productos.length}</strong>
          </div>
          <div className="breakdown-item">
            <span className="breakdown-dot category-dot"></span>
            <span>Categorías registradas</span>
            <strong>{categorias.length}</strong>
          </div>
          <div className="breakdown-item">
            <span className="breakdown-dot attention-dot"></span>
            <span>Productos con stock bajo (menos de 5)</span>
            <strong>
              {productos.filter((p) => Number(p.stock) < 5).length}
            </strong>
          </div>
        </div>

        <button
          className="panel-link"
          onClick={() => setVista("Productos")}
        >
          Ver todos los productos <span>→</span>
        </button>
      </div>

      <div className="dashboard-panel recent-panel">
        <div className="panel-heading">
          <div>
            <span className="section-kicker">CATÁLOGO</span>
            <h3>Productos registrados</h3>
          </div>
          <button
            className="text-action"
            onClick={() => setVista("Productos")}
          >
            Ver todos ↗
          </button>
        </div>

        {cargando ? (
          <p className="empty-state">Cargando productos...</p>
        ) : productos.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">▤</span>
            <p>Aún no hay productos activos.</p>
            <button
              className="panel-link"
              onClick={() => setVista("Productos")}
            >
              Registrar primer producto →
            </button>
          </div>
        ) : (
          <div className="recent-list">
            {productos.slice(0, 5).map((producto) => (
              <div className="recent-product" key={producto.id}>
                <div className="product-initial">
                  {(producto.name || "P").charAt(0).toUpperCase()}
                </div>
                <div className="recent-product-info">
                  <strong>{producto.name}</strong>
                  <span>{producto.category}</span>
                </div>
                <div className="recent-product-stock">
                  <strong>
                    {Number(producto.stock).toLocaleString("es-PE")}
                  </strong>
                  <span>unidades</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>

    <div className="dashboard-note">
      <span className="note-icon">✳</span>
      <div>
        <strong>Un inventario bien organizado comienza aquí.</strong>
        <p>
          Mantén actualizados los precios y las existencias para
          llevar un mejor control de tus productos.
        </p>
      </div>
    </div>
  </section>
)}


        {vista === "Productos" && (
          <section>
            <div className="section-heading">
              <div>
                <h2>Gestión de productos</h2>
                <p>Registra, edita y elimina productos del catálogo.</p>
              </div>
            </div>

            <form className="product-form" onSubmit={guardarProducto}>
              <h3>{editando ? "Editar producto" : "Registrar producto"}</h3>

              <label htmlFor="name">Nombre del producto</label>
              <input
                id="name"
                name="name"
                value={formulario.name}
                onChange={cambiarCampo}
                placeholder="Ej. Arroz extra"
                maxLength={100}
                required
              />

              <label htmlFor="id_categoria">Categoría</label>
              <select
                id="id_categoria"
                name="id_categoria"
                value={formulario.id_categoria}
                onChange={cambiarCampo}
                required
              >
                <option value="">Selecciona una categoría</option>
                {categorias.map((categoria) => (
                  <option
                    key={categoria.id_categoria}
                    value={categoria.id_categoria}
                  >
                    {categoria.nombre}
                  </option>
                ))}
              </select>

              <div className="form-row">
                <div>
                  <label htmlFor="price">Precio (S/)</label>
                  <input
                    id="price"
                    name="price"
                    type="number"
                    min="0"
                    step="0.01"
                    value={formulario.price}
                    onChange={cambiarCampo}
                    placeholder="0.00"
                    required
                  />
                </div>

                <div>
                  <label htmlFor="stock">Stock</label>
                  <input
                    id="stock"
                    name="stock"
                    type="number"
                    min="0"
                    step="1"
                    value={formulario.stock}
                    onChange={cambiarCampo}
                    placeholder="0"
                    required
                  />
                </div>
              </div>

              <div className="form-actions">
                <button
                  className="primary-button"
                  type="submit"
                  disabled={guardando}
                >
                  {guardando
                    ? "Guardando..."
                    : editando
                    ? "Guardar cambios"
                    : "Registrar producto"}
                </button>

                {editando && (
                  <button
                    className="secondary-button"
                    type="button"
                    onClick={limpiarFormulario}
                  >
                    Cancelar edición
                  </button>
                )}
              </div>
            </form>

           
<div className="table-container">
  <div className="products-table-header">
    <div>
      <span className="section-kicker">INVENTARIO</span>
      <h3>Productos registrados</h3>
      <p>Consulta y administra tus productos fácilmente.</p>
    </div>

    <span className="product-count">
      {productosFiltrados.length} de {productos.length} productos
    </span>
  </div>

  <div className="products-toolbar">
    <div className="search-field">
      <span className="search-symbol">⌕</span>
      <input
        type="search"
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
        placeholder="Buscar producto por nombre..."
        aria-label="Buscar producto por nombre"
      />
      {busqueda && (
        <button
          type="button"
          className="clear-search"
          onClick={() => setBusqueda("")}
          aria-label="Limpiar búsqueda"
        >
          ×
        </button>
      )}
    </div>

    <select
      className="category-filter"
      value={filtroCategoria}
      onChange={(e) => setFiltroCategoria(e.target.value)}
      aria-label="Filtrar por categoría"
    >
      <option value="Todas">Todas las categorías</option>
      {categorias.map((categoria) => (
        <option key={categoria.id_categoria} value={categoria.nombre}>
          {categoria.nombre}
        </option>
      ))}
    </select>
  </div>


              {cargando ? (
                <p>Cargando productos...</p>
              ) : productosFiltrados.length === 0 ? (
                <p>
  {productos.length === 0
    ? "Todavía no hay productos activos registrados."
    : "No encontramos productos con esos criterios. Prueba otra búsqueda o categoría."}
</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Producto</th>
                      <th>Categoría</th>
                      <th>Precio</th>
                      <th>Stock</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                  {productosFiltrados.map((producto) => (
                      <tr key={producto.id}>
                        <td>{producto.id}</td>
                        <td>{producto.name}</td>
                        <td>{producto.category}</td>
                        <td>S/ {Number(producto.price).toFixed(2)}</td>
                        <td>
  <span
    className={`stock-badge ${
      Number(producto.stock) < 5
        ? "stock-low"
        : Number(producto.stock) < 10
        ? "stock-medium"
        : "stock-normal"
    }`}
  >
    {producto.stock} unidades
  </span>
</td>
                        <td>
                          <div className="action-buttons">
                            <button
                              className="edit-button"
                              type="button"
                              onClick={() => abrirEdicion(producto)}
                            >
                              Editar
                            </button>
                            <button
                              className="delete-button"
                              type="button"
                              onClick={() => eliminarProducto(producto)}
                            >
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </section>
        )}

        {vista === "Categorías" && (
          <section className="table-container">
            <h2>Categorías registradas</h2>
            {categorias.length === 0 ? (
              <p>No se encontraron categorías.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nombre de la categoría</th>
                  </tr>
                </thead>
                <tbody>
                  {categorias.map((categoria) => (
                    <tr key={categoria.id_categoria}>
                      <td>{categoria.id_categoria}</td>
                      <td>{categoria.nombre}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        )}

        {vista === "Perfil" && (
          <section className="dashboard-card">
            <h2>Bodega Don Pepe</h2>
            <p>Panel de administración del inventario.</p>
            <p>Desde esta aplicación puedes consultar y gestionar los productos activos.</p>
          </section>
        )}
      </main>
    </div>
  );
}
