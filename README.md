```

#include <iostream>
#include <compare>

class Student {
private:
    char* nume;
    int varsta;
    double nota;

    // Utilitare interne
    static int strLen(const char* s) {
        int i = 0;
        while (s && s[i]) i++;
        return i;
    }

    static void strCpy(char* dst, const char* src) {
        int i = 0;
        while ((dst[i] = src[i]) != '\0') i++;
    }

public:
    // Constructor implicit
    Student(const char* n = "Necunoscut", int v = 18, double notaInit = 0.0)
        : varsta(v), nota(notaInit) {
        int len = strLen(n);
        nume = new char[len + 1];
        strCpy(nume, n);
    }

    // Constructor de copiere
    Student(const Student& other)
        : varsta(other.varsta), nota(other.nota) {
        int len = strLen(other.nume);
        nume = new char[len + 1];
        strCpy(nume, other.nume);
    }

    // Destructor
    ~Student() { delete[] nume; }

    // Operator=
    Student& operator=(const Student& other) {
        if (this != &other) {
            delete[] nume;
            int len = strLen(other.nume);
            nume = new char[len + 1];
            strCpy(nume, other.nume);
            varsta = other.varsta;
            nota = other.nota;
        }
        return *this;
    }

    // ++ / -- prefix/postfix
    Student& operator++() { ++nota; return *this; }
    Student operator++(int) { Student temp = *this; ++nota; return temp; }
    Student& operator--() { --nota; return *this; }
    Student operator--(int) { Student temp = *this; --nota; return temp; }

    // <=> si ==
    auto operator<=>(const Student& other) const { return nota <=> other.nota; }
    bool operator==(const Student& other) const { return nota == other.nota; }

    // Getter nota
    double getNota() const { return nota; }

    // Declarare friend
    friend std::ostream& operator<<(std::ostream& out, const Student& s);
    friend std::istream& operator>>(std::istream& in, Student& s);
    friend Student operator+(const Student& s, double bonus);
    friend Student operator+(double bonus, const Student& s);
};

// Definire operatorii friend în afara clasei
std::ostream& operator<<(std::ostream& out, const Student& s) {
    out << s.nume << " (" << s.varsta << " ani) - nota: " << s.nota;
    return out;
}

std::istream& operator>>(std::istream& in, Student& s) {
    char buffer[100];
    std::cout << "Nume: ";
    in >> buffer;
    delete[] s.nume;
    s.nume = new char[Student::strLen(buffer) + 1];
    Student::strCpy(s.nume, buffer);
    std::cout << "Varsta: ";
    in >> s.varsta;
    std::cout << "Nota: ";
    in >> s.nota;
    return in;
}

Student operator+(const Student& s, double bonus) {
    return Student(s.nume, s.varsta, s.nota + bonus);
}

Student operator+(double bonus, const Student& s) {
    return s + bonus;
}

auto calculeazaMedia(const Student studenti[], int n) {
    double suma = 0;
    for (int i = 0; i < n; i++)
        suma += studenti[i].getNota();
    return suma / n; // compilator deduce double
}

auto swapStudenti(Student& a, Student& b) {
    Student temp = a;
    a = b;
    b = temp;
}

int main() {
    Student studenti[5] = {
        Student("Ana", 19, 8.5),
        Student("Ion", 20, 9.0),
        Student("Maria", 18, 7.0),
        Student("George", 21, 10.0),
        Student("Elena", 19, 4.5)
    };

    // Bubble sort dupa nota
    for (int i = 0; i < 5 - 1; i++) {
        for (int j = 0; j < 5 - i - 1; j++) {
            if (studenti[j] > studenti[j + 1]) { // operator <=> gestioneaza comparatia
                swapStudenti(studenti[j], studenti[j + 1]);
            }
        }
    }

    std::cout << "Studentii sortati dupa nota:\n";
    for (const auto& s : studenti)
        std::cout << s << "\n";

    std::cout << "\nStudenti cu nota >= 5:\n";
    for (const auto& s : studenti)
        if (s.getNota() >= 5)
            std::cout << s << "\n";

    auto media = calculeazaMedia(studenti, 5);
    std::cout << "\nMedia notelor: " << media << "\n";

    // Exemplu folosire auto cu operator<=>
    auto rezultat = studenti[0] <=> studenti[1];
    if (rezultat > 0)
        std::cout << studenti[0] << " are nota mai mare decat " << studenti[1] << "\n";
    else if (rezultat < 0)
        std::cout << studenti[0] << " are nota mai mica decat " << studenti[1] << "\n";
    else
        std::cout << studenti[0] << " are nota egala cu " << studenti[1] << "\n";

    return 0;
}


```
