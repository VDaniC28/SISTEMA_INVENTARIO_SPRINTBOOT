package com.example.appsistema.controller;

import com.example.appsistema.model.Rol;
import com.example.appsistema.model.Usuario;
import com.example.appsistema.repository.RolRepository;
import com.example.appsistema.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;  // Asegúrate de que esté aquí


    @GetMapping("/test")
    public String testVista() {
        return "admin/vistaUsuarios";
    }

    // 👉 Mostrar la vista con todos los usuarios
    @GetMapping({"", "/"})
    public String mostrarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());

        // Cargar la lista de roles y pasarla al modelo (¡esto es lo que faltaba!)
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/vistaUsuarios"; // Tu archivo HTML se llama vistaUsuarios.html
    }

    // 👉 Guardar nuevo usuario (desde modal de registro)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        // Encripta la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setFechaRegistro(new Date(System.currentTimeMillis()));
        usuarioRepository.save(usuario);
        return "redirect:/admin/usuarios?guardado=1";
    }

    // 👉 Buscar usuario por ID (AJAX para modal editar)
    @GetMapping("/buscar/{id}")
    @ResponseBody
    public Optional<Usuario> obtenerUsuarioPorId(@PathVariable("id") Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    // 👉 Actualizar usuario (desde modal editar)
    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuarioActualizado) {
        Optional<Usuario> optional = usuarioRepository.findById(usuarioActualizado.getIdUsuario());
        if (optional.isPresent()) {
            Usuario usuarioExistente = optional.get();
            usuarioExistente.setUsername(usuarioActualizado.getUsername());
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
           // Solo actualizar la contraseña si el campo de contraseña no está vacío
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
                // Si la contraseña fue proporcionada, se encripta
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }
            usuarioExistente.setEstado(usuarioActualizado.getEstado());
            usuarioRepository.save(usuarioExistente);
        }
        return "redirect:/admin/usuarios";
    }

    // 👉 Eliminar usuario por ID
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Integer idUsuario) {
        usuarioRepository.deleteById(idUsuario);
        return "redirect:/admin/usuarios";
    }

   
    @Autowired
    private RolRepository rolRepository;

    @PostMapping("/asignar-roles")
    public String asignarRoles(
        @RequestParam Integer idUsuario,
        @RequestParam(required = false) List<Integer> roles) {

        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow();

        if (roles != null) {
            List<Rol> nuevosRoles = rolRepository.findAllById(roles);
            usuario.setRoles(new HashSet<>(nuevosRoles));
        } else {
            usuario.setRoles(new HashSet<>()); // Elimina todos los roles
        }

        usuarioRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/roles/{id}")
    @ResponseBody
    public Map<String, Object> obtenerRolesUsuario(@PathVariable("id") Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow();
        List<Rol> rolesAsignados = new ArrayList<>(usuario.getRoles());
        List<Rol> todosRoles = rolRepository.findAll();

        List<String> nombresAsignados = rolesAsignados.stream()
                .map(Rol::getNombre)
                .toList();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("rolesAsignados", nombresAsignados);
        respuesta.put("todosRoles", todosRoles);
        return respuesta;
    }
}
