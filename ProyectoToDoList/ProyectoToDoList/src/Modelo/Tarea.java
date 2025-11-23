package Modelo;

/**
 * Clase Entidad: Representa una tarea como un Objeto (no solo texto).
 * Implementa 'Comparable' para que Java sepa cómo ordenar una lista de tareas automáticamente.
 */
public class Tarea implements Comparable<Tarea> {
    
    // Variables que definen las características de una tarea
    private String descripcion;
    private boolean esUrgente;

    /**
     * Constructor: Se ejecuta cuando haces "new Tarea(...)".
     * @param descripcion El texto de la tarea (ej: "Comprar pan").
     * @param esUrgente Define si la tarea tiene prioridad alta.
     */
    public Tarea(String descripcion, boolean esUrgente) {
        this.descripcion = descripcion;
        this.esUrgente = esUrgente;
    }

    // --- MÉTODOS GETTER (Para leer los datos privados desde fuera) ---
    
    public String getDescripcion() {
        return descripcion;
    }

    public boolean esUrgente() {
        return esUrgente;
    }

    /**
     * Método toString: Convierte este Objeto en un Texto legible.
     * Esto es lo que se mostraría si metes el objeto directamente en la JList.
     */
    @Override
    public String toString() {
        if (esUrgente) {
            return "★ URGENTE: " + descripcion;
        }
        return descripcion;
    }

    /**
     * Método compareTo: Regla de Ordenamiento.
     * Java usa esto cuando le dices "Collections.sort(lista)".
     * Retorna -1 si 'esta' tarea va antes, 1 si va después, 0 si son iguales.
     */
    @Override
    public int compareTo(Tarea otraTarea) {
        // Si yo soy urgente y la otra no, yo gano (voy primero -> -1)
        if (this.esUrgente && !otraTarea.esUrgente) {
            return -1;
        }
        // Si yo no soy urgente y la otra sí, ella gana (ella va primero -> 1)
        if (!this.esUrgente && otraTarea.esUrgente) {
            return 1;
        }
        // Si somos iguales (ambas urgentes o ambas normales), no cambiamos lugar
        return 0;
    }

    /**
     * Método equals: Regla de Identidad.
     * Sirve para que el HashSet sepa si una tarea es "clon" de otra.
     * Dos tareas son iguales si tienen la misma descripción (sin importar mayúsculas).
     */
    @Override
    public boolean equals(Object obj) {
        // Si soy yo mismo, somos iguales
        if (this == obj) return true;
        // Si el otro es nulo o no es una Tarea, no somos iguales
        if (obj == null || getClass() != obj.getClass()) return false;
        
        // Comparamos los textos ignorando mayúsculas
        Tarea otra = (Tarea) obj;
        return descripcion.equalsIgnoreCase(otra.descripcion);
    }

    /**
     * Método hashCode: Genera un ID numérico único basado en la descripción.
     * Es obligatorio sobrescribirlo si usas 'equals', para que el HashSet funcione rápido.
     */
    @Override
    public int hashCode() {
        return descripcion.toLowerCase().hashCode();
    }
}