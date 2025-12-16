import javax.persistence.*;
import java.util.*;

// 1. Definimos la Clase (El molde del Objeto)
// @Entity indica que esta clase puede ser guardada en la BDOO
@Entity
class Persona {
    private String nombre;
    private int edad;

    public Persona(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    @Override
    public String toString() {
        return "Persona: " + nombre + " (" + edad + " años)";
    }
}

public class Main {
    public static void main(String[] args) {
        // 2. Conectamos a la Base de Datos (se crea un archivo físico 'miBase.odb')
        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("objectdb:miBase.odb");
        EntityManager em = emf.createEntityManager();

        try {
            // 3. Iniciar Transacción
            em.getTransaction().begin();

            // 4. Crear Objetos (Puros, sin SQL)
            Persona p1 = new Persona("Carlos Cabral", 21);
            Persona p2 = new Persona("Ingeniero de Datos", 25);
            Persona p3 = new Persona("Dra. Claudia", 40);

            // 5. Guardar Objetos (Persistencia)
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);

            // 6. Confirmar cambios (Commit)
            em.getTransaction().commit();
            System.out.println("¡Objetos guardados exitosamente en la BDOO!");

            // 7. Recuperar datos para probar
            TypedQuery<Persona> query = em.createQuery("SELECT p FROM Persona p", Persona.class);
            List<Persona> resultados = query.getResultList();
            
            System.out.println("\n--- LISTA DE OBJETOS EN BDOO ---");
            for (Persona p : resultados) {
                System.out.println(p);
            }

        } finally {
            // Cerrar conexión
            em.close();
            emf.close();
        }
    }
}