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
