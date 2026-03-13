package com.adenium.zanatenunchi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameParser {

    private static final String LETTER_CLASS = "\\p{L}";

    private static final Pattern[] NAME_PATTERNS = {
        // "soy Oscar", "Soy oscar"
        Pattern.compile("(?iu)\\bsoy\\s+(" + LETTER_CLASS + "+)"),
        // "me llamo Oscar"
        Pattern.compile("(?iu)\\bme\\s+llamo\\s+(" + LETTER_CLASS + "+)"),
        // "mi nombre es Oscar"
        Pattern.compile("(?iu)\\bmi\\s+nombre\\s+es\\s+(" + LETTER_CLASS + "+)"),
        // "pueden llamarme Oscar" / "puedes llamarme Oscar"
        Pattern.compile("(?iu)\\bpuede[ns]?\\s+llamarme\\s+(" + LETTER_CLASS + "+)"),
        // "llámame Oscar"
        Pattern.compile("(?iu)\\bll[aá]mame\\s+(" + LETTER_CLASS + "+)"),
        // "dime Oscar" / "me dicen Oscar"
        Pattern.compile("(?iu)\\b(?:dime|me\\s+dicen)\\s+(" + LETTER_CLASS + "+)"),
        // "I'm Oscar" / "I am Oscar" (inglés)
        Pattern.compile("(?iu)\\bI['’]?m\\s+(" + LETTER_CLASS + "+)"),
        Pattern.compile("(?iu)\\bI\\s+am\\s+(" + LETTER_CLASS + "+)"),
        // "my name is Oscar"
        Pattern.compile("(?iu)\\bmy\\s+name\\s+is\\s+(" + LETTER_CLASS + "+)"),
        // "call me Oscar"
        Pattern.compile("(?iu)\\bcall\\s+me\\s+(" + LETTER_CLASS + "+)"),
    };

    // Palabras que NO son nombres (filtro)
    private static final String[] NOT_NAMES = {
        "hola", "hey", "ey", "buenas", "que", "el", "la", "un", "una",
        "hello", "hi", "the", "and", "yes", "no", "ok", "okay",
        "jugador", "player", "nuevo", "new", "aqui", "here"
    };

    /**
     * Extrae el nombre de un mensaje.
     * Si no encuentra un patrón conocido, devuelve el mensaje limpio.
     */
    public static String extractName(String message) {
        if (message == null || message.isBlank()) {
            return "Jugador";
        }

        String cleaned = message.trim();

        // Intentar extraer con patrones
        for (Pattern pattern : NAME_PATTERNS) {
            Matcher matcher = pattern.matcher(cleaned);
            if (matcher.find()) {
                String name = matcher.group(1).trim();
                if (isValidName(name)) {
                    return capitalize(name);
                }
            }
        }

        // Si el mensaje es corto (1-2 palabras), probablemente es solo el nombre
        String[] words = cleaned.split("\\s+");
        if (words.length <= 2) {
            // Tomar la última palabra que parezca un nombre
            for (int i = words.length - 1; i >= 0; i--) {
                String word = words[i].replaceAll("[^\\p{L}]", "");
                if (isValidName(word)) {
                    return capitalize(word);
                }
            }
        }

        // Fallback: tomar la primera palabra que parezca nombre
        for (String word : words) {
            String clean = word.replaceAll("[^\\p{L}]", "");
            if (isValidName(clean) && clean.length() >= 2) {
                return capitalize(clean);
            }
        }

        // Último recurso: devolver el mensaje original limpio (máximo 20 chars)
        String fallback = cleaned.replaceAll("[^\\p{L}0-9\\s]", "").trim();
        if (fallback.length() > 20) {
            fallback = fallback.substring(0, 20);
        }
        return fallback.isEmpty() ? "Jugador" : capitalize(fallback.split("\\s+")[0]);
    }

    private static boolean isValidName(String word) {
        if (word == null || word.length() < 2 || word.length() > 20) {
            return false;
        }

        String lower = word.toLowerCase();
        for (String notName : NOT_NAMES) {
            if (lower.equals(notName)) {
                return false;
            }
        }

        // Debe empezar con letra
        return Character.isLetter(word.charAt(0));
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}


