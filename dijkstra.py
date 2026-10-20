"""
Dijkstra: camino mínimo desde un origen en un grafo con pesos NO negativos.

Se implementan dos versiones para poder comparar complejidades:

  1. dijkstra_ingenuo     -> busca el mínimo recorriendo un arreglo: O(V^2)
  2. dijkstra_heap        -> usa una priority queue (heap binario): O((V + E) log V)

El grafo se representa como lista de adyacencia:
    grafo = {vertice: [(vecino, peso), ...], ...}
"""

import heapq

INF = float("inf")


def dijkstra_ingenuo(grafo, origen):
    """Versión clásica sin priority queue.

    En cada iteración se recorren todos los vértices para encontrar el de
    menor distancia no visitado -> O(V) por iteración, V iteraciones -> O(V^2).
    Relajar las aristas cuesta O(E) en total. Total: O(V^2 + E) = O(V^2).
    """
    dist = {v: INF for v in grafo}
    previo = {v: None for v in grafo}
    visitado = {v: False for v in grafo}
    dist[origen] = 0

    for _ in range(len(grafo)):
        # Buscar el vértice no visitado con distancia mínima: O(V)
        u = None
        for v in grafo:
            if not visitado[v] and (u is None or dist[v] < dist[u]):
                u = v
        if u is None or dist[u] == INF:
            break  # el resto de los vértices es inalcanzable
        visitado[u] = True

        # Relajar aristas salientes de u
        for vecino, peso in grafo[u]:
            if dist[u] + peso < dist[vecino]:
                dist[vecino] = dist[u] + peso
                previo[vecino] = u

    return dist, previo


def dijkstra_heap(grafo, origen):
    """Versión con priority queue (heapq = heap binario de mínimos).

    - Cada vértice se extrae del heap (heappop): O(log V).
    - Cada relajación exitosa inserta en el heap (heappush): O(log V).
      Como hay a lo sumo E relajaciones exitosas, el heap tiene O(E) elementos
      y log E <= log V^2 = 2 log V, así que sigue siendo O(log V).
    Total: O((V + E) log V).

    Se usa "lazy deletion": en vez de hacer decrease-key, se inserta un nuevo
    par (dist, v) y al sacar uno desactualizado simplemente se descarta.
    """
    dist = {v: INF for v in grafo}
    previo = {v: None for v in grafo}
    dist[origen] = 0
    heap = [(0, origen)]

    while heap:
        d, u = heapq.heappop(heap)
        if d > dist[u]:
            continue  # entrada vieja, ya encontramos un camino mejor

        for vecino, peso in grafo[u]:
            nueva = d + peso
            if nueva < dist[vecino]:
                dist[vecino] = nueva
                previo[vecino] = u
                heapq.heappush(heap, (nueva, vecino))

    return dist, previo


def reconstruir_camino(previo, destino):
    camino = []
    while destino is not None:
        camino.append(destino)
        destino = previo[destino]
    return camino[::-1]


if __name__ == "__main__":
    grafo = {
        "A": [("B", 4), ("C", 2)],
        "B": [("A", 4), ("C", 1), ("D", 5)],
        "C": [("A", 2), ("B", 1), ("D", 8), ("E", 10)],
        "D": [("B", 5), ("C", 8), ("E", 2), ("F", 6)],
        "E": [("C", 10), ("D", 2), ("F", 2)],
        "F": [("D", 6), ("E", 2)],
    }

    for nombre, algoritmo in [("Ingenuo O(V^2)", dijkstra_ingenuo),
                              ("Heap O((V+E) log V)", dijkstra_heap)]:
        dist, previo = algoritmo(grafo, "A")
        print(f"--- Dijkstra {nombre} desde A ---")
        for v in grafo:
            camino = " -> ".join(reconstruir_camino(previo, v))
            print(f"  {v}: distancia {dist[v]:>2}   camino {camino}")
        print()
