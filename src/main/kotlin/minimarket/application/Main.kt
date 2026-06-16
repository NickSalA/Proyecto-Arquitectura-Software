package minimarket.application

import minimarket.data.model.Articulo
import minimarket.data.persistence.RepositorioArticulosSQL
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

fun main() {
    SwingUtilities.invokeLater {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        InventoryWindow().isVisible = true
    }
}

private class InventoryWindow : JFrame("MinimarketPOS - Inventario Local") {
    private val repository = RepositorioArticulosSQL()

    private val idField = JTextField()
    private val descriptionField = JTextField()
    private val priceField = JTextField()
    private val stockField = JTextField()
    private val statusLabel = JLabel()

    private val tableModel = object : DefaultTableModel(arrayOf("ID", "Descripcion", "Precio", "Stock"), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean = false

        override fun getColumnClass(columnIndex: Int): Class<*> = when (columnIndex) {
            0 -> Int::class.javaObjectType
            2 -> Double::class.javaObjectType
            3 -> Int::class.javaObjectType
            else -> String::class.java
        }
    }
    private val table = JTable(tableModel)

    init {
        title = "MinimarketPOS - Gestion de Inventario Local"
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1080, 680)
        setSize(1180, 720)
        setLocationRelativeTo(null)
        contentPane = buildRoot()
        configureTable()
        configureSelection()
        refreshTable("Sistema listo")
    }

    private fun buildRoot(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = UiColors.background
            border = EmptyBorder(18, 18, 18, 18)
            add(buildHeader(), BorderLayout.NORTH)
            add(buildContent(), BorderLayout.CENTER)
            add(buildStatusBar(), BorderLayout.SOUTH)
        }
    }

    private fun buildHeader(): JPanel {
        return object : JPanel(BorderLayout()) {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.paint = GradientPaint(0f, 0f, UiColors.primaryDark, width.toFloat(), height.toFloat(), UiColors.primary)
                g2.fillRoundRect(0, 0, width, height, 30, 30)
            }
        }.apply {
            isOpaque = false
            preferredSize = Dimension(0, 112)
            border = EmptyBorder(22, 28, 22, 28)

            val titleLabel = JLabel("MinimarketPOS").apply {
                foreground = Color.WHITE
                font = Font("Segoe UI", Font.BOLD, 33)
            }

            val subtitleLabel = JLabel("Cliente Swing legado conectado a SQL Server").apply {
                foreground = Color(219, 234, 254)
                font = Font("Segoe UI", Font.PLAIN, 15)
            }

            val textPanel = JPanel().apply {
                isOpaque = false
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(titleLabel)
                add(Box.createVerticalStrut(6))
                add(subtitleLabel)
            }

            val badge = JLabel("Entregable 3", SwingConstants.CENTER).apply {
                foreground = UiColors.primaryDark
                background = Color.WHITE
                isOpaque = true
                font = Font("Segoe UI", Font.BOLD, 14)
                border = EmptyBorder(10, 18, 10, 18)
            }

            add(textPanel, BorderLayout.WEST)
            add(badge, BorderLayout.EAST)
        }
    }

    private fun buildContent(): JPanel {
        return JPanel(BorderLayout(18, 0)).apply {
            isOpaque = false
            border = EmptyBorder(18, 0, 18, 0)
            add(buildFormCard(), BorderLayout.WEST)
            add(buildTableCard(), BorderLayout.CENTER)
        }
    }

    private fun buildFormCard(): JPanel {
        val card = createCard().apply {
            preferredSize = Dimension(340, 0)
            layout = BorderLayout(0, 18)
        }

        val titleLabel = JLabel("Mantenimiento").apply {
            foreground = UiColors.textStrong
            font = Font("Segoe UI", Font.BOLD, 21)
        }
        val subtitleLabel = JLabel("Registra y administra articulos locales.").apply {
            foreground = UiColors.textMuted
            font = Font("Segoe UI", Font.PLAIN, 13)
        }

        val titlePanel = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(titleLabel)
            add(Box.createVerticalStrut(4))
            add(subtitleLabel)
        }

        val formPanel = JPanel(GridBagLayout()).apply { isOpaque = false }
        addField(formPanel, 0, "ID", idField, "Ej. 1")
        addField(formPanel, 1, "Descripcion", descriptionField, "Ej. Arroz")
        addField(formPanel, 2, "Precio", priceField, "Ej. 5.50")
        addField(formPanel, 3, "Stock", stockField, "Ej. 20")

        val buttonsPanel = JPanel(GridBagLayout()).apply { isOpaque = false }
        addButton(buttonsPanel, 0, 0, createButton("Registrar", UiColors.primary) { registerArticle() })
        addButton(buttonsPanel, 1, 0, createButton("Actualizar", UiColors.warning) { updateArticle() })
        addButton(buttonsPanel, 0, 1, createButton("Eliminar", UiColors.danger) { deleteArticle() })
        addButton(buttonsPanel, 1, 1, createButton("Limpiar", UiColors.neutral) { clearForm() })

        val helpPanel = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiColors.border),
                EmptyBorder(18, 0, 0, 0)
            )
            add(infoLabel("Base de datos"))
            add(infoValue("MinimarketDB (SQL Server)"))
            add(Box.createVerticalStrut(12))
            add(infoLabel("Conexion JDBC"))
            add(infoValue(AppConfig.DB_DISPLAY))
        }

        val body = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(formPanel)
            add(Box.createVerticalStrut(18))
            add(buttonsPanel)
            add(Box.createVerticalStrut(18))
            add(helpPanel)
        }

        card.add(titlePanel, BorderLayout.NORTH)
        card.add(body, BorderLayout.CENTER)
        return card
    }

    private fun buildTableCard(): JPanel {
        val card = createCard().apply { layout = BorderLayout(0, 14) }

        val titleLabel = JLabel("Inventario activo").apply {
            foreground = UiColors.textStrong
            font = Font("Segoe UI", Font.BOLD, 21)
        }
        val subtitleLabel = JLabel("Los registros eliminados logicamente no se muestran.").apply {
            foreground = UiColors.textMuted
            font = Font("Segoe UI", Font.PLAIN, 13)
        }

        val titlePanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            val textPanel = JPanel().apply {
                isOpaque = false
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(titleLabel)
                add(Box.createVerticalStrut(4))
                add(subtitleLabel)
            }
            add(textPanel, BorderLayout.WEST)
            add(createButton("Refrescar", UiColors.neutral) { refreshTable("Tabla actualizada") }, BorderLayout.EAST)
        }

        val scrollPane = JScrollPane(table).apply {
            border = BorderFactory.createLineBorder(UiColors.border)
            viewport.background = Color.WHITE
        }

        card.add(titlePanel, BorderLayout.NORTH)
        card.add(scrollPane, BorderLayout.CENTER)
        return card
    }

    private fun buildStatusBar(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = Color.WHITE
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiColors.border),
                EmptyBorder(12, 16, 12, 16)
            )
            statusLabel.foreground = UiColors.textMuted
            statusLabel.font = Font("Segoe UI", Font.PLAIN, 13)
            add(statusLabel, BorderLayout.CENTER)
        }
    }

    private fun configureTable() {
        table.apply {
            rowHeight = 38
            font = Font("Segoe UI", Font.PLAIN, 14)
            tableHeader.font = Font("Segoe UI", Font.BOLD, 13)
            tableHeader.background = UiColors.tableHeader
            tableHeader.foreground = UiColors.textStrong
            gridColor = UiColors.border
            selectionBackground = UiColors.selection
            selectionForeground = UiColors.textStrong
            fillsViewportHeight = true
            autoCreateRowSorter = true
            setDefaultRenderer(Double::class.javaObjectType, MoneyRenderer())
        }
    }

    private fun configureSelection() {
        table.selectionModel.addListSelectionListener { event ->
            if (!event.valueIsAdjusting && table.selectedRow >= 0) {
                val row = table.convertRowIndexToModel(table.selectedRow)
                idField.text = tableModel.getValueAt(row, 0).toString()
                descriptionField.text = tableModel.getValueAt(row, 1).toString()
                priceField.text = tableModel.getValueAt(row, 2).toString()
                stockField.text = tableModel.getValueAt(row, 3).toString()
            }
        }
    }

    private fun registerArticle() {
        val article = readArticleFromForm() ?: return
        if (repository.existe(article.id)) {
            showWarning("Ya existe un articulo con ID ${article.id}.")
            return
        }

        if (repository.agregar(article)) {
            clearForm()
            refreshTable("Articulo ${article.id} registrado")
        } else {
            showError("No se pudo registrar el articulo.")
        }
    }

    private fun updateArticle() {
        val article = readArticleFromForm() ?: return
        if (!repository.existe(article.id)) {
            showWarning("No existe un articulo con ID ${article.id}.")
            return
        }

        if (repository.actualizar(article)) {
            clearForm()
            refreshTable("Articulo ${article.id} actualizado")
        } else {
            showError("No se pudo actualizar el articulo.")
        }
    }

    private fun deleteArticle() {
        val id = idField.text.trim().toIntOrNull()
        if (id == null || id <= 0) {
            showWarning("Seleccione o ingrese un ID valido para eliminar.")
            return
        }

        val article = repository.buscar(id)
        if (article == null) {
            showWarning("No existe un articulo con ID $id.")
            return
        }

        val confirmation = JOptionPane.showConfirmDialog(
            this,
            "Se eliminara logicamente el articulo:\n\nID: ${article.id}\nDescripcion: ${article.descripcion}\n\nDesea continuar?",
            "Confirmar eliminacion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )

        if (confirmation != JOptionPane.YES_OPTION) return

        if (repository.eliminar(id)) {
            clearForm()
            refreshTable("Articulo $id eliminado logicamente")
        } else {
            showError("No se pudo eliminar el articulo.")
        }
    }

    private fun readArticleFromForm(): Articulo? {
        val id = idField.text.trim().toIntOrNull()
        if (id == null || id <= 0) {
            showWarning("El ID debe ser un entero positivo.")
            return null
        }

        val description = descriptionField.text.trim()
        if (description.isEmpty()) {
            showWarning("La descripcion no puede estar vacia.")
            return null
        }

        val price = priceField.text.trim().toDoubleOrNull()
        if (price == null || price < 0) {
            showWarning("El precio debe ser un numero positivo.")
            return null
        }

        val stock = stockField.text.trim().toIntOrNull()
        if (stock == null || stock < 0) {
            showWarning("El stock debe ser un entero no negativo.")
            return null
        }

        return Articulo(id, description, price, stock)
    }

    private fun refreshTable(message: String) {
        tableModel.setRowCount(0)
        val articles = repository.listar()
        for (article in articles) {
            tableModel.addRow(arrayOf<Any>(article.id, article.descripcion, article.precio, article.stock))
        }
        statusLabel.text = "$message. Articulos activos: ${articles.size}."
    }

    private fun clearForm() {
        idField.text = ""
        descriptionField.text = ""
        priceField.text = ""
        stockField.text = ""
        table.clearSelection()
        idField.requestFocusInWindow()
    }

    private fun createCard(): JPanel {
        return JPanel().apply {
            background = Color.WHITE
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiColors.border),
                EmptyBorder(22, 22, 22, 22)
            )
        }
    }

    private fun addField(panel: JPanel, row: Int, labelText: String, field: JTextField, placeholder: String) {
        val label = JLabel(labelText).apply {
            foreground = UiColors.textStrong
            font = Font("Segoe UI", Font.BOLD, 13)
        }

        field.apply {
            font = Font("Segoe UI", Font.PLAIN, 14)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiColors.border),
                EmptyBorder(10, 12, 10, 12)
            )
            toolTipText = placeholder
        }

        val gbc = GridBagConstraints().apply {
            gridx = 0
            gridy = row * 2
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(if (row == 0) 0 else 12, 0, 4, 0)
        }
        panel.add(label, gbc)

        gbc.gridy = row * 2 + 1
        gbc.insets = Insets(0, 0, 0, 0)
        panel.add(field, gbc)
    }

    private fun addButton(panel: JPanel, x: Int, y: Int, button: JButton) {
        val gbc = GridBagConstraints().apply {
            gridx = x
            gridy = y
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
        }
        panel.add(button, gbc)
    }

    private fun createButton(text: String, color: Color, action: () -> Unit): JButton {
        return JButton(text).apply {
            foreground = Color.WHITE
            background = color
            font = Font("Segoe UI", Font.BOLD, 13)
            isFocusPainted = false
            isBorderPainted = false
            isOpaque = true
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            border = EmptyBorder(11, 14, 11, 14)
            addActionListener { action() }
        }
    }

    private fun infoLabel(text: String): JLabel {
        return JLabel(text).apply {
            foreground = UiColors.textStrong
            font = Font("Segoe UI", Font.BOLD, 12)
        }
    }

    private fun infoValue(text: String): JLabel {
        return JLabel("<html><body style='width:245px'>$text</body></html>").apply {
            foreground = UiColors.textMuted
            font = Font("Segoe UI", Font.PLAIN, 12)
        }
    }

    private fun showWarning(message: String) {
        JOptionPane.showMessageDialog(this, message, "Validacion", JOptionPane.WARNING_MESSAGE)
    }

    private fun showError(message: String) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
    }
}

private class MoneyRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        horizontalAlignment = RIGHT
        text = when (value) {
            is Number -> "%.2f".format(value.toDouble())
            else -> value?.toString() ?: ""
        }
        return component
    }
}

private object UiColors {
    val background = Color(241, 245, 249)
    val primary = Color(37, 99, 235)
    val primaryDark = Color(30, 64, 175)
    val warning = Color(217, 119, 6)
    val danger = Color(220, 38, 38)
    val neutral = Color(71, 85, 105)
    val border = Color(226, 232, 240)
    val tableHeader = Color(248, 250, 252)
    val selection = Color(219, 234, 254)
    val textStrong = Color(15, 23, 42)
    val textMuted = Color(100, 116, 139)
}
