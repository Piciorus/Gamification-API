#include <iostream>
#include <compare>
using namespace std;

class ContorCpp20 {
private:
    int valoare;

public:
    ContorCpp20(int v = 0) : valoare(v) {}

    int getVal() const { return valoare; }

    // ContorCpp20& operator++()    { ++valoare; return *this; }
    // ContorCpp20  operator++(int) { ContorCpp20 v = *this; ++valoare; return v; }
    // ContorCpp20& operator--()    { --valoare; return *this; }
    // ContorCpp20  operator--(int) { ContorCpp20 v = *this; --valoare; return v; }

    bool operator==(const ContorCpp20& o) const { return valoare == o.valoare; }
    auto operator<=>(const ContorCpp20& o) const { return valoare <=> o.valoare; }
};

int main()
{
    auto yn = [](bool v) { return v ? "DA" : "NU"; };

    ContorCpp20 a(3), b(5), e(3);
    cout << "=== Comparatii (a=3, b=5, e=3) ===\n";
    cout << "a == e : " << yn(a == e) << "\n";
    cout << "a != b : " << yn(a != b) << "\n";
    cout << "a <  b : " << yn(a <  b) << "\n";
    cout << "a <= e : " << yn(a <= e) << "\n";
    cout << "b >  a : " << yn(b >  a) << "\n";
    cout << "b >= a : " << yn(b >= a) << "\n";
}
