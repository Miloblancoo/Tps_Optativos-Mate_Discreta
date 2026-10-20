import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Dijkstra: camino mínimo desde un origen en un grafo con pesos NO negativos.
 *
 * Dos versiones para comparar complejidades:
 *   1. dijkstraIngenuo -> busca el mínimo recorriendo un arreglo: O(V^2)
 *   2. dijkstraHeap    -> usa java.util.PriorityQueue (heap binario): O((V + E) log V)
 *
 * El grafo es una lista de adyacencia: grafo.get(u) = aristas que salen de u.
 * Los vértices se numeran 0..V-1.
 */
public class Dijkstra {

    static final long INF = Long.MAX_VALUE;

    static class Arista {
        final int destino;
        final int peso;

        Arista(int destino, int peso) {
            this.destino = destino;
            this.peso = peso;
        }
    }

    /** Lo que se guarda en la priority queue: un vértice y la distancia con la que se encoló. */
    static class Nodo implements Comparable<Nodo> {
        final int vertice;
        final long distancia;

        Nodo(int vertice, long distancia) {
            this.vertice = vertice;
            this.distancia = distancia;
        }

        @Override
        public int compareTo(Nodo otro) {
            return Long.compare(this.distancia, otro.distancia);
        }
    }

    static class Resultado {
        final long[] dist;
        final int[] previo;

        Resultado(long[] dist, int[] previo) {
            this.dist = dist;
            this.previo = previo;
        }
    }

    /**
     * Versión sin priority queue.
     * V iteraciones, y en cada una se recorren los V vértices para buscar el mínimo
     * no visitado -> O(V^2). Relajar todas las aristas suma O(E). Total O(V^2).
     */
    static Resultado dijkstraIngenuo(List<List<Arista>> grafo, int origen) {
        int n = grafo.size();
        long[] dist = new long[n];
        int[] previo = new int[n];
        boolean[] visitado = new boolean[n];
        Arrays.fill(dist, INF);
        Arrays.fill(previo, -1);
        dist[origen] = 0;

        for (int i = 0; i < n; i++) {
            // Buscar el vértice no visitado con menor distancia: O(V)
            int u = -1;
            for (int v = 0; v < n; v++) {
                if (!visitado[v] && (u == -1 || dist[v] < dist[u])) {
                    u = v;
                }
            }
            if (u == -1 || dist[u] == INF) {
                break; // lo que queda es inalcanzable
            }
            visitado[u] = true;

            // Relajar las aristas salientes de u
            for (Arista a : grafo.get(u)) {
                if (dist[u] + a.peso < dist[a.destino]) {
                    dist[a.destino] = dist[u] + a.peso;
                    previo[a.destino] = u;
                }
            }
        }
        return new Resultado(dist, previo);
    }

    /**
     * Versión con priority queue (java.util.PriorityQueue = heap binario de mínimos).
     *
     * La única diferencia con la ingenua es CÓMO se obtiene el vértice de menor distancia:
     * en vez de recorrer el arreglo en O(V), se saca de la cola con poll() en O(log V).
     *
     * PriorityQueue no tiene "decrease-key" (bajar la prioridad de un elemento que ya
     * está adentro). Por eso se usa "lazy deletion": cuando se mejora la distancia de un
     * vértice se encola un Nodo NUEVO, y las entradas viejas que quedan en la cola se
     * descartan al sacarlas (if d > dist[u] continue).
     *
     * - poll(): O(log V), se hace como mucho una vez por cada offer().
     * - offer(): O(log V), se hace como mucho una vez por arista (solo si la relajación mejora).
     * Total: O((V + E) log V).
     */
    static Resultado dijkstraHeap(List<List<Arista>> grafo, int origen) {
        int n = grafo.size();
        long[] dist = new long[n];
        int[] previo = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(previo, -1);
        dist[origen] = 0;

        PriorityQueue<Nodo> cola = new PriorityQueue<>();
        cola.offer(new Nodo(origen, 0));

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            int u = actual.vertice;
            if (actual.distancia > dist[u]) {
                continue; // entrada desactualizada: ya se procesó u con una distancia menor
            }

            for (Arista a : grafo.get(u)) {
                long nueva = dist[u] + a.peso;
                if (nueva < dist[a.destino]) {
                    dist[a.destino] = nueva;
                    previo[a.destino] = u;
                    cola.offer(new Nodo(a.destino, nueva));
                }
            }
        }
        return new Resultado(dist, previo);
    }

    static List<Integer> reconstruirCamino(int[] previo, int destino) {
        List<Integer> camino = new ArrayList<>();
        for (int v = destino; v != -1; v = previo[v]) {
            camino.add(v);
        }
        Collections.reverse(camino);
        return camino;
    }

    // ---------- Utilidades para armar grafos ----------

    static List<List<Arista>> grafoVacio(int n) {
        List<List<Arista>> g = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            g.add(new ArrayList<>());
        }
        return g;
    }

    static void agregarAristaNoDirigida(List<List<Arista>> g, int u, int v, int peso) {
        g.get(u).add(new Arista(v, peso));
        g.get(v).add(new Arista(u, peso));
    }

    // ---------- Ejemplo ----------

    public static void main(String[] args) {
        String nombres = "ABCDEF"; // A=0, B=1, ..., F=5
        List<List<Arista>> g = grafoVacio(6);
        agregarAristaNoDirigida(g, 0, 1, 4);  // A-B
        agregarAristaNoDirigida(g, 0, 2, 2);  // A-C
        agregarAristaNoDirigida(g, 1, 2, 1);  // B-C
        agregarAristaNoDirigida(g, 1, 3, 5);  // B-D
        agregarAristaNoDirigida(g, 2, 3, 8);  // C-D
        agregarAristaNoDirigida(g, 2, 4, 10); // C-E
        agregarAristaNoDirigida(g, 3, 4, 2);  // D-E
        agregarAristaNoDirigida(g, 3, 5, 6);  // D-F
        agregarAristaNoDirigida(g, 4, 5, 2);  // E-F

        Resultado[] resultados = { dijkstraIngenuo(g, 0), dijkstraHeap(g, 0) };
        String[] titulos = { "Ingenuo O(V^2)", "Heap O((V+E) log V)" };

        for (int k = 0; k < 2; k++) {
            System.out.println("--- Dijkstra " + titulos[k] + " desde A ---");
            for (int v = 0; v < g.size(); v++) {
                StringBuilder camino = new StringBuilder();
                for (int x : reconstruirCamino(resultados[k].previo, v)) {
                    if (camino.length() > 0) camino.append(" -> ");
                    camino.append(nombres.charAt(x));
                }
                System.out.printf("  %c: distancia %2d   camino %s%n",
                        nombres.charAt(v), resultados[k].dist[v], camino);
            }
            System.out.println();
        }
    }
}
