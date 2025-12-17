import javax.persistence.*;
import java.util.*;

public class ConsultasLaboratorio {
    public static void main(String[] args) {
        // 1. Conectamos a la MISMA base de datos anterior
        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("objectdb:laboratorio.odb");
        EntityManager em = emf.createEntityManager();

        try {
            System.out.println("=== INICIANDO BATERÍA DE CONSULTAS BDOO ===");

            // --- CONSULTA 1: POLIMORFISMO (Traer TODO el inventario) ---
            // En SQL tendrías que hacer UNION de tablas. Aquí pides 'Equipo' y te trae Laptops y Componentes.
            System.out.println("\n1. CONSULTA POLIMÓRFICA (Todo el Inventario):");
            TypedQuery<Equipo> q1 = em.createQuery("SELECT e FROM Equipo e", Equipo.class);
            List<Equipo> inventario = q1.getResultList();
            for (Equipo e : inventario) {
                System.out.println(" - " + e);
            }

            // --- CONSULTA 2: FILTRO (WHERE) ---
            // Buscar Estudiantes de 'Sistemas' con semestre avanzado
            System.out.println("\n2. CONSULTA CON FILTRO (Estudiantes de Sistemas > 5o Semestre):");
            TypedQuery<Estudiante> q2 = em.createQuery(
                "SELECT e FROM Estudiante e WHERE e.carrera = 'Sistemas' AND e.semestre > 5", Estudiante.class);
            for (Estudiante e : q2.getResultList()) {
                System.out.println(" - " + e);
            }

            // --- OPERACIÓN 1: ACTUALIZACIÓN (UPDATE) ---
            System.out.println("\n3. OPERACIÓN DE ACTUALIZACIÓN:");
            em.getTransaction().begin();
            
            // Buscamos al estudiante "Carlos Cabral" (Suponiendo que es el primero que encuentra)
            // JPQL permite buscar objetos completos
            Estudiante carlos = em.createQuery("SELECT e FROM Estudiante e WHERE e.nombre LIKE 'Carlos%'", Estudiante.class)
                                  .getSingleResult();
            
            System.out.println(" * Antes: " + carlos);
            carlos.toString(); // Forzamos lectura
            
            // MODIFICAMOS EL OBJETO DIRECTAMENTE (Sin 'UPDATE tabla SET...')
            // Al cambiar el atributo en memoria dentro de una transacción, la BD se actualiza sola.
            // Para simular que pasaste de semestre:
            // (Nota: Necesitamos acceso al campo o un setter, pero como es ejemplo académico, asumimos acceso o reflexión)
            // Como los atributos eran privados en el archivo anterior, usaremos una consulta UPDATE de JPQL para no modificar las clases.
            
            int actualizados = em.createQuery("UPDATE Estudiante e SET e.semestre = 8 WHERE e.nombre LIKE 'Carlos%'").executeUpdate();
            
            em.getTransaction().commit();
            System.out.println(" * " + actualizados + " registro(s) actualizado(s). Carlos pasó a 8vo Semestre.");
            
            // Verificamos el cambio
            em.clear(); // Limpiamos caché para obligar a leer de disco
            Estudiante carlosNuevo = em.createQuery("SELECT e FROM Estudiante e WHERE e.nombre LIKE 'Carlos%'", Estudiante.class).getSingleResult();
            System.out.println(" * Después: " + carlosNuevo);


            // --- OPERACIÓN 2: ELIMINACIÓN (DELETE) ---
            System.out.println("\n4. OPERACIÓN DE ELIMINACIÓN (Borrando Préstamos Viejos):");
            em.getTransaction().begin();
            
            // Borramos préstamos anteriores a hoy (o todos para el ejemplo)
            // Ojo: ObjectDB protege borrados si no se configuran cascadas, aquí borraremos el objeto Préstamo raiz.
            Query qDelete = em.createQuery("DELETE FROM Prestamo p");
            int borrados = qDelete.executeUpdate();
            
            em.getTransaction().commit();
            System.out.println(" * Se han eliminado " + borrados + " préstamos del historial.");


        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
        }
    }
}