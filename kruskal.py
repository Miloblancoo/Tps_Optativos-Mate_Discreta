"""
Kruskal: árbol de expansión mínima (AEM / MST) de un grafo no dirigido y ponderado.

Idea: ordenar las aristas de menor a mayor peso e ir agregándolas al árbol
siempre que NO formen un ciclo. Para detectar ciclos rápido se usa la
estructura Union-Find (conjuntos disjuntos).

Complejidad: O(E log E) por el ordenamiento (= O(E log V), ya que E <= V^2).
Las operaciones de Union-Find con compresión de caminos y unión por rango
cuestan O(α(V)) amortizado, donde α es la inversa de Ackermann (prácticamente
constante), así que no cambian el orden total.
"""


class UnionFind:
    def __init__(self, elementos):
        self.padre = {x: x for x in elementos}
        self.rango = {x: 0 for x in elementos}

    def find(self, x):
        # Compresión de caminos: cada nodo apunta directo a la raíz
        if self.padre[x] != x:
            self.padre[x] = self.find(self.padre[x])
        return self.padre[x]

    def union(self, a, b):
        """Une los conjuntos de a y b. Devuelve False si ya estaban juntos."""
        ra, rb = self.find(a), self.find(b)
        if ra == rb:
            return False
        # Unión por rango: el árbol más bajo cuelga del más alto
        if self.rango[ra] < self.rango[rb]:
            ra, rb = rb, ra
        self.padre[rb] = ra
        if self.rango[ra] == self.rango[rb]:
            self.rango[ra] += 1
        return True


def kruskal(vertices, aristas):
    """
    vertices: iterable de vértices
    aristas:  lista de tuplas (u, v, peso)
    Devuelve (lista de aristas del AEM, peso total).
    Si el grafo no es conexo devuelve un bosque de expansión mínima.
    """
    uf = UnionFind(vertices)
    arbol = []
    total = 0
    n = len(uf.padre)

    for u, v, peso in sorted(aristas, key=lambda a: a[2]):
        if uf.union(u, v):  # no forma ciclo
            arbol.append((u, v, peso))
            total += peso
            if len(arbol) == n - 1:
                break  # ya tenemos V-1 aristas: el árbol está completo

    return arbol, total


if __name__ == "__main__":
    vertices = ["A", "B", "C", "D", "E", "F"]
    aristas = [
        ("A", "B", 4), ("A", "C", 2), ("B", "C", 1), ("B", "D", 5),
        ("C", "D", 8), ("C", "E", 10), ("D", "E", 2), ("D", "F", 6),
        ("E", "F", 2),
    ]

    arbol, total = kruskal(vertices, aristas)
    print("--- Kruskal: árbol de expansión mínima ---")
    for u, v, peso in arbol:
        print(f"  {u} - {v}  (peso {peso})")
    print(f"  Peso total: {total}")
