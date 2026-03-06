auto calculeazaMedia(const Student tablou[], int n) {
    double suma = 0.0;
    for (int i = 0; i < n; i++)
        suma += tablou[i].getNota();
    return suma / n;
    // auto deduce: double / int => double
    // Compilatorul vede tipul expresiei "suma / n" si il foloseste
}
