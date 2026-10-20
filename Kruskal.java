import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Kruskal: árbol de expansión mínima (AEM) de un grafo no dirigido y ponderado.
 *
 * Idea: ordenar las aristas de menor a mayor peso e ir agregándolas al árbol
 * siempre que NO formen un ciclo. Para detectar ciclos rápido se usa Union-Find.
 *
 * Complejidad: O(E log E) por el ordenamiento (= O(E log V), ya que E <= V^2).
 * Union-Find con compresión de caminos y unión por rango cuesta O(α(V))
 * amortizado por operación (α = inversa de Ackermann, prácticamente constante).
 */
public class Kruskal {

    static class Arista implements Comparable<Arista> {
        final int u, v, peso;

        Arista(int u, int v, int peso) {
            this.u = u;
            this.v = v;
            this.peso = peso;
        }

        @Override
        public int compareTo(Arista otra) {
            return Integer.compare(this.peso, otra.peso);
        }
    }

    static class UnionFind {
        private final int[] padre;
        private final int[] rango;

        UnionFind(int n) {
            padre = new int[n];
            rango = new int[n];
            for (int i = 0; i < n; i++) {
                padre[i] = i; // cada vértice empieza en su propio conjunto
            }
        }

        int find(int x) {
            // Compresión de caminos: cada nodo queda apuntando directo a la raíz
            if (padre[x] != x) {
                padre[x] = find(padre[x]);
            }
            return padre[x];
        }

        /** Une los conjuntos de a y b. Devuelve false si ya estaban en el mismo (=> ciclo). */
        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) {
                return false;
            }
            // Unión por rango: el árbol más bajo se cuelga del más alto
            if (rango[ra] < rango[rb]) {
                int tmp = ra; ra = rb; rb = tmp;
            }
            padre[rb] = ra;
            if (rango[ra] == rango[rb]) {
                rango[ra]++;
            }
            return true;
        }
    }

    /**
     * Devuelve las aristas del AEM. Si el grafo no es conexo, devuelve un
     * bosque de expansión mínima.
     */
    static List<Arista> kruskal(int n, Arista[] aristas) {
        Arista[] ordenadas = aristas.clone();
        Arrays.sort(ordenadas); // O(E log E)

        UnionFind uf = new UnionFind(n);
        List<Arista> arbol = new ArrayList<>();

        for (Arista a : ordenadas) {
            if (uf.union(a.u, a.v)) { // no forma ciclo
                arbol.add(a);
                if (arbol.size() == n - 1) {
                    break; // ya hay V-1 aristas: árbol completo
                }
            }
        }
        return arbol;
    }

    public static void main(String[] args) {
        String nombres = "ABCDEF"; // A=0, B=1, ..., F=5
        Arista[] aristas = {
            new Arista(0, 1, 4), new Arista(0, 2, 2), new Arista(1, 2, 1),
            new Arista(1, 3, 5), new Arista(2, 3, 8), new Arista(2, 4, 10),
            new Arista(3, 4, 2), new Arista(3, 5, 6), new Arista(4, 5, 2),
        };

        List<Arista> arbol = kruskal(6, aristas);
        int total = 0;
        System.out.println("--- Kruskal: árbol de expansión mínima ---");
        for (Arista a : arbol) {
            System.out.printf("  %c - %c  (peso %d)%n",
                    nombres.charAt(a.u), nombres.charAt(a.v), a.peso);
            total += a.peso;
        }
        System.out.println("  Peso total: " + total);
    }
}
