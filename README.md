// ============================================================
//  Clasa Vector2D — operatori aritmetici, scalare, I/O
//  Compilare: g++ -std=c++17 -Wall -Wextra -o vector2d vector2d.cpp
// ============================================================

#include <iostream>
#include <cmath>    // fabs — fara STL containers/algorithms

class Vector2D {
private:
    double x, y;

public:
    // ════════════════════════════════════════════════════════
    // 1. CONSTRUCTOR cu valori implicite (0.0, 0.0)
    // ════════════════════════════════════════════════════════
    // Parametrii au valori default → acelasi constructor
    // acopera: Vector2D(), Vector2D(3.0), Vector2D(3.0, 4.0)
    Vector2D(double x = 0.0, double y = 0.0)
        : x(x), y(y) {}

    // ── Getteri (utili in main pentru demonstratii) ──────────
    double getX() const { return x; }
    double getY() const { return y; }

    // ════════════════════════════════════════════════════════
    // 2. operator+ — suma a doi vectori (metoda membra)
    // ════════════════════════════════════════════════════════
    // Returnam o noua instanta (vectorii originali raman neschimbati).
    // Metoda membra: this = operandul stang, parametrul = operandul drept.
    Vector2D operator+(const Vector2D& o) const {
        return Vector2D(x + o.x, y + o.y);
    }

    // ════════════════════════════════════════════════════════
    // 3. operator* cu scalar — AMBELE forme
    // ════════════════════════════════════════════════════════

    // ── Forma   v * 2.0   (metoda membra) ────────────────────
    // Operandul STANG este obiectul, cel drept este scalarul.
    Vector2D operator*(double scalar) const {
        return Vector2D(x * scalar, y * scalar);
    }

    // ── Forma   2.0 * v   (functie friend) ───────────────────
    // Cand operandul STANG este un double (tip primitiv), compilatorul
    // nu poate apela o metoda membra pe el → avem nevoie de o functie
    // libera. Declarata friend pentru a accesa x si y private direct.
    // Implementam prin delegare la metoda membra → logica e intr-un singur loc.
    friend Vector2D operator*(double scalar, const Vector2D& v) {
        return v * scalar;   // delega la metoda membra de mai sus
    }

    // ════════════════════════════════════════════════════════
    // 4. operator<< — afisare in formatul (x, y)
    // ════════════════════════════════════════════════════════
    // Trebuie sa fie functie libera (operandul stang e std::ostream,
    // nu Vector2D). Declarata friend pentru acces la campurile private.
    // Returnam os& pentru a permite inlantuirea: cout << a << b << "\n"
    friend std::ostream& operator<<(std::ostream& os, const Vector2D& v) {
        os << "(" << v.x << ", " << v.y << ")";
        return os;
    }

    // ════════════════════════════════════════════════════════
    // 5. operator== si != cu toleranta epsilon pentru double
    // ════════════════════════════════════════════════════════
    // Comparatia exacta (==) este nesigura pentru double din cauza
    // erorilor de reprezentare in virgula mobila.
    // Solutie: doua valori sunt "egale" daca diferenta absoluta < 1e-9.
    static constexpr double EPSILON = 1e-9;

    bool operator==(const Vector2D& o) const {
        return fabs(x - o.x) < EPSILON &&
               fabs(y - o.y) < EPSILON;
    }

    // != exprimat prin ==, fara duplicare de logica
    bool operator!=(const Vector2D& o) const {
        return !(*this == o);
    }
};


// ============================================================
//  MAIN — demonstratii
// ============================================================
int main()
{
    std::cout << "=== 1. Constructori ===\n";
    Vector2D zero;                    // (0.0, 0.0) — valori implicite
    Vector2D a(3.0, 4.0);
    Vector2D b(1.5, -2.0);
    std::cout << "zero = " << zero << "\n";
    std::cout << "a    = " << a    << "\n";
    std::cout << "b    = " << b    << "\n\n";

    std::cout << "=== 2. Suma vectorilor (operator+) ===\n";
    Vector2D suma = a + b;
    std::cout << "a + b = " << a << " + " << b << " = " << suma << "\n\n";

    std::cout << "=== 3. Produs cu scalar comutativitate ===\n";
    double scalar = 2.0;

    Vector2D v1 = a * scalar;         // v * 2.0  →  metoda membra
    Vector2D v2 = scalar * a;         // 2.0 * v  →  functie friend

    std::cout << "a * 2.0 = " << v1 << "\n";
    std::cout << "2.0 * a = " << v2 << "\n";

    // Demonstram comutativitatea: ambele forme dau acelasi rezultat
    if (v1 == v2)
        std::cout << "=> v * 2.0 == 2.0 * v   (comutativitate confirmata)\n\n";
    else
        std::cout << "=> EROARE: rezultatele difera!\n\n";

    std::cout << "=== 4. operator<< (inlantuire) ===\n";
    std::cout << "a=" << a << "  b=" << b << "  suma=" << (a + b) << "\n\n";

    std::cout << "=== 5. operator== si != cu toleranta 1e-9 ===\n";
    Vector2D c(3.0, 4.0);
    Vector2D d(3.0 + 1e-10, 4.0);    // diferenta sub epsilon → "egal"
    Vector2D e(3.0 + 1e-8,  4.0);    // diferenta peste epsilon → "diferit"

    std::cout << "a       = " << a << "\n";
    std::cout << "c       = " << c << "  (identic cu a)\n";
    std::cout << "d       = (3.0+1e-10, 4.0)  (diferenta < 1e-9)\n";
    std::cout << "e       = (3.0+1e-8,  4.0)  (diferenta > 1e-9)\n\n";

    std::cout << "a == c  : " << (a == c ? "DA" : "NU") << "  (exact egale)\n";
    std::cout << "a == d  : " << (a == d ? "DA" : "NU") << "  (sub toleranta)\n";
    std::cout << "a == e  : " << (a == e ? "DA" : "NU") << "  (peste toleranta)\n";
    std::cout << "a != e  : " << (a != e ? "DA" : "NU") << "\n\n";

    std::cout << "=== 6. Compunere de operatii ===\n";
    // (a + b) * 3.0  si  3.0 * (a + b)  — combina + cu ambele forme *
    Vector2D r1 = (a + b) * 3.0;
    Vector2D r2 = 3.0 * (a + b);
    std::cout << "(a+b)*3.0 = " << r1 << "\n";
    std::cout << "3.0*(a+b) = " << r2 << "\n";
    std::cout << "Egale: " << (r1 == r2 ? "DA" : "NU") << "\n";

    return 0;
}
