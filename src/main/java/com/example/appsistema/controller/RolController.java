package com.example.appsistema.controller;

import com.example.appsistema.model.Rol;
import com.example.appsistema.repository.RolRepository;
import com.example.appsistema.service.RolService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/admin")
public class RolController {
    
    private static final Logger logger = LoggerFactory.getLogger(RolController.class);

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private RolService rolService;

    // Ya no necesitas este método en el controlador de Roles, ya que la vista
    // completa se carga desde el controlador de Usuarios (donde están ambas tablas).
    // Las listas de roles se pasan al modelo desde el controlador de usuarios.
    // Si quieres mantenerlo separado, puedes hacerlo, pero la llamada desde el
    // navegador para ver la página ahora es desde el controlador de usuarios.
    /* @GetMapping("/roles")
    public String listarRoles(Model model) {
        try {
            model.addAttribute("roles", rolRepository.findAll());
            model.addAttribute("nuevoRol", new Rol());
            return "admin/vistaAdmin";
        } catch (Exception e) {
            logger.error("Error al cargar roles: " + e.getMessage(), e);
            model.addAttribute("error", "Ocurrió un error al cargar los roles.");
            return "errorPage";
        }
    } */

    // Guardar nuevo rol y redirigir a la vista de usuarios.
    // La vista unificada debe tener la acción del formulario apuntando a este endpoint.
    @PostMapping("/roles/guardar")
    public String guardarRol(@ModelAttribute Rol nuevoRol) {
        try {
            rolRepository.save(nuevoRol);
            return "redirect:/admin/usuarios?rolesGuardado=true";
        } catch (Exception e) {
            logger.error("Error al guardar rol: " + e.getMessage(), e);
            // Puedes redirigir a una página de error o a la misma vista con un mensaje de error
            return "redirect:/admin/usuarios?errorRol=true";
        }
    }

    // Endpoint para eliminar un rol y redirigir a la vista de usuarios.
    @PostMapping("/roles/eliminar/{id}")
    public String eliminarRol(@PathVariable Integer id) {
        try {
            rolService.eliminarRol(id);
            return "redirect:/admin/usuarios?rolesEliminado=true";
        } catch (Exception e) {
            logger.error("Error al eliminar rol con ID {}: {}", id, e.getMessage(), e);
            return "redirect:/admin/usuarios?errorEliminarRol=true";
        }
    }
}