package minimarket.data.persistence

import minimarket.data.model.Articulo
import java.io.File
import java.io.RandomAccessFile

/**
 * Capa de persistencia local basada en archivo binario de acceso aleatorio.
 *
 * Mantiene un índice en memoria (HashMap<Int, Long>) que mapea el ID de cada
 * artículo a su offset (posición en bytes) dentro del archivo, permitiendo
 * operaciones de lectura posicional directa con seek() en tiempo O(1).
 *
 * Estrategia de eliminación: se usa "eliminación lógica" sobreescribiendo
 * el campo ID con el valor -1, preservando la integridad del bloque de bytes.
 *
 * @param rutaArchivo Ruta al archivo binario de datos (ej: "data/articulos.dat").
 */
class ArchivoArticulos(private val rutaArchivo: String) {

    /** Índice en memoria: ID del artículo → posición (offset) en el archivo. */
    private val indice: HashMap<Int, Long> = HashMap()

    init {
        // Asegurar que el directorio padre exista
        val parent = File(rutaArchivo).parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        // Cargar índice si el archivo ya existe
        if (File(rutaArchivo).exists()) {
            cargarIndice()
        }
    }

    // =========================================================================
    // GESTIÓN DEL ÍNDICE
    // =========================================================================

    /**
     * Recorre el archivo completo y construye el HashMap de índice,
     * omitiendo registros con ID == -1 (eliminados lógicamente).
     */
    private fun cargarIndice() {
        val raf = RandomAccessFile(rutaArchivo, "r")
        try {
            val fileLength = raf.length()
            var offset: Long = 0

            while (offset < fileLength) {
                raf.seek(offset)
                val articulo = Articulo.readFromFile(raf)

                if (!articulo.isDeleted()) {
                    indice[articulo.id] = offset
                }

                offset += Articulo.RECORD_SIZE
            }
        } finally {
            raf.close()
        }
    }

    // =========================================================================
    // OPERACIONES CRUD
    // =========================================================================

    /**
     * Agrega un nuevo artículo al final del archivo.
     *
     * @param articulo Artículo a registrar.
     * @return true si se registró exitosamente, false si el ID ya existe.
     */
    fun agregar(articulo: Articulo): Boolean {
        if (indice.containsKey(articulo.id)) {
            return false // ID duplicado
        }

        val raf = RandomAccessFile(rutaArchivo, "rw")
        try {
            val offset = raf.length()
            raf.seek(offset)
            articulo.writeToFile(raf)
            indice[articulo.id] = offset
            return true
        } finally {
            raf.close()
        }
    }

    /**
     * Busca un artículo por su ID usando el índice en memoria.
     * Realiza un seek() directo al offset correspondiente → O(1) en disco.
     *
     * @param id Identificador del artículo.
     * @return El artículo encontrado, o null si no existe.
     */
    fun buscar(id: Int): Articulo? {
        val offset = indice[id] ?: return null

        val raf = RandomAccessFile(rutaArchivo, "r")
        try {
            raf.seek(offset)
            return Articulo.readFromFile(raf)
        } finally {
            raf.close()
        }
    }

    /**
     * Lista todos los artículos activos (no eliminados lógicamente).
     *
     * @return Lista inmutable de artículos válidos.
     */
    fun listar(): List<Articulo> {
        val articulos = mutableListOf<Articulo>()

        if (!File(rutaArchivo).exists()) {
            return articulos
        }

        val raf = RandomAccessFile(rutaArchivo, "r")
        try {
            val fileLength = raf.length()
            var offset: Long = 0

            while (offset < fileLength) {
                raf.seek(offset)
                val articulo = Articulo.readFromFile(raf)

                if (!articulo.isDeleted()) {
                    articulos.add(articulo)
                }

                offset += Articulo.RECORD_SIZE
            }
        } finally {
            raf.close()
        }

        return articulos
    }

    /**
     * Actualiza un artículo existente, sobreescribiendo sus datos
     * en la misma posición del archivo (in-place update).
     *
     * @param articulo Artículo con datos actualizados (mismo ID).
     * @return true si se actualizó, false si el ID no existe.
     */
    fun actualizar(articulo: Articulo): Boolean {
        val offset = indice[articulo.id] ?: return false

        val raf = RandomAccessFile(rutaArchivo, "rw")
        try {
            raf.seek(offset)
            articulo.writeToFile(raf)
            return true
        } finally {
            raf.close()
        }
    }

    /**
     * Elimina lógicamente un artículo, sobreescribiendo su ID con -1.
     * El espacio en disco se preserva para mantener la integridad
     * de los offsets de los demás registros.
     *
     * @param id Identificador del artículo a eliminar.
     * @return true si se eliminó, false si el ID no existe.
     */
    fun eliminar(id: Int): Boolean {
        val offset = indice[id] ?: return false

        val raf = RandomAccessFile(rutaArchivo, "rw")
        try {
            raf.seek(offset)
            raf.writeInt(Articulo.DELETED_ID) // Solo sobreescribe los 4 bytes del ID
            indice.remove(id)
            return true
        } finally {
            raf.close()
        }
    }

    /**
     * Retorna la cantidad de artículos activos en el sistema.
     */
    fun cantidad(): Int = indice.size

    /**
     * Verifica si un ID ya está registrado.
     */
    fun existe(id: Int): Boolean = indice.containsKey(id)
}
