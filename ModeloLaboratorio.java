import javax.persistence.*;
import java.util.*;

// --- 1. CLASES PADRE (Superclases) ---
@Entity
class Usuario {
    protected String id; // Matrícula o No. Empleado
    protected String nombre;

    public Usuario(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    @Override
    public String toString() { return nombre + " (" + id + ")"; }
}

@Entity
class Equipo {
    protected String codigoInventario;
    protected String marca;

    public Equipo(String codigo, String marca) {
        this.codigoInventario = codigo;
        this.marca = marca;
    }
    @Override
    public String toString() { return "Equipo: " + marca; }
}

// --- 2. CLASES HIJAS (Herencia) ---
@Entity
class Estudiante extends Usuario {
    private String carrera;
    private int semestre;

    public Estudiante(String mat, String nom, String car, int sem) {
        super(mat, nom);
        this.carrera = car;
        this.semestre = sem;
    }

    @Override
    public String toString() { 
        // CORREGIDO: Ahora usamos la variable 'semestre' para quitar la advertencia
        return super.toString() + " - Estudiante de " + carrera + " (" + semestre + "º Semestre)"; 
    }
}

@Entity
class Profesor extends Usuario {
    private String departamento;

    public Profesor(String num, String nom, String depto) {
        super(num, nom);
        this.departamento = depto;
    }
    @Override
    public String toString() { return "Prof. " + super.toString() + " [" + departamento + "]"; }
}

@Entity
class Laptop extends Equipo {
    private String procesador;
    private int ramGB;

    public Laptop(String cod, String marca, String proc, int ram) {
        super(cod, marca);
        this.procesador = proc;
        this.ramGB = ram;
    }
    @Override
    public String toString() { return "Laptop " + marca + " (" + procesador + ", " + ramGB + "GB RAM)"; }
}

@Entity
class ComponenteElectronico extends Equipo {
    private String tipo; // Sensor, Microcontrolador, etc.

    public ComponenteElectronico(String cod, String marca, String tipo) {
        super(cod, marca);
        this.tipo = tipo;
    }
    @Override
    public String toString() { return tipo + " - " + marca; }
}

// --- 3. CLASE COMPUESTA (El Préstamo) ---
@Entity
class Prestamo {
    private Usuario quienRecibe; // Polimorfismo: Puede ser Estudiante o Profesor
    private Date fechaPrestamo;
    
    // Lista polimórfica: Puede contener Laptops Y Componentes mezclados
    private List<Equipo> equiposPrestados = new ArrayList<>();

    public Prestamo(Usuario usuario) {
        this.quienRecibe = usuario;
        this.fechaPrestamo = new Date();
    }

    public void agregarEquipo(Equipo e) {
        equiposPrestados.add(e);
    }

    @Override
    public String toString() {
        // CORREGIDO: Ahora usamos 'fechaPrestamo' para quitar la advertencia
        return "Préstamo [" + fechaPrestamo + "] a: " + quienRecibe + "\n  -> Equipos: " + equiposPrestados;
    }
}

// --- MAIN PARA PROBAR EL MODELO ---
public class ModeloLaboratorio {
    public static void main(String[] args) {
        // Conectar a la BD (se creará o abrirá 'laboratorio.odb')
        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("objectdb:laboratorio.odb");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // 1. Crear Usuarios
            Usuario alumno = new Estudiante("2104", "Carlos Cabral", "Sistemas", 7);
            Usuario profe = new Profesor("9988", "Ing. Juan Pérez", "Ciencias Básicas");

            // 2. Crear Equipos
            Equipo lap1 = new Laptop("L-001", "Dell", "i7", 16);
            Equipo arduino = new ComponenteElectronico("E-500", "Arduino", "Microcontrolador Uno");

            // 3. Crear un Préstamo
            Prestamo p1 = new Prestamo(alumno); 
            p1.agregarEquipo(lap1);     // Agrego una Laptop
            p1.agregarEquipo(arduino);  // Agrego un Arduino

            // 4. Guardar TOO (SOLUCIÓN AL ERROR)
            // Guardamos explícitamente cada objeto para que estén "managed" antes de relacionarlos
            em.persist(alumno);
            em.persist(profe);
            em.persist(lap1);
            em.persist(arduino);
            em.persist(p1); 

            em.getTransaction().commit();
            System.out.println(">>> Modelo de Laboratorio guardado exitosamente. <<<");

            // Mostrar resultados recuperados de la BD
            List<Prestamo> prestamos = em.createQuery("SELECT p FROM Prestamo p", Prestamo.class).getResultList();
            
            System.out.println("\n--- LISTADO DE PRÉSTAMOS ACTIVOS ---");
            for (Prestamo p : prestamos) {
                System.out.println(p);
                System.out.println("------------------------------------------------");
            }

        } catch (Exception e) {
            // En caso de error, deshacer cambios y mostrar el problema real
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}