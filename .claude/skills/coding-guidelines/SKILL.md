---
name: coding-guidelines
description: Prüft Java-Quelltext auf Einhaltung der Coding Guidelines. Nutze diesen Skill sobald es darum geht, Quelltext qualitativ zu verbessern, Code zu reviewen, nach Coding-Verstößen zu suchen, oder wenn der Benutzer explizit nach Coding Guidelines, Code-Qualität, Clean Code, Code Review oder statischer Analyse fragt. Auch aktivieren bei Aussagen wie "prüf mal den Code", "stimmt die Qualität", "ist der Code sauber", "Code verbessern", "Coding Standards prüfen".
---

# Coding Guidelines — Java Code-Qualitätsprüfung

Dieser Skill prüft Java-Quelltext in zwei parallelen Schritten und fasst die Ergebnisse zusammen.

## Schritt 1: Pfad bestimmen

Frage den Benutzer welchen Code er prüfen lassen möchte, falls nicht bereits klar:
- Einzelne Java-Datei
- Ein Package/Verzeichnis
- Das gesamte Projekt (src/main/java)

Ermittle den konkreten Pfad. Falls unklar, nutze `find` um die Java-Quelltexte zu lokalisieren.

## Schritt 2: Zwei parallele Prüfungen starten

Starte **gleichzeitig** (im selben Turn, nicht nacheinander!) diese beiden Prüfungen:

### 2a) Statische Code-Analyse (SCA) — via Bash

Führe das eingebettete SCA-Script aus:

```bash
python3 <skill-dir>/scripts/sca.py <pfad-zum-code> --format markdown
```

Dabei ist `<skill-dir>` das Verzeichnis in dem diese SKILL.md liegt.

Das Script prüft automatisch:
- Datei- und Methodenlänge
- Zeilenlänge
- Wildcard-Imports
- System.out/err statt Logger
- Leere catch-Blöcke
- Magic Numbers
- TODO/FIXME-Kommentare
- Naming Conventions (PascalCase, camelCase, UPPER_SNAKE_CASE)
- Parameterzahl pro Methode
- Verschachtelungstiefe
- Zyklomatische Komplexität
- Fehlende Javadoc bei öffentlichen Methoden

Die Schwellwerte können per CLI-Parameter angepasst werden:
```bash
python3 <skill-dir>/scripts/sca.py <pfad> --max-method-lines 30 --max-complexity 15
```

### 2b) Guideline-Review — via Subagent (Sonnet)

Starte **parallel** zur SCA einen Subagent mit dem Sonnet-Modell:

```
Agent({
  description: "Guideline-Review",
  model: "sonnet",
  prompt: "Lies die Datei <skill-dir>/GUIDELINES.md — das sind die Coding Guidelines.
           Dann lies den Quelltext in <pfad-zum-code>.
           Prüfe den Code Datei für Datei gegen jede einzelne Guideline.
           Erstelle einen Report im folgenden Format:

           # Guideline-Review

           ## <Dateiname>

           ### Eingehaltene Guidelines
           - [Guideline-Abschnitt]: Kurze Bestätigung

           ### Verstöße
           - [Guideline-Abschnitt] Zeile X: Beschreibung des Verstoßes
             Empfehlung: Konkreter Vorschlag zur Behebung

           ## Zusammenfassung
           - Geprüfte Dateien: N
           - Eingehaltene Guidelines: N
           - Verstöße: N (X kritisch, Y Verbesserungsvorschläge)

           Wichtig: Sei konkret. Nenne Zeilennummern. Gib bei jedem Verstoß eine
           umsetzbare Empfehlung. Unterscheide zwischen kritischen Verstößen und
           Verbesserungsvorschlägen."
})
```

## Schritt 3: Ergebnisse zusammenführen

Wenn beide Prüfungen abgeschlossen sind, erstelle einen kombinierten Report:

### Aufbau des Gesamtreports

```
# Code-Qualitätsreport

## Übersicht
- Geprüfte Dateien: N
- SCA-Findings: X (Fehler: A, Warnungen: B, Hinweise: C)
- Guideline-Verstöße: Y

## Kritische Findings (sofort beheben)
[Findings mit Severity ERROR oder kritische Guideline-Verstöße]

## Warnungen (sollten behoben werden)
[Warnings aus SCA + Guideline-Verstöße]

## Hinweise (optional, aber empfohlen)
[Infos aus SCA + Verbesserungsvorschläge]

## Positiv aufgefallen
[Was der Code gut macht — das gehört auch dazu]
```

### Deduplizierung

SCA und Guideline-Review finden teilweise die gleichen Probleme (z.B. beide melden eine zu lange Methode). Dedupliziere: zeige jedes Problem nur einmal, aber vermerke wenn beide Prüfungen es gefunden haben — das unterstreicht die Relevanz.

## Schritt 4: Benutzer fragen

Frage den Benutzer: "Soll ich die Findings direkt im Code beheben?"

Falls ja: Arbeite die Findings nach Priorität ab (Fehler → Warnungen → Hinweise).
