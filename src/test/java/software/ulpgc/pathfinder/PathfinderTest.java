package software.ulpgc.pathfinder;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PathfinderTest {

    // --- Tests para GraphContainer (Lógica del grafo) ---
    @Test
    public void testShortestPathAndWeight() {
        // 1. Crear un grafo de prueba manualmente
        SimpleWeightedGraph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");

        // A -> B (peso 1)
        graph.addEdge("A", "B");
        graph.setEdgeWeight("A", "B", 1.0);

        // B -> C (peso 2)
        graph.addEdge("B", "C");
        graph.setEdgeWeight("B", "C", 2.0);

        // A -> C directo (peso 10, camino largo)
        graph.addEdge("A", "C");
        graph.setEdgeWeight("A", "C", 10.0);

        GraphContainer container = new GraphContainer(graph);

        // 2. Verificar que elige el camino corto (A->B->C, total 3.0) y no el directo
        // (10.0)
        List<String> path = container.shortestPathBetween("A", "C");
        assertEquals(3, path.size());
        assertEquals("A", path.get(0));
        assertEquals("B", path.get(1));
        assertEquals("C", path.get(2));

        // 3. Verificar el cálculo del peso
        assertEquals(3.0, container.pathWeightBetween("A", "C"), 0.001);
    }

    @Test
    public void testGraphContainerExceptions() {
        SimpleWeightedGraph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B"); // Desconectado
        GraphContainer container = new GraphContainer(graph);

        // Caso: Nodos que no existen
        assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "Z"));
        assertThrows(IllegalArgumentException.class, () -> container.pathWeightBetween("Z", "A"));

        // Caso: No hay camino entre nodos existentes
        assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "B"));
        assertThrows(IllegalArgumentException.class, () -> container.pathWeightBetween("A", "B"));
    }

    // --- Tests para FileGraphLoader (Lectura de archivos) ---
    @Test
    public void testFileLoader() throws IOException {
        // 1. Crear un archivo temporal con datos (incluyendo líneas erróneas para
        // forzar fallos controlados)
        File tempFile = File.createTempFile("graph", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("A,B,1.0\n"); // Válido
            writer.write("B,C,2.0\n"); // Válido
            writer.write("LineaBasura\n"); // Inválido (formato incorrecto)
            writer.write("A,C,NoEsNumero\n");// Inválido (peso no numérico)
        }

        // 2. Cargar el archivo
        FileGraphLoader loader = new FileGraphLoader(tempFile);
        GraphContainer container = loader.load();

        // 3. Verificar que se cargaron los datos válidos correctamente
        assertEquals(3.0, container.pathWeightBetween("A", "C"), 0.001);
    }
}