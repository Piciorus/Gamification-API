
    // --------------------------------------------------------
    // PUNCTUL 1 — const auto& intr-un tablou
    //
    // const auto& s : studenti
    //   auto deduce tipul elementului din tablou => Student
    //   const => nu modificam studentul (e doar citit)
    //   &     => nu copiem (referinta) — eficient pentru obiecte mari
    //
    // Fara &:   auto s  => copiaza fiecare Student la fiecare iteratie
    // Fara const: auto& s => am putea modifica accidental studentul
    // Cu const&: auto& s => citim, fara copiere, fara modificare
    // --------------------------------------------------------
    cout << "=== 1. const auto& — studenti cu nota >= 5 ===\n";
    for (const auto& s : studenti) {
        if (s.getNota() >= 5.0)
            cout << "  " << s << "\n";
    }

    // Comparatie vizuala — ce face fiecare varianta:
    cout << "\n  -- Ce deduce auto in fiecare caz --\n";
    cout << "  for (auto s : studenti)        => Student  (copie la fiecare pas)\n";
    cout << "  for (auto& s : studenti)       => Student& (referinta, poate modifica)\n";
    cout << "  for (const auto& s : studenti) => const Student& (referinta, read-only)\n";

    // --------------------------------------------------------
    // PUNCTUL 2 — functie cu tip de intoarcere auto
    //
    // Compilatorul analizeaza expresia "return suma / n"
    //   suma = double, n = int
    //   double / int => double (int promovat la double)
    //   => auto = double
    //
    // Putem verifica cu decltype:
    //   decltype(calculeazaMedia(studenti, 6)) este double
    // --------------------------------------------------------
    cout << "\n=== 2. Functie cu tip de intoarcere auto ===\n";

    auto media = calculeazaMedia(studenti, 6);
    //   ^
    //   auto deduce double (tipul returnat de functie)

    cout << "  Media notelor: " << media << "\n";
    cout << "  Tipul dedus de auto: double\n";
    cout << "  De ce double? suma(double) / n(int) => int promovat la double\n";

    // Demonstrare: auto se comporta ca double
    auto rezultat = media * 2.0;    // double * double = double
    cout << "  media * 2.0 = " << rezultat << "  (auto deduce tot double)\n";

    // --------------------------------------------------------
    // PUNCTUL 3 — auto pentru rezultatul <=>  (C++20)
    //
    // operator<=> returneaza un tip de ordering, nu int sau bool.
    // Pentru double => partial_ordering (din cauza NaN)
    // Pentru int    => strong_ordering
    //
    // auto e IDEAL aici — evitam sa scriem tipul lung:
    //   partial_ordering cmp = s1 <=> s2;  // lung si greu de retinut
    //   auto cmp = s1 <=> s2;              // simplu, compilatorul stie tipul
    //
    // Compararea rezultatului:
    //   cmp < 0   => primul e mai mic
    //   cmp > 0   => primul e mai mare
    //   cmp == 0  => egali
    // --------------------------------------------------------
    cout << "\n=== 3. auto cu rezultatul <=> ===\n";

    Student s1("Alice", 20, 8.0);
    Student s2("Bob",   21, 9.0);
    Student s3("Carol", 19, 8.0);

    // Caz 1: s1 < s2
    auto cmp1 = s1 <=> s2;
    //   ^
    //   auto deduce: partial_ordering (tipul returnat de operator<=>)
    //   Fara auto am scrie: partial_ordering cmp1 = s1 <=> s2;
    cout << "  s1(8.0) <=> s2(9.0) => ";
    if (cmp1 < 0)       cout << "s1 are nota mai mica\n";
    else if (cmp1 > 0)  cout << "s1 are nota mai mare\n";
    else                cout << "note egale\n";

    // Caz 2: s1 == s3 (note egale)
    auto cmp2 = s1 <=> s3;
    cout << "  s1(8.0) <=> s3(8.0) => ";
    if (cmp2 < 0)       cout << "s1 are nota mai mica\n";
    else if (cmp2 > 0)  cout << "s1 are nota mai mare\n";
    else                cout << "note egale\n";

    // Caz 3: s2 > s1
    auto cmp3 = s2 <=> s1;
    cout << "  s2(9.0) <=> s1(8.0) => ";
    if (cmp3 < 0)       cout << "s2 are nota mai mica\n";
    else if (cmp3 > 0)  cout << "s2 are nota mai mare\n";
    else                cout << "note egale\n";

    cout << "\n  Tipul dedus de auto: partial_ordering\n";
    cout << "  De ce partial si nu strong?\n";
    cout << "  => double are NaN (Not a Number): NaN <=> NaN nu e nici <, >, sau ==\n";
    cout << "  => partial_ordering permite valori incomparabile\n";
    cout << "  => int ar folosi strong_ordering (toti intregii sunt comparabili)\n";
