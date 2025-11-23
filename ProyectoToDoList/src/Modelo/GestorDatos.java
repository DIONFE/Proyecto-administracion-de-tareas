package Modelo;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Clase Modelo: Actúa como la base de datos en memoria RAM.
 * Se encarga de almacenar, ordenar y validar los datos de las tareas.
 */
public class GestorDatos {
    
    // Variable estática para el Patrón Singleton (garantiza que solo exista una memoria compartida)
    private static GestorDatos instancia;

    // HashSet: Una colección especial que no permite elementos duplicados.
    // Lo usamos para saber rápidamente si un nombre de tarea ya existe.
    private HashSet<String> memoriaValidacion;
    
    // ArrayList: Listas dinámicas para guardar las tareas visuales (con iconos y fechas).
    // Usamos listas porque nos importa el orden en que se muestran.
    private ArrayList<String> listaPendientes;    // Tareas por hacer
    private ArrayList<String> listaCompletadas;   // Historial

    /**
     * Constructor privado.
     * Al ser privado, nadie fuera de esta clase puede hacer "new GestorDatos()".
     * Esto obliga a usar el método getInstancia().
     */
    private GestorDatos() {
        // Inicializamos las colecciones vacías para evitar errores de "NullPointerException"
        this.memoriaValidacion = new HashSet<>();
        this.listaPendientes = new ArrayList<>();
        this.listaCompletadas = new ArrayList<>(); 
    }

    /**
     * Método Singleton.
     * Devuelve siempre la misma instancia de la clase. Si no existe, la crea.
     * Así, la ventana Principal y la ventana Completadas ven los mismos datos.
     */
    public static GestorDatos getInstancia() {
        if (instancia == null) {
            instancia = new GestorDatos();
        }
        return instancia;
    }

    // --- LÓGICA DE NEGOCIO ---

    /**
     * Verifica si una tarea ya existe.
     * @param tarea Texto de la tarea.
     * @return true si es nueva, false si ya está registrada.
     */
    public boolean esTareaNueva(String tarea) {
        // Convertimos a mayúsculas para que "Comprar pan" sea igual a "comprar pan"
        // contains() busca en el HashSet de forma muy rápida.
        return !memoriaValidacion.contains(tarea.toUpperCase());
    }

    /**
     * Guarda una tarea nueva en las listas.
     * @param tareaOriginal El nombre puro (para validación).
     * @param tareaFormateada El nombre con iconos y fecha (para mostrar).
     * @param esImportante Si es true, se guarda al principio de la lista.
     */
    public void registrarTarea(String tareaOriginal, String tareaFormateada, boolean esImportante) {
        // 1. Guardamos el nombre en mayúsculas en el HashSet para bloquear duplicados futuros
        memoriaValidacion.add(tareaOriginal.toUpperCase());
        
        // 2. Guardamos el texto visual en la lista
        if (esImportante) {
            // El índice 0 inserta el elemento al principio, empujando a los demás abajo.
            // Esto se usa para las tareas con Estrella o Calendario.
            listaPendientes.add(0, tareaFormateada); 
        } else {
            // El método add normal inserta al final de la lista.
            // Esto se usa para las tareas generales.
            listaPendientes.add(tareaFormateada); 
        }
    }
    
    /**
     * Mueve una tarea de la lista de pendientes a la de completadas.
     */
    public void completarTarea(String tareaFormateada) {
        // 1. Borramos la tarea de la lista de pendientes
        listaPendientes.remove(tareaFormateada);
        
        // 2. La agregamos a la lista de completadas, poniéndole un check visual
        // Usamos add(0, ...) para que las recién completadas salgan arriba del historial
        listaCompletadas.add(0, "✔ " + tareaFormateada); 
        
        // 3. IMPORTANTE: Liberamos el nombre original.
        // Al borrarla de memoriaValidacion, el usuario podrá volver a crear una tarea con ese nombre.
        String nombreReal = obtenerNombreOriginal(tareaFormateada);
        memoriaValidacion.remove(nombreReal); 
    }
    
    /**
     * Elimina una tarea definitivamente (Boton Eliminar).
     */
    public void eliminarTareaPendiente(String tareaFormateada) {
        // 1. La quitamos de la lista visual
        listaPendientes.remove(tareaFormateada);
        
        // 2. La olvidamos de la memoria de validación
        String nombreReal = obtenerNombreOriginal(tareaFormateada);
        memoriaValidacion.remove(nombreReal);
    }
    
    /**
     * Borra todo el historial de tareas completadas.
     * No necesitamos tocar memoriaValidacion aquí porque las tareas completadas
     * ya fueron borradas de la memoria de validación al completarse.
     */
    public void vaciarCompletadas() {
        listaCompletadas.clear();
    }
    
    // --- GETTERS PARA RECUPERAR DATOS ---
    
    public ArrayList<String> obtenerPendientes() {
        return listaPendientes;
    }

    public ArrayList<String> obtenerCompletadas() {
        return listaCompletadas;
    }

    /**
     * Método Mágico: Limpia el texto visual para encontrar el nombre original.
     * Ejemplo: Convierte "★ [URGENTE] Tarea 1 (Vence: 10/10/2025)" en "TAREA 1".
     */
    private String obtenerNombreOriginal(String textoFormateado) {
        String limpio = textoFormateado;
        
        // 1. Quitamos los prefijos (Iconos y etiquetas)
        limpio = limpio.replace("★ [URGENTE] ", "");
        limpio = limpio.replace("📅 [HOY] ", "");
        limpio = limpio.replace("📝 ", ""); // El icono del lápiz
        
        // 2. Quitamos la fecha si existe
        // Buscamos dónde empieza el texto "(Vence:" y cortamos el String hasta ahí
        if (limpio.contains(" (Vence:")) {
            int indiceFecha = limpio.indexOf(" (Vence:");
            limpio = limpio.substring(0, indiceFecha);
        }
        
        // 3. Quitamos la descripción si existe
        // Buscamos el separador " : " y cortamos hasta ahí
        if (limpio.contains(" : ")) {
            int indiceDesc = limpio.indexOf(" : ");
            limpio = limpio.substring(0, indiceDesc);
        }
        
        // Devolvemos limpio, sin espacios extra y en mayúsculas para comparar
        return limpio.toUpperCase().trim();
    }
}