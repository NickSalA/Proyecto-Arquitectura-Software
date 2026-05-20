package minimarket.data.model

import java.io.RandomAccessFile

/**
 * Entidad inmutable que representa un artículo del inventario.
 *
 * Diseño de registro de longitud fija (56 bytes):
 * ┌──────────┬────────────────────────┬──────────┬──────────┐
 * │ ID (4B)  │ Descripción (40B)      │ Precio   │ Stock    │
 * │ Int      │ 20 chars × 2 bytes     │ Double   │ Int      │
 * │          │ = 40 bytes             │ (8B)     │ (4B)     │
 * └──────────┴────────────────────────┴──────────┴──────────┘
 * Total = 4 + 40 + 8 + 4 = 56 bytes
 */
data class Articulo(
    val id: Int,
    val descripcion: String,
    val precio: Double,
    val stock: Int
) {
    companion object {
        /** Tamaño fijo de cada registro en disco (bytes). */
        const val RECORD_SIZE = 56

        /** Longitud fija del campo descripción (caracteres). */
        const val DESC_LENGTH = 20

        /** Identificador especial para eliminación lógica. */
        const val DELETED_ID = -1

        /**
         * Lee un artículo desde la posición actual del archivo.
         * El cursor del archivo debe estar posicionado al inicio del registro.
         */
        fun readFromFile(raf: RandomAccessFile): Articulo {
            val id = raf.readInt()

            // Leer 20 caracteres (cada uno de 2 bytes = 40 bytes)
            val charArray = CharArray(DESC_LENGTH)
            for (i in 0 until DESC_LENGTH) {
                charArray[i] = raf.readChar()
            }
            val descripcion = String(charArray).trimEnd()

            val precio = raf.readDouble()
            val stock = raf.readInt()

            return Articulo(id, descripcion, precio, stock)
        }
    }

    /**
     * Escribe este artículo en la posición actual del archivo.
     * La descripción se padea o trunca a exactamente [DESC_LENGTH] caracteres.
     */
    fun writeToFile(raf: RandomAccessFile) {
        raf.writeInt(id)

        // Normalizar descripción a exactamente 20 caracteres
        val normalized = descripcion.padEnd(DESC_LENGTH).substring(0, DESC_LENGTH)
        for (char in normalized) {
            raf.writeChar(char.code)
        }

        raf.writeDouble(precio)
        raf.writeInt(stock)
    }

    /** Indica si el registro fue eliminado lógicamente. */
    fun isDeleted(): Boolean = id == DELETED_ID

    override fun toString(): String {
        return "| %-4d | %-20s | %10.2f | %6d |".format(id, descripcion, precio, stock)
    }
}
