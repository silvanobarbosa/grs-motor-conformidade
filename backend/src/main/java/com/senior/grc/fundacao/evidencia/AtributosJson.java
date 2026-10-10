package com.senior.grc.fundacao.evidencia;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Serializacao canonica dos atributos da evidencia: JSON plano {@code {"chave":"valor",...}} com
 * chaves ORDENADAS, para o hash da trilha ser estavel e o read-model poder reler os atributos.
 * Formato simples e proprio (atributos sao sempre string:string, sem aninhamento) — por isso o
 * parser e seguro para o que este modulo escreve.
 */
public final class AtributosJson {

    private AtributosJson() {
    }

    public static String escrever(Map<String, String> atributos) {
        StringBuilder sb = new StringBuilder("{");
        boolean primeiro = true;
        for (var e : new TreeMap<>(atributos == null ? Map.of() : atributos).entrySet()) {
            if (!primeiro) {
                sb.append(",");
            }
            sb.append(aspas(e.getKey())).append(":").append(aspas(e.getValue()));
            primeiro = false;
        }
        return sb.append("}").toString();
    }

    public static Map<String, String> ler(String json) {
        Map<String, String> out = new LinkedHashMap<>();
        if (json == null) {
            return out;
        }
        String s = json.trim();
        if (s.length() < 2 || s.charAt(0) != '{') {
            return out;
        }
        int i = 1;
        int n = s.length();
        while (i < n) {
            while (i < n && (s.charAt(i) == ',' || s.charAt(i) == ' ')) {
                i++;
            }
            if (i >= n || s.charAt(i) == '}') {
                break;
            }
            int[] k = lerString(s, i);
            String chave = desescapar(s.substring(i + 1, k[0]));
            i = k[1];
            while (i < n && (s.charAt(i) == ':' || s.charAt(i) == ' ')) {
                i++;
            }
            int[] v = lerString(s, i);
            String valor = desescapar(s.substring(i + 1, v[0]));
            i = v[1];
            out.put(chave, valor);
        }
        return out;
    }

    /** Devolve [indice da aspa de fechamento, indice apos a aspa de fechamento]. */
    private static int[] lerString(String s, int inicio) {
        int i = inicio + 1; // pula a aspa de abertura
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == '"') {
                return new int[]{i, i + 1};
            }
            i++;
        }
        throw new IllegalArgumentException("JSON de atributos malformado: " + s);
    }

    private static String aspas(String v) {
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String desescapar(String v) {
        return v.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
