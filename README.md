#include <cstring>
#include <iostream>
using namespace std;

class Carte {
private:
    char *titlu;
    char *autor;
    int anPublicare;

public:
    Carte(const char *t = "Anonim", const char *a = "Anonimus", int an = 0) : anPublicare(an) {
        titlu = new char[strlen(t) + 1];
        autor = new char[strlen(a) + 1];
        strcpy(this->titlu, t);
        strcpy(autor, a);
    }

    Carte(const Carte &alt) : anPublicare(alt.anPublicare) {
        titlu = new char[strlen(alt.titlu) + 1];
        autor = new char[strlen(alt.autor) + 1];
        strcpy(this->titlu, alt.titlu);
        strcpy(autor, alt.autor);
        cout << "[Copy ctor]   \"" << titlu << "\" copiat din \""
                << alt.titlu << "\"\n";
    }

    ~Carte() {
        cout << "[Destructor] \"" << titlu << "\" distrus\n";
        delete[] titlu;
        titlu = nullptr;
        delete[] autor;
        autor = nullptr;
    }

    Carte &operator=(const Carte &alt) {
        if (this == &alt) {
            cout << "[operator=]   Auto-atribuire detectata, ignorata!\n";
            return *this;
        }

        delete[] titlu;
        delete[] autor;

        titlu = new char[strlen(alt.titlu) + 1];
        strcpy(titlu, alt.titlu);

        autor = new char[strlen(alt.autor) + 1];
        strcpy(autor, alt.autor);

        anPublicare = alt.anPublicare;

        cout << "[operator=]   \"" << titlu << "\" atribuit\n";
        return *this;
    }

    friend ostream &operator<<(ostream &os, const Carte &c) {
        os << "\"" << c.titlu << "\" de " << c.autor
                << " (" << c.anPublicare << ")";
        return os;
    }

    friend istream &operator>>(istream &is, Carte &c) {
        char buf[256];

        cout << "  Titlu : ";
        is >> buf;
        delete[] c.titlu;
        c.titlu = new char[strlen(buf) + 1];
        strcpy(c.titlu, buf);

        cout << "  Autor : ";
        is >> buf;
        delete[] c.autor;
        c.autor = new char[strlen(buf) + 1];
        strcpy(c.autor, buf);

        cout << "  An    : ";
        is >> c.anPublicare;

        return is;
    }
};

int main() {
    // In main: creati 3 carti, copiati una in alta, testati auto-atribuirea, verificati ca
    // destructorii se apeleaza.
    Carte c1("Micul Print", "Antoine de Saint", 1943);
    Carte c2("Povestea slujitoarei", "Margaret Atwood", 1985);
    Carte c3("1984", "George Orwell", 1949);
    cout << "--- Copiere carte ---" << endl;
    Carte c4 = c1; // copy ctor
    cout << "--- Atribuire carte ---" << endl;
    c2 = c3; // operator=
    cout << "--- Auto-atribuire carte ---" << endl;
    c3 = c3; // auto-atribuire
    cout << "======= Afisare cu << =======" << endl;
    cout << c1 << endl;
    cout << c2 << endl;
    cout << c3 << endl;
    cout << "======= Citire carte noua cu >> =======" << endl;
    Carte c10;
    cout << "Introduceti datele (fara spatii in titlu/autor):" << endl;
    cin >> c4;
    cout << "Cartea citita: " << c4 << endl;

    return 0;
}
