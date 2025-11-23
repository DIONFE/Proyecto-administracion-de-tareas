package Controlador;

import Modelo.GestorDatos;
import Vista.GestionTareas;
import Vista.TareasCompletadas; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;   
import java.text.SimpleDateFormat; 
import java.time.LocalDate; 
import java.time.format.DateTimeFormatter; 
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

// La clase implementa ActionListener para poder responder a los clics de los botones
public class ControladorTareas implements ActionListener {

    // Variables para controlar las ventanas (Vistas)
    private GestionTareas vistaPrincipal;     
    private TareasCompletadas vistaHistorial;  
    
    // Variable para controlar los datos (Modelo)
    private GestorDatos modelo;
    
    // Modelos visuales que permiten agregar o quitar elementos de las listas en pantalla
    private DefaultListModel<String> modeloPendientes; 
    private DefaultListModel<String> modeloCompletadas;

    // Constructor: Se ejecuta una sola vez al iniciar el controlador
    public ControladorTareas(GestionTareas v1, TareasCompletadas v2) {
        // Guardamos las referencias de las ventanas que nos pasan
        this.vistaPrincipal = v1;
        this.vistaHistorial = v2;
        
        // Obtenemos la instancia única del gestor de datos (Memoria del programa)
        this.modelo = GestorDatos.getInstancia();
        
        // Inicializamos el modelo visual para la lista de tareas pendientes
        this.modeloPendientes = new DefaultListModel<>();
        // Conectamos este modelo a la JList visual de la ventana principal
        this.vistaPrincipal.getJList().setModel(modeloPendientes);
        
        // Inicializamos el modelo visual para la lista de tareas completadas
        this.modeloCompletadas = new DefaultListModel<>();
        // Conectamos este modelo a la JList visual de la ventana de historial
        this.vistaHistorial.getListaVisual().setModel(modeloCompletadas);
        
        // Llamamos a este método para recuperar tareas si ya existían en memoria
        cargarDatosGuardados();
        
        // Configuramos el botón "Crear": Le ponemos una etiqueta interna y le asignamos este controlador
        this.vistaPrincipal.getBtnCrear().setActionCommand("BTN_CREAR");
        this.vistaPrincipal.getBtnCrear().addActionListener(this);

        // Configuramos el botón "Completar": Etiqueta y oyente
        this.vistaPrincipal.getBtnTareaCompleta().setActionCommand("BTN_COMPLETAR");
        this.vistaPrincipal.getBtnTareaCompleta().addActionListener(this);
        
        // Configuramos el botón "Eliminar": Etiqueta y oyente
        this.vistaPrincipal.getBtnEliminar().setActionCommand("BTN_ELIMINAR");
        this.vistaPrincipal.getBtnEliminar().addActionListener(this);
        
        // Configuramos el botón "Limpiar Historial": Etiqueta y oyente
        this.vistaHistorial.getBtnLimpiar().setActionCommand("BTN_LIMPIAR");
        this.vistaHistorial.getBtnLimpiar().addActionListener(this);
        
        // Imprimimos en consola para confirmar que todo cargó bien
        System.out.println("DEBUG: Controlador iniciado correctamente.");
    }
    
    // Método que se dispara automáticamente cuando se hace clic en cualquier botón configurado
    @Override
    public void actionPerformed(ActionEvent e) {
        // Obtenemos la etiqueta (comando) del botón que fue presionado
        String comando = e.getActionCommand();

        // Si el botón fue "Crear", ejecutamos el método de agregar
        if ("BTN_CREAR".equals(comando)) agregarTarea();
        // Si fue "Completar", ejecutamos el método de completar
        else if ("BTN_COMPLETAR".equals(comando)) completarTarea();
        // Si fue "Eliminar", ejecutamos el método de eliminar
        else if ("BTN_ELIMINAR".equals(comando)) eliminarTarea();
        // Si fue "Limpiar", ejecutamos el método de vaciar historial
        else if ("BTN_LIMPIAR".equals(comando)) vaciarListaHistorial();
    }
    
    // Método principal para crear y guardar una tarea
    private void agregarTarea() {
        // Obtenemos el texto de los campos de la vista y quitamos espacios extra (trim)
        String titulo = vistaPrincipal.getTxtRecordatorio().getText().trim();
        String descripcion = vistaPrincipal.getTxtDescripcion().getText().trim();
        String fechaTexto = vistaPrincipal.getTxtFecha().getText().trim(); 
        // Obtenemos la opción seleccionada en el menú desplegable (ComboBox)
        String categoria = (String) vistaPrincipal.getCmbCategoria().getSelectedItem();
        
        // Validación: Si el título está vacío, mostramos error y salimos
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Escribe el nombre de la tarea.");
            return;
        }
        
        // Validación: Si hay fecha escrita...
        if (!fechaTexto.isEmpty()) {
            // Verificamos que el formato sea dd/MM/yyyy
            if (!esFormatoValido(fechaTexto)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Formato incorrecto. Usa: dd/MM/yyyy");
                vistaPrincipal.getTxtFecha().requestFocus(); // Ponemos el cursor en el campo fecha
                return;
            }
            // Verificamos que la fecha no sea anterior a hoy
            if (esFechaPasada(fechaTexto)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "La fecha debe ser hoy o futura.");
                return;
            }
        }

        // Verificamos en el modelo si la tarea es nueva (para evitar duplicados)
        if (modelo.esTareaNueva(titulo)) {
            
            // Usamos StringBuilder para construir el texto final eficientemente
            StringBuilder sb = new StringBuilder();
            sb.append(titulo); // Agregamos el título
            
            // Si hay descripción, la agregamos separada por dos puntos
            if (!descripcion.isEmpty()) sb.append(" : ").append(descripcion);
            // Si hay fecha, la agregamos entre paréntesis
            if (!fechaTexto.isEmpty()) sb.append(" (Vence: ").append(fechaTexto).append(")");
            
            // Convertimos el constructor a un String final
            String textoFinal = sb.toString();
            // Variable para saber si guardamos como prioritaria en el modelo
            boolean esImportanteModelo = false; 

            // Lógica para decidir el icono y la importancia según la categoría
            if ("Importantes".equals(categoria) || "Importante".equals(categoria)) {
                textoFinal = "★ [URGENTE] " + textoFinal.toUpperCase(); // Agregamos estrella y mayúsculas
                esImportanteModelo = true; // Marcamos como importante
            } 
            else if ("Tarea de hoy".equals(categoria)) {
                textoFinal = "📅 [HOY] " + textoFinal; // Agregamos calendario
                esImportanteModelo = true; 
            } 
            else {
                textoFinal = "📝 " + textoFinal; // Agregamos lápiz para generales
            }

            // Guardamos los datos en la memoria lógica (Modelo)
            modelo.registrarTarea(titulo, textoFinal, esImportanteModelo);
            
            // Insertamos la tarea en la lista visual usando el algoritmo de ordenamiento
            insertarConPrioridadYFecha(textoFinal);
            
            // Limpiamos los campos de texto para que el usuario escriba otra
            vistaPrincipal.limpiarCampos();
            
        } else {
            // Si ya existe, mostramos error
            JOptionPane.showMessageDialog(vistaPrincipal, "¡Esa tarea ya existe!");
        }
    }

    // Algoritmo para ordenar la lista visualmente (Categoría > Fecha)
    private void insertarConPrioridadYFecha(String nuevaTarea) {
        // Calculamos el nivel de importancia de la nueva tarea (1, 2 o 3)
        int nivelNuevo = obtenerNivelPrioridad(nuevaTarea);
        // Extraemos la fecha de la nueva tarea (si no tiene, será una fecha muy lejana)
        LocalDate fechaNueva = extraerFecha(nuevaTarea);
        
        // Asumimos por defecto que va al final de la lista
        int indiceInsertar = modeloPendientes.getSize(); 

        // Recorremos toda la lista actual para encontrar la posición correcta
        for (int i = 0; i < modeloPendientes.getSize(); i++) {
            // Obtenemos la tarea que ya está en la lista en esa posición
            String tareaActual = modeloPendientes.get(i);
            
            // Calculamos su nivel y su fecha
            int nivelActual = obtenerNivelPrioridad(tareaActual);
            LocalDate fechaActual = extraerFecha(tareaActual);

            // Regla 1: Si la nueva es más importante (nivel menor es mejor), la insertamos aquí
            if (nivelNuevo < nivelActual) {
                indiceInsertar = i;
                break; 
            }
            
            // Regla 2: Si tienen la misma importancia, comparamos las fechas
            if (nivelNuevo == nivelActual) {
                // Si la nueva vence antes que la actual, la insertamos aquí
                if (fechaNueva.isBefore(fechaActual)) {
                    indiceInsertar = i;
                    break;
                }
            }
        }

        // Insertamos la tarea en el índice calculado
        modeloPendientes.add(indiceInsertar, nuevaTarea);
    }

    // Método auxiliar: Devuelve un número según el icono (1=Estrella, 2=Calendario, 3=Lápiz)
    private int obtenerNivelPrioridad(String textoTarea) {
        if (textoTarea.startsWith("★")) return 1; // Prioridad máxima
        if (textoTarea.startsWith("📅")) return 2; // Prioridad media
        if (textoTarea.startsWith("📝")) return 3; // Prioridad baja
        return 4; // Otros casos
    }

    // Método auxiliar: Busca el texto de la fecha y lo convierte a un objeto LocalDate
    private LocalDate extraerFecha(String textoTarea) {
        try {
            // Busca si el texto contiene la etiqueta de fecha
            if (textoTarea.contains("(Vence:")) {
                // Calcula dónde empieza la fecha
                int inicio = textoTarea.indexOf("(Vence:") + 8;
                // Calcula dónde termina
                int fin = textoTarea.indexOf(")", inicio);
                // Extrae solo los números de la fecha
                String fechaStr = textoTarea.substring(inicio, fin).trim();
                // Define el formato esperado
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                // Convierte el String a LocalDate
                return LocalDate.parse(fechaStr, formatter);
            }
        } catch (Exception e) { }
        // Si no tiene fecha o falla, devolvemos una fecha muy lejana para que se ordene al final
        return LocalDate.of(9999, 12, 31);
    }

    // Método auxiliar: Verifica si el texto cumple el formato dd/MM/yyyy
    private boolean esFormatoValido(String fechaTexto) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false); // Hace que la validación sea estricta
        try {
            formato.parse(fechaTexto); // Intenta convertirla
            return true; // Si funciona, es válida
        } catch (ParseException e) { 
            return false; // Si falla, no es válida
        }
    }

    // Método auxiliar: Verifica si la fecha ingresada ya pasó
    private boolean esFechaPasada(String fechaTexto) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaIngresada = LocalDate.parse(fechaTexto, formatter);
            LocalDate hoy = LocalDate.now(); // Obtiene la fecha del sistema
            // Retorna verdadero si la fecha ingresada es anterior a hoy
            return fechaIngresada.isBefore(hoy);
        } catch (Exception e) { return true; }
    }

    // Método para mover una tarea a completadas
    private void completarTarea() {
        // Obtenemos el valor seleccionado en la lista
        String tareaSeleccionada = vistaPrincipal.getJList().getSelectedValue();
        
        if (tareaSeleccionada != null) {
            // Avisamos al modelo para que mueva los datos
            modelo.completarTarea(tareaSeleccionada);
            // Quitamos de la lista visual de pendientes
            modeloPendientes.removeElement(tareaSeleccionada);
            // Agregamos a la lista visual de completadas con un check
            modeloCompletadas.add(0, "✔ " + tareaSeleccionada);
        } else {
            JOptionPane.showMessageDialog(vistaPrincipal, "Selecciona una tarea.");
        }
    }
    
    // Método para borrar una tarea definitivamente
    private void eliminarTarea() {
        // Obtenemos el valor seleccionado
        String tareaSeleccionada = vistaPrincipal.getJList().getSelectedValue();
        
        if (tareaSeleccionada != null) {
            // Pedimos confirmación al usuario
            int confirm = JOptionPane.showConfirmDialog(vistaPrincipal, "¿Eliminar permanentemente?", "Eliminar", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Borramos del modelo (para liberar el nombre)
                modelo.eliminarTareaPendiente(tareaSeleccionada);
                // Borramos de la lista visual
                modeloPendientes.removeElement(tareaSeleccionada);
            }
        } else {
            JOptionPane.showMessageDialog(vistaPrincipal, "Selecciona una tarea.");
        }
    }

    // Método para borrar todo el historial
    private void vaciarListaHistorial() {
        // Si la lista no está vacía
        if (!modeloCompletadas.isEmpty()) {
            // Pedimos confirmación
            int r = JOptionPane.showConfirmDialog(vistaHistorial, "¿Borrar todo?", "Confirmar", JOptionPane.YES_NO_OPTION);
            
            if (r == JOptionPane.YES_OPTION) {
                // Borramos del modelo
                modelo.vaciarCompletadas();
                // Limpiamos la lista visual
                modeloCompletadas.clear();
            }
        }
    }
    
    // Método que recarga las listas visuales con los datos del modelo
    private void cargarDatosGuardados() {
        // Recorremos las pendientes del modelo y las agregamos a la vista
        for (String t : modelo.obtenerPendientes()) modeloPendientes.addElement(t);
        // Recorremos las completadas del modelo y las agregamos a la vista
        for (String t : modelo.obtenerCompletadas()) modeloCompletadas.addElement(t);
    }
}