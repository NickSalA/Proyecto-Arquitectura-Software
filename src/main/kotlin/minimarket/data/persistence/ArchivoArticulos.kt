package minimarket.data.persistence

import minimarket.data.model.Articulo
import java.io.File
import java.io.RandomAccessFile

class ArchivoArticulos(private val rutaArchivo: String) {

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

    fun cantidad(): Int = indice.size

    fun existe(id: Int): Boolean = indice.containsKey(id)
}
