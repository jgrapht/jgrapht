# Plan de Test Complet pour LouvainClustering

## Objectifs

Ce plan de test vise à valider :
1. **Correction fonctionnelle** : L'algorithme détecte correctement les communautés
2. **Robustesse** : Gestion des cas limites et graphes variés
3. **Performance** : Vérification de la complexité O(n log n)
4. **Qualité** : Maximisation de la modularité

---

## Structure des Tests

### 1. Tests Fonctionnels de Base (BasicFunctionalTests)

#### Test 1.1 : Graphe vide
- **Entrée** : Graphe sans sommets
- **Résultat attendu** : 0 communautés
- **Justification** : Cas limite trivial

#### Test 1.2 : Sommet isolé
- **Entrée** : Graphe avec 1 sommet, 0 arête
- **Résultat attendu** : 1 communauté contenant ce sommet
- **Justification** : Cas minimal

#### Test 1.3 : Deux sommets déconnectés
- **Entrée** : 2 sommets, 0 arête
- **Résultat attendu** : 2 communautés distinctes
- **Justification** : Sommets isolés restent séparés

#### Test 1.4 : Graphe complet
- **Entrée** : Graphe complet K₅ (5 sommets, tous connectés)
- **Résultat attendu** : 1 communauté
- **Justification** : Pas de structure modulaire évidente

---

### 2. Tests de Détection de Communautés (CommunityDetectionTests)

#### Test 2.1 : Karate Club de Zachary
- **Entrée** : Graphe social classique de 34 sommets
- **Résultat attendu** : 2-4 communautés
- **Justification** : Benchmark standard en détection de communautés
- **Structure** : 
  - Groupe 1 : Autour de M. Hi (instructeur)
  - Groupe 2 : Autour de l'officier
  - Split historique réel du club

#### Test 2.2 : Trois cliques denses avec ponts
- **Entrée** : 
  - 3 cliques de 5 sommets (K₅)
  - Arêtes de pont entre cliques
- **Résultat attendu** : 3 communautés distinctes
- **Justification** : Structure modulaire claire
- **Schéma** :
```
[Clique 1] --pont-- [Clique 2] --pont-- [Clique 3]
```

#### Test 2.3 : Grille 10×10
- **Entrée** : Graphe grille avec voisins 4-connexes
- **Résultat attendu** : Multiple communautés (entre 2 et 100)
- **Justification** : Structure géométrique avec régions naturelles

---

### 3. Tests sur Graphes Pondérés (WeightedGraphTests)

#### Test 3.1 : Poids forts maintiennent les sommets ensemble
- **Entrée** :
```
0 --[10.0]-- 1 --[0.1]-- 2 --[10.0]-- 3
```
- **Résultat attendu** : 2 communautés {0,1} et {2,3}
- **Justification** : Lien faible (0.1) sépare les groupes

#### Test 3.2 : Poids uniformes = graphe non pondéré
- **Entrée** : Graphe avec tous les poids = 1.0
- **Résultat attendu** : Comportement identique au non-pondéré
- **Justification** : Vérification de la cohérence

---

### 4. Tests du Paramètre de Résolution (ResolutionParameterTests)

#### Test 4.1 : Résolution faible (γ = 0.5)
- **Effet** : Crée **moins de communautés, plus grandes**
- **Justification** : Favorise la fusion des communautés

#### Test 4.2 : Résolution élevée (γ = 2.0)
- **Effet** : Crée **plus de communautés, plus petites**
- **Justification** : Favorise la division des communautés

#### Test 4.3 : Résolution très faible (γ = 0.1)
- **Résultat attendu** : Potentiellement 1 seule grande communauté
- **Justification** : Test des extrêmes

**Relation attendue** :
```
Nombre de clusters (γ=0.5) ≤ Nombre de clusters (γ=1.0) ≤ Nombre de clusters (γ=2.0)
```

---

### 5. Tests de Cas Limites (EdgeCaseTests)

#### Test 5.1 : Self-loops (boucles)
- **Entrée** : Graphe avec arêtes v→v
- **Résultat attendu** : Pas de crash, traitement correct
- **Justification** : Certains graphes réels ont des self-loops

#### Test 5.2 : Graphe en étoile
- **Structure** :
```
    v1  v2  v3
     \ | /
  v10--v0--v4
     / | \
    v9 v8  v5
```
- **Justification** : Topologie hiérarchique

#### Test 5.3 : Chaîne linéaire
- **Structure** : v₀ - v₁ - v₂ - ... - v₁₉
- **Justification** : Graphe 1D, faible modularité

#### Test 5.4 : Cycle
- **Structure** : v₀ - v₁ - ... - v₁₉ - v₀
- **Justification** : Graphe régulier sans structure claire

---

### 6. Tests de Déterminisme (DeterminismTests)

#### Test 6.1 : Reproductibilité avec même seed
- **Procédure** :
  1. Exécuter avec Random(42)
  2. Exécuter à nouveau avec Random(42)
- **Résultat attendu** : Communautés identiques
- **Justification** : Déterminisme essentiel pour debugger

#### Test 6.2 : Cache fonctionne
- **Procédure** :
  1. Appeler getClustering() deux fois
- **Résultat attendu** : Même instance retournée
- **Justification** : Performance et cohérence

---

## Tests de Performance

### 7. Tests de Complexité Temporelle

#### Test 7.1 : Petit graphe (n=100, m=300)
- **Timeout** : < 1 seconde
- **Baseline** : Référence pour les mesures

#### Test 7.2 : Graphe moyen (n=500, m=1500)
- **Timeout** : < 5 secondes
- **Ratio attendu** : ~5× baseline

#### Test 7.3 : Grand graphe (n=1000, m=3000)
- **Timeout** : < 15 secondes
- **Ratio attendu** : ~10× baseline

#### Test 7.4 : Vérification O(n log n)
- **Procédure** :
  1. Mesurer temps pour n = 100, 150, 200, 300, 400, 600, 800, 1200, 1600, 2000 (10 points de mesure)
  2. Calculer ratios t(n2)/t(n1) pour chaque transition
- **Résultat attendu** :
```
Pour O(n log n), ratio théorique = (n2/n1) × log(n2)/log(n1)
```
- **Tolérance** : Facteur 4× (due aux constantes cachées et variance pour petits graphes)

**Formule de vérification** :
```
Si t(n) = c × n × log(n), alors
t(n2)/t(n1) = (n2/n1) × (log(n2)/log(n1))

Exemples:
- n=100 à n=150: ratio théorique ≈ 1.63
- n=400 à n=600: ratio théorique ≈ 1.60
- n=800 à n=1200: ratio théorique ≈ 1.59
```

#### Test 7.5 : Graphe dense (n=500, m=62500)
- **Densité** : ~50% des arêtes possibles
- **Timeout** : < 20 secondes
- **Justification** : Pire cas en termes de complexité

#### Test 7.6 : Graphe sparse (n=1000, m=2000)
- **Densité** : m ≈ 2n (très sparse)
- **Timeout** : < 5 secondes
- **Justification** : Cas optimal pour Louvain

---

## Métriques de Performance Attendues

| Taille du graphe | Sommets (n) | Arêtes (m) | Temps attendu | Complexité |
|------------------|-------------|------------|---------------|------------|
| Petit            | 100         | 300        | < 1s          | Baseline   |
| Moyen            | 500         | 1,500      | < 5s          | ~5× base   |
| Grand            | 1,000       | 3,000      | < 15s         | ~10× base  |
| Dense            | 500         | 62,500     | < 20s         | Pire cas   |
| Sparse           | 1,000       | 2,000      | < 5s          | Optimal    |
| Très grand       | 5,000       | 15,000     | < 60s         | Scalabilité|

---

## Graphes de Test Spécifiques

### Karate Club (Zachary, 1977)
```
Sommets : 34 membres du club
Arêtes : 78 interactions sociales
Communautés attendues : 2 (split historique)
Modularité attendue : Q ≈ 0.42
```

### Graphe modulaire synthétique
```
Structure : k modules de taille n
Densité intra-module : 70%
Densité inter-module : 5%
Résultat attendu : k communautés détectées
```

### Grid 2D
```
Disposition : Grille régulière
Voisinage : 4-connexe (haut, bas, gauche, droite)
Communautés attendues : Régions carrées
```

---

## Validation de Résultats

### Critères de succès :

1. **Correction** :
   - Tous les sommets sont assignés
   - Pas de communautés vides
   - Communautés disjointes

2. **Performance** :
   - Temps < limites spécifiées
   - Scaling ≈ O(n log n)

3. **Robustesse** :
   - Pas de crash sur cas limites
   - Résultats stables (même seed → mêmes résultats)

---

## Exécution des Tests

### Commande Maven :
```bash
# Tous les tests
mvn test -Dtest=LouvainClusteringComprehensiveTest

# Tests fonctionnels uniquement
mvn test -Dtest=LouvainClusteringComprehensiveTest#BasicFunctionalTests

# Tests de performance uniquement
mvn test -Dtest=LouvainClusteringComprehensiveTest#PerformanceTests

# Test de complexité spécifique
mvn test -Dtest=LouvainClusteringComprehensiveTest#testComplexityScaling
```



## Checklist de Couverture

- [x] Graphes vides et triviaux
- [x] Graphes avec structure claire (cliques)
- [x] Graphes sans structure (complet, aléatoire)
- [x] Graphes pondérés vs non-pondérés
- [x] Paramètre de résolution (faible, normal, élevé)
- [x] Cas limites (self-loops, étoile, chaîne, cycle)
- [x] Déterminisme et cache
- [x] Performance sur différentes tailles
- [x] Vérification de complexité O(n log n)
- [x] Graphes denses vs sparse
- [x] Benchmark réaliste (Karate Club)

---

## Rapport de Test Attendu

```
╔════════════════════════════════════════╗
║  LOUVAIN CLUSTERING TEST REPORT       ║
╠════════════════════════════════════════╣
║ Total tests: 24                        ║
║ Passed: 24                             ║
║ Failed: 0                              ║
║ Skipped: 0                             ║
╠════════════════════════════════════════╣
║ Coverage:                              ║
║   - Functional correctness: ✓          ║
║   - Edge cases: ✓                      ║
║   - Performance: ✓                     ║
║   - Complexity verification: ✓         ║
╠════════════════════════════════════════╣
║ Performance Summary:                   ║
║   - Small graphs (100v): < 1s ✓        ║
║   - Medium graphs (500v): < 5s ✓       ║
║   - Large graphs (1000v): < 15s ✓      ║
║   - Complexity: O(n log n) ✓           ║
╚════════════════════════════════════════╝
```

---

## Tests Additionnels Recommandés

### Tests de régression :
1. Comparer avec résultats connus (benchmarks publiés)
2. Vérifier stabilité entre versions

### Tests de stress :
1. Graphes très grands (n > 10,000)
2. Graphes très denses (> 80% d'arêtes)
3. Graphes avec poids extrêmes (0.001 à 10000)

### Tests de cas réels :
1. Réseaux sociaux (Facebook, Twitter)
2. Réseaux biologiques (protéines)
3. Graphes de citations (papiers scientifiques)

---

**Auteur** : Plan créé pour validation complète de l'implémentation Louvain  
**Version** : 1.0  
**Date** : Novembre 2025
