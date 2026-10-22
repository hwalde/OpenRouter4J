# Java Coding Guidelines

## 1. Datei- und Klassenstruktur

- **Eine öffentliche Klasse pro Datei.** Der Dateiname entspricht dem Klassennamen.
- **Maximale Dateilänge: 300 Zeilen.** Wird eine Datei länger, ist das ein Hinweis, dass die Klasse zu viele Verantwortlichkeiten trägt — aufteilen.
- **Maximale Klassenlänge: 250 Zeilen** (ohne Imports und Javadoc-Header).
- **Import-Reihenfolge:** java.* → javax.* → Drittanbieter → Projekt-eigene. Keine Wildcard-Imports (`import java.util.*`).

## 2. Methoden

- **Maximale Methodenlänge: 25 Zeilen.** Eine Methode, die länger ist, macht zu viel auf einmal.
- **Maximale Parameterzahl: 4.** Mehr Parameter → Parameter-Objekt oder Builder einführen.
- **Sprechende Namen:** Der Methodenname beschreibt, *was* sie tut, nicht *wie*. `calculateTotalPrice()` statt `calc()`.
- **Single Responsibility:** Jede Methode hat genau eine Aufgabe. Wenn du "und" brauchst um zu erklären was sie tut, ist sie zu groß.

## 3. Naming Conventions

- **Klassen/Interfaces/Enums:** `PascalCase` — `OrderService`, `PaymentGateway`
- **Methoden/Variablen:** `camelCase` — `calculateTotal()`, `orderCount`
- **Konstanten:** `UPPER_SNAKE_CASE` — `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`
- **Packages:** nur Kleinbuchstaben — `com.example.orderservice`
- **Keine Abkürzungen** außer allgemein bekannte (URL, HTTP, ID). `customerNumber` statt `custNr`.

## 4. Fehlerbehandlung

- **Niemals leere catch-Blöcke.** Mindestens loggen oder bewusst mit Kommentar ignorieren.
- **Spezifische Exceptions fangen** — kein `catch (Exception e)` wenn `IOException` gemeint ist.
- **Keine Exceptions für Kontrollfluss.** Exceptions sind für unerwartete Fehler, nicht für erwartbare Zustände.
- **Checked Exceptions nur an API-Grenzen.** Intern bevorzugt unchecked Exceptions verwenden.

## 5. Komplexität und Verschachtelung

- **Maximale Verschachtelungstiefe: 3 Ebenen** (innerhalb einer Methode). Tiefer verschachtelt → Guard Clauses oder Hilfsmethode extrahieren.
- **Zyklomatische Komplexität ≤ 10** pro Methode. Höher → die Methode hat zu viele Verzweigungen.
- **Early Return statt tiefer Verschachtelung:**
  ```java
  // Schlecht
  public void process(Order order) {
      if (order != null) {
          if (order.isValid()) {
              // ... 20 Zeilen Logik
          }
      }
  }

  // Gut
  public void process(Order order) {
      if (order == null) return;
      if (!order.isValid()) return;
      // ... 20 Zeilen Logik
  }
  ```

## 6. Magic Numbers und Konstanten

- **Keine Magic Numbers im Code.** Jede Zahl (außer 0, 1, -1) als benannte Konstante extrahieren.
  ```java
  // Schlecht
  if (retryCount > 3) { ... }

  // Gut
  private static final int MAX_RETRIES = 3;
  if (retryCount > MAX_RETRIES) { ... }
  ```

## 7. Logging statt System.out

- **Kein `System.out.println` in Produktivcode.** Immer ein Logging-Framework verwenden (SLF4J, Log4j2).
- **Log-Level korrekt wählen:** ERROR für Fehler, WARN für Probleme die behandelt werden, INFO für wichtige Geschäftsereignisse, DEBUG für Entwickler-Details.

## 8. Zeilenlänge

- **Maximale Zeilenlänge: 120 Zeichen.** Längere Zeilen umbrechen — bevorzugt nach Komma oder vor Operator.

## 9. Dokumentation

- **Öffentliche Methoden mit Javadoc dokumentieren** — beschreibt *was* die Methode macht und *warum*, nicht *wie*.
- **Keine redundanten Kommentare:** `// Setzt den Namen` über `setName()` bringt nichts.
- **Kommentare nur für das Warum:** Wenn der Code nicht selbsterklärend ist, erst versuchen den Code klarer zu schreiben. Dann kommentieren.

## 10. Allgemeine Prinzipien

- **DRY (Don't Repeat Yourself):** Duplizierter Code → extrahieren.
- **KISS (Keep It Simple):** Die einfachste Lösung ist meist die beste.
- **YAGNI (You Ain't Gonna Need It):** Nichts bauen, was nicht jetzt gebraucht wird.
- **Favor Composition over Inheritance:** Vererbungshierarchien flach halten. Interfaces und Komposition bevorzugen.
