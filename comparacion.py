"""
Compara empíricamente Dijkstra ingenuo vs Dijkstra con priority queue
sobre grafos aleatorios ralos (E ~ 5V) y densos (E ~ V^2 / 2).
"""

import random
import time

from dijkstra import dijkstra_heap, dijkstra_ingenuo


def grafo_aleatorio(n, aristas_por_vertice=None, seed=0):
    """Grafo no dirigido conexo. Si aristas_por_vertice es None, es completo (denso)."""
    rnd = random.Random(seed)
    g = {i: [] for i in range(n)}

    def agregar(u, v):
        w = rnd.randint(1, 100)
        g[u].append((v, w))
        g[v].append((u, w))

    # Camino base para garantizar conexidad
    for i in range(1, n):
        agregar(i - 1, i)

    if aristas_por_vertice is None:
        for u in range(n):
            for v in range(u + 2, n):
                agregar(u, v)
    else:
        for _ in range(n * aristas_por_vertice):
            u, v = rnd.randrange(n), rnd.randrange(n)
            if u != v:
                agregar(u, v)
    return g


def medir(f, g):
    t = time.perf_counter()
    resultado = f(g, 0)
    return time.perf_counter() - t, resultado


def comparar(titulo, tamanios, aristas_por_vertice):
    print(f"\n=== {titulo} ===")
    print(f"{'V':>6} {'E':>9} {'ingenuo (s)':>12} {'heap (s)':>10} {'aceleración':>12}")
    for n in tamanios:
        g = grafo_aleatorio(n, aristas_por_vertice)
        e = sum(len(a) for a in g.values()) // 2
        t1, (d1, _) = medir(dijkstra_ingenuo, g)
        t2, (d2, _) = medir(dijkstra_heap, g)
        assert d1 == d2, "Las dos versiones deberían dar las mismas distancias"
        print(f"{n:>6} {e:>9} {t1:>12.4f} {t2:>10.4f} {t1 / t2:>11.1f}x")


if __name__ == "__main__":
    comparar("Grafos RALOS (E ~ 5V)", [500, 1000, 2000, 4000, 8000], 5)
    comparar("Grafos DENSOS (completos, E ~ V^2/2)", [200, 400, 800, 1600], None)
