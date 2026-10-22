#!/usr/bin/env python3
"""
Statische Code-Analyse für Java-Quelltexte.
Prüft Coding-Conventions, Komplexität, Stil und häufige Probleme.
Keine externen Abhängigkeiten — nur Python-Standardbibliothek.
"""

import argparse
import json
import os
import re
import sys
from dataclasses import dataclass, field, asdict
from pathlib import Path
from typing import Optional


# ── Konfiguration (Schwellwerte) ──────────────────────────────────────────────

DEFAULTS = {
    "max_file_lines": 300,
    "max_method_lines": 25,
    "max_class_lines": 250,
    "max_params": 4,
    "max_nesting_depth": 3,
    "max_cyclomatic_complexity": 10,
    "max_line_length": 120,
}


# ── Datenmodell ───────────────────────────────────────────────────────────────

@dataclass
class Finding:
    rule: str
    severity: str          # ERROR, WARNING, INFO
    file: str
    line: int
    message: str
    context: str = ""

@dataclass
class FileReport:
    path: str
    lines: int = 0
    classes: int = 0
    methods: int = 0
    findings: list = field(default_factory=list)

@dataclass
class SCAReport:
    files_analyzed: int = 0
    total_findings: int = 0
    errors: int = 0
    warnings: int = 0
    infos: int = 0
    file_reports: list = field(default_factory=list)


# ── Hilfsfunktionen ──────────────────────────────────────────────────────────

def strip_comments_and_strings(source: str) -> str:
    result = []
    i = 0
    in_line_comment = False
    in_block_comment = False
    in_string = False
    string_char = None

    while i < len(source):
        c = source[i]

        if in_line_comment:
            if c == '\n':
                in_line_comment = False
                result.append(c)
            else:
                result.append(' ')
            i += 1
            continue

        if in_block_comment:
            if c == '*' and i + 1 < len(source) and source[i + 1] == '/':
                in_block_comment = False
                result.append('  ')
                i += 2
            else:
                result.append(' ' if c != '\n' else '\n')
                i += 1
            continue

        if in_string:
            if c == '\\':
                result.append('  ')
                i += 2
                continue
            if c == string_char:
                in_string = False
                result.append(' ')
            else:
                result.append(' ' if c != '\n' else '\n')
            i += 1
            continue

        if c == '/' and i + 1 < len(source):
            if source[i + 1] == '/':
                in_line_comment = True
                result.append(' ')
                i += 1
                continue
            if source[i + 1] == '*':
                in_block_comment = True
                result.append('  ')
                i += 2
                continue

        if c in ('"', "'"):
            in_string = True
            string_char = c
            result.append(' ')
            i += 1
            continue

        result.append(c)
        i += 1

    return ''.join(result)


def find_java_files(path: str) -> list[str]:
    p = Path(path)
    if p.is_file() and p.suffix == '.java':
        return [str(p)]
    if p.is_dir():
        files = sorted(p.rglob('*.java'))
        return [str(f) for f in files if '/test/' not in str(f) and '/build/' not in str(f) and '/target/' not in str(f)]
    return []


# ── Einzelne Prüfregeln ──────────────────────────────────────────────────────

def check_file_length(lines: list[str], filepath: str, cfg: dict) -> list[Finding]:
    if len(lines) > cfg["max_file_lines"]:
        return [Finding(
            rule="FILE_TOO_LONG",
            severity="WARNING",
            file=filepath,
            line=len(lines),
            message=f"Datei hat {len(lines)} Zeilen (max {cfg['max_file_lines']})",
        )]
    return []


def check_line_length(lines: list[str], filepath: str, cfg: dict) -> list[Finding]:
    findings = []
    for i, line in enumerate(lines, 1):
        if len(line.rstrip()) > cfg["max_line_length"]:
            findings.append(Finding(
                rule="LINE_TOO_LONG",
                severity="WARNING",
                file=filepath,
                line=i,
                message=f"Zeile hat {len(line.rstrip())} Zeichen (max {cfg['max_line_length']})",
                context=line.rstrip()[:120] + "…" if len(line.rstrip()) > 120 else line.rstrip(),
            ))
    return findings


def check_wildcard_imports(lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        if re.match(r'^import\s+.*\.\*\s*;', stripped):
            findings.append(Finding(
                rule="WILDCARD_IMPORT",
                severity="WARNING",
                file=filepath,
                line=i,
                message=f"Wildcard-Import vermeiden: {stripped}",
                context=stripped,
            ))
    return findings


def check_system_out(lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    for i, line in enumerate(lines, 1):
        if re.search(r'System\.(out|err)\.(print|println|printf)', line):
            findings.append(Finding(
                rule="SYSTEM_OUT",
                severity="WARNING",
                file=filepath,
                line=i,
                message="System.out/err statt Logger verwenden",
                context=line.strip(),
            ))
    return findings


def check_empty_catch(source: str, lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    cleaned = strip_comments_and_strings(source)
    cleaned_lines = cleaned.split('\n')
    for i, line in enumerate(cleaned_lines):
        if re.search(r'catch\s*\(', line):
            brace_count = 0
            found_open = False
            body_empty = True
            for j in range(i, min(i + 10, len(cleaned_lines))):
                for ch in cleaned_lines[j]:
                    if ch == '{':
                        brace_count += 1
                        found_open = True
                    elif ch == '}':
                        brace_count -= 1
                if found_open and brace_count == 0:
                    break
                if found_open and brace_count > 0 and cleaned_lines[j].strip() and j > i:
                    content = cleaned_lines[j].strip().replace('{', '').replace('}', '').strip()
                    if content:
                        body_empty = False
            if body_empty and found_open:
                findings.append(Finding(
                    rule="EMPTY_CATCH",
                    severity="ERROR",
                    file=filepath,
                    line=i + 1,
                    message="Leerer catch-Block — Exceptions nicht stillschweigend schlucken",
                    context=lines[i].strip() if i < len(lines) else "",
                ))
    return findings


def check_magic_numbers(lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    allowed = {'0', '1', '-1', '2', '0.0', '1.0', '0L', '1L', '100'}
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        if stripped.startswith('//') or stripped.startswith('*') or stripped.startswith('/*'):
            continue
        if re.search(r'(import|package)\s', stripped):
            continue
        for m in re.finditer(r'(?<![.\w])(-?\d+\.?\d*[LlFfDd]?)(?![.\w])', stripped):
            num = m.group(1)
            if num in allowed:
                continue
            if re.search(r'(static\s+final|final\s+static)', stripped):
                continue
            if re.search(r'(LOG|log|logger|Logger)', stripped):
                continue
            findings.append(Finding(
                rule="MAGIC_NUMBER",
                severity="INFO",
                file=filepath,
                line=i,
                message=f"Magic Number {num} — als Konstante extrahieren",
                context=stripped,
            ))
    return findings


def check_todo_fixme(lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    for i, line in enumerate(lines, 1):
        if re.search(r'\b(TODO|FIXME|HACK|XXX)\b', line, re.IGNORECASE):
            findings.append(Finding(
                rule="TODO_FIXME",
                severity="INFO",
                file=filepath,
                line=i,
                message="Offenes TODO/FIXME gefunden",
                context=line.strip(),
            ))
    return findings


def check_naming_conventions(lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    for i, line in enumerate(lines, 1):
        stripped = line.strip()

        # Klassennamen: PascalCase
        m = re.match(r'(?:public\s+|private\s+|protected\s+)?(?:abstract\s+|final\s+)?(?:class|interface|enum)\s+([A-Za-z_]\w*)', stripped)
        if m:
            name = m.group(1)
            if not re.match(r'^[A-Z][a-zA-Z0-9]*$', name):
                findings.append(Finding(
                    rule="NAMING_CLASS",
                    severity="WARNING",
                    file=filepath,
                    line=i,
                    message=f"Klassenname '{name}' sollte PascalCase sein",
                    context=stripped,
                ))

        # Konstanten: UPPER_SNAKE_CASE
        m = re.match(r'\s*(?:public|private|protected)?\s*static\s+final\s+\S+\s+([A-Za-z_]\w*)\s*=', stripped)
        if m:
            name = m.group(1)
            if not re.match(r'^[A-Z][A-Z0-9_]*$', name) and not re.match(r'^log(ger)?$', name, re.IGNORECASE):
                findings.append(Finding(
                    rule="NAMING_CONSTANT",
                    severity="INFO",
                    file=filepath,
                    line=i,
                    message=f"Konstante '{name}' sollte UPPER_SNAKE_CASE sein",
                    context=stripped,
                ))
    return findings


def extract_methods(source: str, lines: list[str], filepath: str) -> list[dict]:
    cleaned = strip_comments_and_strings(source)
    cleaned_lines = cleaned.split('\n')
    methods = []

    method_pattern = re.compile(
        r'(?:(?:public|private|protected|static|final|abstract|synchronized|native)\s+)*'
        r'(?:<[^>]+>\s+)?'
        r'(\w[\w<>\[\],\s]*?)\s+'
        r'(\w+)\s*\('
    )

    for i, line in enumerate(cleaned_lines):
        m = method_pattern.search(line)
        if not m:
            continue
        name = m.group(2)
        if name in ('if', 'for', 'while', 'switch', 'catch', 'return', 'new', 'class', 'interface', 'enum'):
            continue

        brace_start = None
        brace_count = 0
        for j in range(i, min(i + 5, len(cleaned_lines))):
            for ci, ch in enumerate(cleaned_lines[j]):
                if ch == '{':
                    if brace_start is None:
                        brace_start = j
                    brace_count += 1
                elif ch == '}':
                    brace_count -= 1
                    if brace_count == 0 and brace_start is not None:
                        methods.append({
                            "name": name,
                            "start": brace_start + 1,
                            "end": j + 1,
                            "length": j - brace_start + 1,
                        })
                        break
            else:
                continue
            break

    return methods


def check_method_length(methods: list[dict], filepath: str, cfg: dict) -> list[Finding]:
    findings = []
    for method in methods:
        if method["length"] > cfg["max_method_lines"]:
            findings.append(Finding(
                rule="METHOD_TOO_LONG",
                severity="WARNING",
                file=filepath,
                line=method["start"],
                message=f"Methode '{method['name']}' hat {method['length']} Zeilen (max {cfg['max_method_lines']})",
            ))
    return findings


def check_method_params(lines: list[str], filepath: str, cfg: dict) -> list[Finding]:
    findings = []
    method_sig = re.compile(
        r'(?:public|private|protected|static|final|abstract|synchronized)\s+.*?\s+(\w+)\s*\(([^)]*)\)'
    )
    for i, line in enumerate(lines, 1):
        m = method_sig.search(line)
        if m:
            name = m.group(1)
            params_str = m.group(2).strip()
            if not params_str:
                continue
            params = [p.strip() for p in params_str.split(',') if p.strip()]
            if len(params) > cfg["max_params"]:
                findings.append(Finding(
                    rule="TOO_MANY_PARAMS",
                    severity="WARNING",
                    file=filepath,
                    line=i,
                    message=f"Methode '{name}' hat {len(params)} Parameter (max {cfg['max_params']})",
                    context=line.strip(),
                ))
    return findings


def check_nesting_depth(source: str, lines: list[str], filepath: str, cfg: dict) -> list[Finding]:
    findings = []
    cleaned = strip_comments_and_strings(source)
    depth = 0
    class_depth = 0
    reported_lines = set()

    for i, line in enumerate(cleaned.split('\n'), 1):
        for ch in line:
            if ch == '{':
                depth += 1
            elif ch == '}':
                depth -= 1
        effective_depth = depth - 2  # class + method
        if effective_depth > cfg["max_nesting_depth"] and i not in reported_lines:
            reported_lines.add(i)
            findings.append(Finding(
                rule="DEEP_NESTING",
                severity="WARNING",
                file=filepath,
                line=i,
                message=f"Verschachtelungstiefe {effective_depth} (max {cfg['max_nesting_depth']})",
                context=lines[i - 1].strip() if i <= len(lines) else "",
            ))
    return findings


def estimate_cyclomatic_complexity(source: str, methods: list[dict], filepath: str, cfg: dict) -> list[Finding]:
    findings = []
    cleaned = strip_comments_and_strings(source)
    cleaned_lines = cleaned.split('\n')

    complexity_keywords = re.compile(r'\b(if|else\s+if|for|while|do|switch|case|catch|\?\s*[^:])\b|&&|\|\|')

    for method in methods:
        cc = 1
        for line_idx in range(method["start"] - 1, min(method["end"], len(cleaned_lines))):
            cc += len(complexity_keywords.findall(cleaned_lines[line_idx]))
        if cc > cfg["max_cyclomatic_complexity"]:
            findings.append(Finding(
                rule="HIGH_COMPLEXITY",
                severity="WARNING",
                file=filepath,
                line=method["start"],
                message=f"Methode '{method['name']}' hat Zyklomatische Komplexität {cc} (max {cfg['max_cyclomatic_complexity']})",
            ))
    return findings


def check_missing_javadoc(lines: list[str], filepath: str) -> list[Finding]:
    findings = []
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        if re.match(r'public\s+(?!class|interface|enum|static\s+void\s+main)', stripped):
            has_javadoc = False
            for j in range(i - 2, max(i - 8, -1), -1):
                prev = lines[j].strip() if j >= 0 else ""
                if prev == '*/':
                    has_javadoc = True
                    break
                if prev.startswith('/**'):
                    has_javadoc = True
                    break
                if prev == '' or prev.startswith('@') or prev.startswith('*'):
                    continue
                break
            if not has_javadoc:
                findings.append(Finding(
                    rule="MISSING_JAVADOC",
                    severity="INFO",
                    file=filepath,
                    line=i,
                    message="Öffentliche Methode/Feld ohne Javadoc",
                    context=stripped[:100],
                ))
    return findings


# ── Hauptanalyse ──────────────────────────────────────────────────────────────

def analyze_file(filepath: str, cfg: dict) -> FileReport:
    report = FileReport(path=filepath)
    try:
        with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
            source = f.read()
    except (IOError, OSError) as e:
        report.findings.append(Finding(
            rule="FILE_READ_ERROR", severity="ERROR", file=filepath, line=0, message=str(e)
        ))
        return report

    lines = source.split('\n')
    report.lines = len(lines)

    report.findings.extend(check_file_length(lines, filepath, cfg))
    report.findings.extend(check_line_length(lines, filepath, cfg))
    report.findings.extend(check_wildcard_imports(lines, filepath))
    report.findings.extend(check_system_out(lines, filepath))
    report.findings.extend(check_empty_catch(source, lines, filepath))
    report.findings.extend(check_magic_numbers(lines, filepath))
    report.findings.extend(check_todo_fixme(lines, filepath))
    report.findings.extend(check_naming_conventions(lines, filepath))
    report.findings.extend(check_method_params(lines, filepath, cfg))
    report.findings.extend(check_nesting_depth(source, lines, filepath, cfg))
    report.findings.extend(check_missing_javadoc(lines, filepath))

    methods = extract_methods(source, lines, filepath)
    report.methods = len(methods)
    report.findings.extend(check_method_length(methods, filepath, cfg))
    report.findings.extend(estimate_cyclomatic_complexity(source, methods, filepath, cfg))

    return report


def analyze(paths: list[str], cfg: dict) -> SCAReport:
    sca = SCAReport()
    java_files = []
    for p in paths:
        java_files.extend(find_java_files(p))

    for filepath in java_files:
        fr = analyze_file(filepath, cfg)
        sca.file_reports.append(fr)
        sca.files_analyzed += 1

    for fr in sca.file_reports:
        for f in fr.findings:
            sca.total_findings += 1
            if f.severity == "ERROR":
                sca.errors += 1
            elif f.severity == "WARNING":
                sca.warnings += 1
            else:
                sca.infos += 1

    return sca


# ── Ausgabeformate ────────────────────────────────────────────────────────────

def format_markdown(report: SCAReport) -> str:
    parts = []
    parts.append("# SCA-Report — Statische Code-Analyse\n")
    parts.append(f"**Dateien analysiert:** {report.files_analyzed}")
    parts.append(f"**Findings gesamt:** {report.total_findings} ({report.errors} Fehler, {report.warnings} Warnungen, {report.infos} Hinweise)\n")

    if report.total_findings == 0:
        parts.append("Keine Findings — sauber! ✓\n")
        return '\n'.join(parts)

    for fr in report.file_reports:
        if not fr.findings:
            continue
        parts.append(f"\n## {fr.path}")
        parts.append(f"Zeilen: {fr.lines} | Methoden: {fr.methods}\n")
        parts.append("| Zeile | Regel | Schwere | Beschreibung |")
        parts.append("|-------|-------|---------|-------------|")
        for f in sorted(fr.findings, key=lambda x: x.line):
            parts.append(f"| {f.line} | `{f.rule}` | {f.severity} | {f.message} |")

    parts.append("\n---")
    parts.append(f"*{report.total_findings} Findings in {report.files_analyzed} Dateien*")
    return '\n'.join(parts)


def format_json(report: SCAReport) -> str:
    data = {
        "files_analyzed": report.files_analyzed,
        "total_findings": report.total_findings,
        "errors": report.errors,
        "warnings": report.warnings,
        "infos": report.infos,
        "files": []
    }
    for fr in report.file_reports:
        file_data = {
            "path": fr.path,
            "lines": fr.lines,
            "methods": fr.methods,
            "findings": [asdict(f) for f in fr.findings]
        }
        data["files"].append(file_data)
    return json.dumps(data, indent=2, ensure_ascii=False)


# ── CLI ───────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Statische Code-Analyse für Java")
    parser.add_argument("paths", nargs="+", help="Java-Dateien oder Verzeichnisse")
    parser.add_argument("--format", choices=["markdown", "json"], default="markdown")
    parser.add_argument("--max-file-lines", type=int, default=DEFAULTS["max_file_lines"])
    parser.add_argument("--max-method-lines", type=int, default=DEFAULTS["max_method_lines"])
    parser.add_argument("--max-class-lines", type=int, default=DEFAULTS["max_class_lines"])
    parser.add_argument("--max-params", type=int, default=DEFAULTS["max_params"])
    parser.add_argument("--max-nesting-depth", type=int, default=DEFAULTS["max_nesting_depth"])
    parser.add_argument("--max-complexity", type=int, default=DEFAULTS["max_cyclomatic_complexity"])
    parser.add_argument("--max-line-length", type=int, default=DEFAULTS["max_line_length"])
    args = parser.parse_args()

    cfg = {
        "max_file_lines": args.max_file_lines,
        "max_method_lines": args.max_method_lines,
        "max_class_lines": args.max_class_lines,
        "max_params": args.max_params,
        "max_nesting_depth": args.max_nesting_depth,
        "max_cyclomatic_complexity": args.max_complexity,
        "max_line_length": args.max_line_length,
    }

    report = analyze(args.paths, cfg)

    if args.format == "json":
        print(format_json(report))
    else:
        print(format_markdown(report))

    sys.exit(1 if report.errors > 0 else 0)


if __name__ == "__main__":
    main()
