package com.senior.grc.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** API de administracao: login do admin master, gestao de usuarios e observabilidade do sistema. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String H = "X-Admin-Token";

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        return admin.autenticar(body.get("login"), body.get("senha"))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "credenciais invalidas")));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(H) String token) {
        return admin.sessao(token)
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(Map.of("login", s.login(), "nome", s.nome(), "papeis", s.papeis())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/usuarios")
    public List<Map<String, Object>> usuarios(@RequestHeader(H) String token) {
        return admin.listarUsuarios(token).stream().map(u -> Map.<String, Object>of(
                "id", u.getId(), "login", u.getLogin(), "nome", u.getNome(),
                "papeis", u.papeisLista(), "ativo", u.isAtivo(), "criadoEm", u.getCriadoEm())).toList();
    }

    @PostMapping("/usuarios")
    public Map<String, Object> criar(@RequestHeader(H) String token, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> papeis = (List<String>) body.get("papeis");
        UsuarioSistema u = admin.criarUsuario(token, (String) body.get("login"), (String) body.get("nome"),
                (String) body.get("senha"), papeis);
        return Map.of("id", u.getId(), "login", u.getLogin());
    }

    @PostMapping("/usuarios/{id}/ativo")
    public Map<String, Object> ativo(@RequestHeader(H) String token, @PathVariable UUID id,
                                     @RequestParam boolean ativo) {
        admin.definirAtivo(token, id, ativo);
        return Map.of("id", id, "ativo", ativo);
    }

    @GetMapping("/observabilidade")
    public Map<String, Object> observabilidade(@RequestHeader(H) String token) {
        return admin.observabilidade(token);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> acesso(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
