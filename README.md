``` Ruby
// ============================================================
// EXERCITIUL 5 — Clasa Student completa (C++17)
// ============================================================

#include <iostream>
#include <cstring>   // strlen, strcpy
using namespace std;

class Student {
private:
    char*  nume;
    int    varsta;
    double nota;

public:
    // --------------------------------------------------------
    // Constructor cu valori implicite
    // --------------------------------------------------------
    Student(const char* n = "Necunoscut", int v = 18, double nt = 5.0)
        : varsta(v), nota(nt)
    {
        nume = new char[strlen(n) + 1];
        strcpy(nume, n);
    }

    // --------------------------------------------------------
    // Constructor de copiere — deep copy
    // --------------------------------------------------------
    Student(const Student& alt)
        : varsta(alt.varsta), nota(alt.nota)
    {
        nume = new char[strlen(alt.nume) + 1];
        strcpy(nume, alt.nume);
        cout<<"Copy Constructor:"<<nume<<"copiat din"<<alt.nume<<endl;

    }

    // --------------------------------------------------------
    // Destructor
    // --------------------------------------------------------
    ~Student() {
        cout<<"Destructor:"<<nume<<"distrus"<<endl;
        delete[] nume;
        //A dangling pointer in C/C++ is a pointer that points to a memory location that is no longer valid (the memory was already freed or the variable no longer exists
        nume = nullptr;
    }

    // --------------------------------------------------------
    // operator= — 4 pasi
    // --------------------------------------------------------
    Student& operator=(const Student& alt) {
        if (this == &alt) return *this;    // auto-atribuire

        delete[] nume;                     // eliberam vechiul nume

        varsta = alt.varsta;
        nota   = alt.nota;
        nume   = new char[strlen(alt.nume) + 1];
        strcpy(nume, alt.nume);

        return *this;
    }

    // --------------------------------------------------------
    // operator<< si >>
    // --------------------------------------------------------
    friend ostream& operator<<(ostream& os, const Student& s) {
        os << "[" << s.nume << " | v=" << s.varsta << " | n=" << s.nota << "]";
        return os;
    }

    friend istream& operator>>(istream& is, Student& s) {
        char buf[256];
        cin >> buf >> s.varsta >> s.nota;

        delete[] s.nume;                   // realocare obligatorie
        s.nume = new char[strlen(buf) + 1];
        strcpy(s.nume, buf);

        return is;
    }

    // --------------------------------------------------------
    // ++ prefixat si postfixat — modifica nota
    // --------------------------------------------------------
    Student& operator++() {
        if (nota < 10.0) nota += 1.0;
        return *this;
    }

    Student operator++(int) {
        Student vechi = *this;
        if (nota < 10.0) nota += 1.0;
        return vechi;
    }

    // --------------------------------------------------------
    // -- prefixat si postfixat
    // --------------------------------------------------------
    Student& operator--() {
        if (nota > 1.0) nota -= 1.0;
        return *this;
    }

    Student operator--(int) {
        Student vechi = *this;
        if (nota > 1.0) nota -= 1.0;
        return vechi;
    }

    // --------------------------------------------------------
    // Comparatii C++17 — dupa nota
    // Implementam doar == si <, restul prin acestea
    // --------------------------------------------------------
    bool operator==(const Student& alt) const { return nota == alt.nota; }
    bool operator!=(const Student& alt) const { return !(*this == alt); }
    bool operator< (const Student& alt) const { return nota <  alt.nota; }
    bool operator<=(const Student& alt) const { return !(alt < *this); }
    bool operator> (const Student& alt) const { return alt < *this; }
    bool operator>=(const Student& alt) const { return !(*this < alt); }

    // --------------------------------------------------------
    // operator+ cu comutativitate — adauga bonus la nota
    // --------------------------------------------------------
    friend Student operator+(const Student& s, double bonus) {
        double notaNoua = s.nota + bonus;
        if (notaNoua > 10.0) notaNoua = 10.0;
        return Student(s.nume, s.varsta, notaNoua);
    }

    friend Student operator+(double bonus, const Student& s) {
        return s + bonus;   // delegam, nu duplicam logica
    }

    double getNota() const { return nota; }
};

// ── Swap pentru sortare ──────────────────────────────────────
void swapStudenti(Student& a, Student& b) {
    Student tmp = a;
    a = b;
    b = tmp;
}

// ════════════════════════════════════════════════════════════
int main() {

    // 1. Tablou de 5 studenti
    cout << "=== 1. Tablou initial ===\n";
    Student studenti[5] = {
        Student("Ana",   20, 9.5),
        Student("Ion",   21, 6.0),
        Student("Maria", 19, 8.5),
        Student("Mihai", 22, 4.0),
        Student("Elena", 20, 7.5)
    };
    for (int i = 0; i < 5; i++)
        cout << "  " << studenti[i] << "\n";

    // 2. Sortare cu bule dupa nota (operator<)
    cout << "\n=== 2. Dupa sortare ===\n";
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4 - i; j++)
            if (studenti[j + 1] < studenti[j])
                swapStudenti(studenti[j], studenti[j + 1]);

    for (int i = 0; i < 5; i++)
        cout << "  " << i + 1 << ". " << studenti[i] << "\n";

    // 3. Rule of Three
    cout << "\n=== 3. Rule of Three ===\n";
    Student s1("Test", 20, 7.0);
    Student s2 = s1;
    Student s3;
    s3 = s1;
    s1 = s1;   // auto-atribuire — safe
    cout << "s1: " << s1 << "\n";
    cout << "s2: " << s2 << "\n";
    cout << "s3: " << s3 << "\n";

    // 4. ++ si --
    cout << "\n=== 4. ++ si -- ===\n";
    Student t("T", 20, 7.0);
    cout << "initial:    " << t << "\n";
    cout << "t++ ret:    " << t++ << "  t acum: " << t << "\n";
    cout << "++t ret:    " << ++t << "  t acum: " << t << "\n";
    cout << "--t ret:    " << --t << "\n";

    // 5. Comparatii
    cout << "\n=== 5. Comparatii ===\n";
    Student sa("A", 20, 8.0), sb("B", 21, 9.0), sc("C", 19, 8.0);
    cout << boolalpha;
    cout << "sa=8  sb=9  sc=8\n";
    cout << "sa == sc : " << (sa == sc) << "\n";
    cout << "sa != sb : " << (sa != sb) << "\n";
    cout << "sa <  sb : " << (sa < sb)  << "\n";
    cout << "sa <= sc : " << (sa <= sc) << "\n";
    cout << "sb >  sa : " << (sb > sa)  << "\n";
    cout << "sa >= sc : " << (sa >= sc) << "\n";

    // 6. operator+ comutativ
    cout << "\n=== 6. operator+ comutativ ===\n";
    Student b1("Bonus", 20, 7.0);
    cout << "b1:       " << b1 << "\n";
    cout << "b1 + 1.5: " << (b1 + 1.5) << "\n";
    cout << "1.5 + b1: " << (1.5 + b1) << "\n";

    // 7. operator>>
    cout << "\n=== 7. Citire ===\n";
    Student nou;
    cout << "Introduceti student (Nume Varsta Nota): ";
    cin >> nou;
    cout << "Citit: " << nou << "\n";

    return 0;
}
```
