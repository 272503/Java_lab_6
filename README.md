# Laboratorium 6 – Wielowątkowość i GUI w Java

## Cel projektu

Celem laboratorium było stworzenie aplikacji graficznej w języku Java z wykorzystaniem JavaFX, umożliwiającej podstawową obróbkę graficzną obrazów z wykorzystaniem przetwarzania równoległego. Zadanie miało na celu połączenie wiedzy z zakresu projektowania GUI, operacji na obrazach oraz programowania współbieżnego.

---

## Zrealizowane funkcjonalności

Projekt zaimplementował pełny zestaw funkcji zgodnych z wymaganiami zawartymi w historjach użytkownika:

### Interfejs graficzny
- Ekran startowy z nazwą aplikacji, logo PWr i tekstem powitalnym.
- Widok umożliwiający wybór operacji z listy rozwijanej.
- Przycisk „Wykonaj” z walidacją braku wyboru operacji.
- Widok oryginalnego oraz przetworzonego obrazu.
- Widoczna stopka z danymi autora.

### Obsługa plików graficznych
- Wczytywanie tylko plików `.jpg` przez systemowy selektor plików.
- Komunikaty o sukcesie, błędach formatu i niepowodzeniach wczytania.
- Usuwanie poprzednich obrazów i ich kopii przy ponownym wczytaniu.

### Zapis przetworzonego obrazu
- Okno modalne z możliwością nadania nazwy plikowi.
- Walidacja długości nazwy (3–100 znaków).
- Obsługa kolizji nazw (plik istnieje).
- Zapis do folderu systemowego „Obrazy” w formacie `.jpg`.

### Operacje na obrazach (Zadanie 2)
- **Skalowanie:** okno modalne z wpisywaniem szerokości/wysokości.
- **Obrót:** dwa przyciski (90° w lewo/prawo).
- **Negatyw:** opcja w liście, wykonanie na kopii obrazu.
- **Progowanie:** okno modalne z wartością progu (0–255).
- **Konturowanie:** automatyczna operacja na kopii obrazu.

### Wielowątkowość i logowanie (Zadanie 3)
- Operacje: negatyw, progowanie i konturowanie zrealizowane z użyciem do 4 wątków.
- Logowanie do pliku `applog.txt` z czasem, poziomem i opisem akcji:
  - Start i zamknięcie aplikacji.
  - Błędy i działania użytkownika.

---

## Uruchamianie aplikacji

1. Wymagania:
   - Java 17+
   - Maven 3.6+
2. Sposób uruchomienia:
   ```bash
   ./mvnw javafx:run
   ```
lub w IDE (np. IntelliJ IDEA) uruchom HelloApplication.java jako aplikację JavaFX.

---

## Struktura projektu
```
Java_fx1/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/example/java_fx1/
│       │       ├── HelloApplication.java
│       │       └── HelloController.java
│       └── resources/
│           └── org/example/java_fx1/
│               ├── hello-view.fxml
│               └── logo/logo.png
├── applog.txt
├── pom.xml
├── result_*.png
```

---

## Technologie
- Java 17
- JavaFX 19
- FXML – definicja layoutu GUI
- Maven – zarządzanie projektem i zależnościami
- Multithreading (ExecutorService) – optymalizacja wydajności
- Logowanie (Logger) – rejestrowanie zdarzeń w pliku tekstowym

---

## Podsumowanie i wnioski
- Projekt zrealizowany zgodnie z wymaganiami zadania laboratoryjnego. Aplikacja spełnia wszystkie kryteria akceptacji z historii użytkownika:
- Posiada przejrzysty interfejs,
- Obsługuje poprawnie wczytywanie i zapis obrazów,
- Implementuje 4 operacje przetwarzania graficznego,
- Zoptymalizowano czas ich działania poprzez zastosowanie wielowątkowości,
- Aplikacja jest odporna na błędy i posiada system logowania.
- Rozwiązanie pokazało znaczenie planowania struktury aplikacji, projektowania UX oraz bezpiecznego przetwarzania danych wejściowych. Laboratorium stanowiło wartościowy przykład zastosowania wiedzy z różnych obszarów Javy w praktyce.

Autor: Piotr Kosior
