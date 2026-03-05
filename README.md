// ============================================================
//  Clasa Contor — operatori de incrementare & comparatie
//  Compilare C++17:  g++ -std=c++17 -o contor contor.cpp
//  Compilare C++20:  g++ -std=c++20 -o contor contor.cpp
// ============================================================

#include <iostream>
#include <compare>   // necesar pentru <=> (C++20)

using namespace std;

class Contor {
private:
    int valoare;

public:
    // ── Constructor ──────────────────────────────────────────
    Contor(int v = 0) : valoare(v) {}

    // ── Getter ───────────────────────────────────────────────
    int getVal() const { return valoare; }

    // ════════════════════════════════════════════════════════
    // 1. OPERATORI DE INCREMENTARE / DECREMENTARE
    // ════════════════════════════════════════════════════════

    // ── Prefix ++c ───────────────────────────────────────────
    // Incrementeaza inainte, returneaza *this dupa modificare.
    // Returnam referinta, nu copie → eficient si consistent cu
    // semantica standard (permite (++c) = altceva).
    Contor& operator++() {
        ++valoare;
        return *this;
    }

    // ── Postfix c++ ──────────────────────────────────────────
    // Parametrul dummy „int" distinge postfix de prefix.
    // Salveaza starea VECHE, incrementeaza intern, returneaza
    // copia veche → de aceea returnam valoare, nu referinta.
    Contor operator++(int) {
        Contor vechi = *this;   // copie a starii curente
        ++valoare;              // modifica obiectul original
        return vechi;           // returneaza starea dinainte
    }

    // ── Prefix --c ───────────────────────────────────────────
    Contor& operator--() {
        --valoare;
        return *this;
    }

    // ── Postfix c-- ──────────────────────────────────────────
    Contor operator--(int) {
        Contor vechi = *this;
        --valoare;
        return vechi;
    }

    // ════════════════════════════════════════════════════════
    // 3. OPERATORI DE COMPARATIE — C++17
    //    Definim == si < cu logica reala.
    //    Ceilalti 4 sunt derivati din acestia doi.
    // ════════════════════════════════════════════════════════

    // ── Egalitate ────────────────────────────────────────────
    bool operator==(const Contor& o) const {
        return valoare == o.valoare;          // logica reala
    }

    // ── Mai mic strict ───────────────────────────────────────
    bool operator<(const Contor& o) const {
        return valoare < o.valoare;           // logica reala
    }

    // ── Diferit: exprimat prin == ─────────────────────────────
    bool operator!=(const Contor& o) const {
        return !(*this == o);                 // derivat din ==
    }

    // ── Mai mic sau egal: exprimat prin < si == ──────────────
    bool operator<=(const Contor& o) const {
        return (*this < o) || (*this == o);   // derivat
    }

    // ── Mai mare strict: exprimat prin < (inversand operanzii)
    bool operator>(const Contor& o) const {
        return o < *this;                     // derivat din <
    }

    // ── Mai mare sau egal: exprimat prin < ───────────────────
    bool operator>=(const Contor& o) const {
        return !(*this < o);                  // derivat din <
    }
};

int main()
{
    cout << "=== 1. Prefix vs Postfix ++ ===\n";
    {
        // ── Prefix ++c ───────────────────────────────────────
        // operator++() modifica c INAINTE de a returna *this.
        // Deci getVal() vede valoarea DEJA incrementata.
        Contor c(10);
        int x = (++c).getVal();
        // c.valoare = 11 chiar in momentul returnarii
        cout << "c porneste de la 10\n";
        cout << "x = (++c).getVal() => x = " << x
                  << "  (c = " << c.getVal() << ")\n";
        // x == 11, c == 11

        // ── Postfix c++ ──────────────────────────────────────
        // operator++(int) salveaza o copie a lui c (valoare=11),
        // incrementeaza originalul (c devine 12),
        // returneaza COPIA cea veche.
        // getVal() este apelat pe COPIE → vede valoarea veche.
        int y = (c++).getVal();
        cout << "y = (c++).getVal() => y = " << y
                  << "  (c = " << c.getVal() << ")\n";
        // y == 11 (valoarea dinaintea incrementarii), c == 12

        cout << "\n  CONCLUZIE:\n"
                  << "  ++c returneaza *this modificat  -> getVal() = valoare noua\n"
                  << "  c++ returneaza copie veche      -> getVal() = valoare veche\n\n";
    }

    return 0;
}


// ============================================================
// BONUS — C++20: spaceship operator <=>
//
// Inlocuim cei 6 operatori cu == si <=>.
// Compilatorul genereaza automat !=, <, <=, >, >= din acestia.
// ============================================================

/*  --- Varianta C++20 ---  (decomentati si compilati cu -std=c++20)

class ContorCpp20 {
private:
    int valoare;

public:
    explicit ContorCpp20(int v = 0) : valoare(v) {}
    int getVal() const { return valoare; }

    // ++ / -- identice cu versiunea C++17 (omise pentru concizie)

    // ── Egalitate: definita manual ────────────────────────────
    // (necesara deoarece daca definim <=>, == nu este generat
    //  automat decat daca scriem explicit `= default` sau o
    //  declaram separat)
    bool operator==(const ContorCpp20& o) const {
        return valoare == o.valoare;
    }

    // ── Three-way comparison (spaceship) ─────────────────────
    // std::strong_ordering este potrivit pentru int (ordine totala,
    // fara valori incomparabile, fara "echivalenta" ne-egala).
    //
    // Din <=> compilatorul sintetizeaza automat:
    //   <, <=, >, >=
    // (si != daca nu e definit manual, folosind ==)
    std::strong_ordering operator<=>(const ContorCpp20& o) const {
        return valoare <=> o.valoare;
    }
};

// --- Verificare C++20 ---
void demoCpp20() {
    ContorCpp20 a(3), b(5), c(3);
    auto yn = [](bool v) { return v ? "DA" : "NU"; };

    std::cout << "\n=== BONUS C++20 <=> ===\n";
    std::cout << "a=3, b=5, c=3\n";
    // Operatorii de mai jos sunt generati AUTOMAT de compilator:
    std::cout << "a == c : " << yn(a == c) << "\n";   // definit manual
    std::cout << "a != b : " << yn(a != b) << "\n";   // generat din ==
    std::cout << "a <  b : " << yn(a <  b) << "\n";   // generat din <=>
    std::cout << "a <= c : " << yn(a <= c) << "\n";   // generat din <=>
    std::cout << "b >  a : " << yn(b >  a) << "\n";   // generat din <=>
    std::cout << "b >= a : " << yn(b >= a) << "\n";   // generat din <=>
}

*/
