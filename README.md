# TP optativo – Matemática Discreta

Archivos (Java):

| Archivo | Contenido |
|---|---|
| `Dijkstra.java` | Dijkstra ingenuo (arreglo) y Dijkstra con `java.util.PriorityQueue` |
| `Kruskal.java` | Kruskal con Union-Find (compresión de caminos + unión por rango) |
| `Comparacion.java` | Medición de tiempos de las dos versiones de Dijkstra |

(Los `.py` son la misma implementación en Python.)

Compilar y ejecutar:

```
javac -encoding UTF-8 -d out Dijkstra.java Kruskal.java Comparacion.java
java -cp out Dijkstra
java -cp out Kruskal
java -cp out Comparacion
```

---

## 1. Dijkstra con priority queue: ¿mejora la complejidad? ¿Cuánto?

### Dónde está el cuello de botella
Dijkstra repite V veces: *"tomar el vértice no visitado con menor distancia y relajar
sus aristas"*. Relajar todas las aristas cuesta O(E) en total en cualquier versión.
Lo que cambia es **cómo se obtiene el mínimo**:

- **Ingenuo:** se recorre el arreglo `dist[]` entero → O(V) cada vez → V · O(V) = **O(V²)**.
- **Con priority queue:** los candidatos están en un heap de mínimos y el menor se
  saca con `poll()` en **O(log V)**.

### Qué priority queue se usa
Se usa `java.util.PriorityQueue`, que es un **heap binario de mínimos**:

| Operación | Costo |
|---|---|
| `offer(x)` (insertar) | O(log n) |
| `poll()` (sacar el mínimo) | O(log n) |
| `peek()` | O(1) |
| `remove(x)` (un elemento cualquiera) | O(n) |

Para que sepa ordenar, la clase `Nodo` (vértice, distancia) implementa
`Comparable<Nodo>` comparando por distancia.

### El problema: no hay *decrease-key*
El Dijkstra "de libro" con heap usa la operación *decrease-key*: cuando se mejora
`dist[v]`, se baja la prioridad de `v` dentro del heap. `PriorityQueue` **no la tiene**
(y `remove` + `offer` costaría O(V)).

La solución es la **eliminación perezosa (lazy deletion)**:
1. Cuando se mejora `dist[v]`, se inserta un `Nodo(v, nuevaDist)` **nuevo**. El viejo queda adentro.
2. Al hacer `poll()`, si `nodo.distancia > dist[nodo.vertice]`, es una entrada
   vieja y se descarta con `continue`.

```java
while (!cola.isEmpty()) {
    Nodo actual = cola.poll();                     // O(log V)
    int u = actual.vertice;
    if (actual.distancia > dist[u]) continue;      // entrada vieja: se ignora

    for (Arista a : grafo.get(u)) {
        long nueva = dist[u] + a.peso;
        if (nueva < dist[a.destino]) {             // relajación
            dist[a.destino] = nueva;
            previo[a.destino] = u;
            cola.offer(new Nodo(a.destino, nueva)); // O(log V)
        }
    }
}
```

**¿Por qué sigue siendo O(log V)?** Cada `offer` viene de una relajación exitosa, y
hay a lo sumo E, así que la cola tiene como mucho E elementos. Cada operación cuesta
O(log E), y como E ≤ V², log E ≤ 2 log V = **O(log V)**.

### Complejidad
- `poll()`: a lo sumo uno por cada `offer` → O(E log V)
- `offer()`: a lo sumo uno por arista → O(E log V)
- más O(V) de inicialización

**T(V, E) = O((V + E) log V)**, o sea O(E log V) si el grafo es conexo.

### ¿Cuánto mejora?
El cociente entre ambas es ≈ **V² / (E log V)**, así que depende de la densidad:

| Tipo de grafo | E | Ingenuo | Con heap | Resultado |
|---|---|---|---|---|
| Ralo (E ≈ k·V) | O(V) | O(V²) | O(V log V) | Mejora en un factor **V / log V** |
| Denso (E ≈ V²) | O(V²) | O(V²) | O(V² log V) | **No mejora**: empeora en un factor log V |

Ejemplo con V = 10.000 y E = 50.000: ingenuo ≈ 10⁸ operaciones, heap ≈
50.000 · log₂ 10.000 ≈ 6,6·10⁵ → unas **150 veces menos**.

Con un **heap de Fibonacci** (decrease-key en O(1) amortizado) se llega a
**O(E + V log V)**, mejor en todos los casos, pero Java no lo trae y en la práctica
sus constantes son altas.

### Resultados experimentales (`Comparacion.java`)

Grafos ralos (E ≈ 6V):

| V | E | ingenuo (ms) | heap (ms) | aceleración |
|---|---|---|---|---|
| 1.000 | 5.998 | 0,78 | 0,60 | 1,3x |
| 2.000 | 11.994 | 2,36 | 0,95 | 2,5x |
| 4.000 | 23.996 | 12,30 | 3,15 | 3,9x |
| 8.000 | 47.995 | 39,39 | 3,96 | 10,0x |
| 16.000 | 95.995 | 176,23 | 12,37 | 14,2x |
| 32.000 | 191.993 | 1.552,46 | 29,17 | 53,2x |

Al duplicar V, el ingenuo tarda ~4 veces más (cuadrático) y el heap ~2 veces más
(casi lineal). La ventaja crece con V, como predice V / log V.

Grafos densos (completos, E ≈ V²/2):

| V | E | ingenuo (ms) | heap (ms) | aceleración |
|---|---|---|---|---|
| 250 | 31.125 | 0,25 | 0,26 | 1,0x |
| 500 | 124.750 | 1,41 | 1,46 | 1,0x |
| 1.000 | 499.500 | 7,45 | 15,25 | 0,5x |
| 2.000 | 1.999.000 | 21,63 | 29,55 | 0,7x |

Acá **el ingenuo es igual o más rápido**, como dice la teoría: con E ≈ V² el heap
paga un log V extra y además crea un objeto `Nodo` por cada relajación exitosa.

(Los tiempos varían un poco entre corridas. La JVM se "calienta" antes de medir
porque el compilador JIT optimiza el código recién después de ejecutarlo varias veces.)

**Conclusión:** la priority queue mejora la complejidad de O(V²) a O((V+E) log V).
La mejora es grande (factor ~V / log V) en grafos ralos, que son los más comunes
(redes viales, redes de computadoras). En grafos densos no mejora y conviene la versión ingenua.

---

## 2. Kruskal

**Algoritmo:**
1. Ordenar las aristas por peso de menor a mayor (`Arrays.sort`, la clase `Arista` implementa `Comparable`).
2. Recorrerlas y agregar cada una al árbol si une dos componentes distintas
   (o sea, si no forma ciclo).
3. Terminar cuando el árbol tiene V − 1 aristas.

Para saber si dos vértices ya están en la misma componente se usa **Union-Find**
(dos arreglos `padre[]` y `rango[]`):
- `find(x)`: devuelve el representante del conjunto de x. Con **compresión de caminos**,
  cada nodo recorrido queda apuntando directo a la raíz.
- `union(a, b)`: une los dos conjuntos colgando el árbol de menor rango del de mayor
  (**unión por rango**). Si ya tenían la misma raíz devuelve `false`: esa arista formaría ciclo.

**Complejidad:**
- Ordenar: O(E log E) = O(E log V).
- E operaciones de Union-Find: O(E · α(V)), con α la inversa de Ackermann (≤ 4 en la práctica).
- **Total: O(E log V)**, dominado por el ordenamiento.

**Ejemplo** (`java -cp out Kruskal`), mismo grafo que el de Dijkstra:

```
B - C  (peso 1)
A - C  (peso 2)
D - E  (peso 2)
E - F  (peso 2)
B - D  (peso 5)
Peso total: 12
```

Se descartan A-B (4), D-F (6), C-D (8) y C-E (10) porque formarían ciclo.
