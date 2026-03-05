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
