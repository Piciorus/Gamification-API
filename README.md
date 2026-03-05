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
