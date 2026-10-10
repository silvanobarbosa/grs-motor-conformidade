package com.senior.grc.mocks;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock da Sara: extracao deterministica por padroes simples (local, data, pseudonimos) e explicacao
 * que apenas reescreve a base. Confianca fixa e modelo/prompt registrados. Nada aqui decide.
 */
@Component
public class MockSaraAdapter implements SaraPort {

    public static final String MODELO = "sara-demo-1.0";

    private static final Pattern LOCAL = Pattern.compile("(PLANTA_NORTE|PLANTA_SUL|PORTARIA_\\w+|SUBESTACAO\\w*|ALMOXARIFADO)");
    private static final Pattern DATA = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern PESSOA = Pattern.compile("(P-[A-Z0-9]{6,})");

    @Override
    public Triagem triar(String textoLivre) {
        String txt = textoLivre == null ? "" : textoLivre;
        String local = primeira(LOCAL, txt);
        String data = primeira(DATA, txt);
        List<String> pessoas = todas(PESSOA, txt);
        String prompt = "Extraia local, data e pessoas do relato: \"" + resumo(txt) + "\"";
        return new Triagem(local, data, pessoas, 0.82, MODELO, prompt);
    }

    @Override
    public String explicar(String base) {
        if (base == null || base.isBlank()) {
            return "Sem base para explicar.";
        }
        return "Em linguagem natural: " + base;
    }

    @Override
    public String modelo() {
        return MODELO;
    }

    private static String primeira(Pattern p, String txt) {
        Matcher m = p.matcher(txt);
        return m.find() ? m.group(1) : null;
    }

    private static List<String> todas(Pattern p, String txt) {
        List<String> out = new ArrayList<>();
        Matcher m = p.matcher(txt);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    private static String resumo(String txt) {
        return txt.length() <= 80 ? txt : txt.substring(0, 80) + "...";
    }
}
