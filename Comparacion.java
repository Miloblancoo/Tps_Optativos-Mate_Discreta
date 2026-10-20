import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Compara empíricamente Dijkstra ingenuo vs Dijkstra con PriorityQueue
 * sobre grafos aleatorios ralos (E ~ 6V) y densos (completos, E ~ V^2/2).
 */
public class Comparacion {

    /** Grafo no dirigido conexo. Si aristasPorVertice < 0, es completo (denso). */
    static List<List<Dijkstra.Arista>> grafoAleatorio(int n, int aristasPorVertice, long seed) {
        Random rnd = new Random(seed);
        List<List<Dijkstra.Arista>> g = Dijkstra.grafoVacio(n);

        // Camino base para garantizar que sea conexo
        for (int i = 1; i < n; i++) {
            Dijkstra.agregarAristaNoDirigida(g, i - 1, i, 1 + rnd.nextInt(100));
        }

        if (aristasPorVertice < 0) {
            for (int u = 0; u < n; u++) {
                for (int v = u + 2; v < n; v++) {
                    Dijkstra.agregarAristaNoDirigida(g, u, v, 1 + rnd.nextInt(100));
                }
            }
        } else {
            for (int k = 0; k < n * aristasPorVertice; k++) {
                int u = rnd.nextInt(n), v = rnd.nextInt(n);
                if (u != v) {
                    Dijkstra.agregarAristaNoDirigida(g, u, v, 1 + rnd.nextInt(100));
                }
            }
        }
        return g;
    }

    static long cantidadAristas(List<List<Dijkstra.Arista>> g) {
        long total = 0;
        for (List<Dijkstra.Arista> ady : g) total += ady.size();
        return total / 2;
    }

    static void comparar(String titulo, int[] tamanios, int aristasPorVertice) {
        System.out.println("\n=== " + titulo + " ===");
        System.out.printf("%7s %10s %13s %11s %12s%n", "V", "E", "ingenuo (ms)", "heap (ms)", "aceleración");
        for (int n : tamanios) {
            List<List<Dijkstra.Arista>> g = grafoAleatorio(n, aristasPorVertice, 0);

            long t0 = System.nanoTime();
            Dijkstra.Resultado r1 = Dijkstra.dijkstraIngenuo(g, 0);
            long t1 = System.nanoTime();
            Dijkstra.Resultado r2 = Dijkstra.dijkstraHeap(g, 0);
            long t2 = System.nanoTime();

            if (!Arrays.equals(r1.dist, r2.dist)) {
                throw new AssertionError("Las dos versiones deberían dar las mismas distancias");
            }
            double ms1 = (t1 - t0) / 1e6, ms2 = (t2 - t1) / 1e6;
            System.out.printf("%7d %10d %13.2f %11.2f %11.1fx%n", n, cantidadAristas(g), ms1, ms2, ms1 / ms2);
        }
    }

    public static void main(String[] args) {
        // Calentamiento: la JVM compila (JIT) el código las primeras veces que corre,
        // así que se ejecuta un poco antes de medir para no falsear los primeros tiempos.
        for (int i = 0; i < 20; i++) {
            List<List<Dijkstra.Arista>> g = grafoAleatorio(1000, 5, i);
            Dijkstra.dijkstraIngenuo(g, 0);
            Dijkstra.dijkstraHeap(g, 0);
        }

        comparar("Grafos RALOS (E ~ 6V)", new int[] { 1000, 2000, 4000, 8000, 16000, 32000 }, 5);
        comparar("Grafos DENSOS (completos, E ~ V^2/2)", new int[] { 250, 500, 1000, 2000 }, -1);
    }
}
