package com.example.appsistema.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.example.appsistema.model.Categoria;
import com.example.appsistema.model.Color;
import com.example.appsistema.model.EstadoProducto;
import com.example.appsistema.model.Genero;
import com.example.appsistema.model.Marca;
import com.example.appsistema.model.MaterialPrincipal;
import com.example.appsistema.model.MaterialSuela;
import com.example.appsistema.model.Talla;
import com.example.appsistema.model.TipoItem;
import com.example.appsistema.model.TipoPersona;
import com.example.appsistema.service.CategoriaService;
import com.example.appsistema.service.ColorService;
import com.example.appsistema.service.EstadoProductoService;
import com.example.appsistema.service.GeneroService;
import com.example.appsistema.service.MarcaService;
import com.example.appsistema.service.MaterialPrincipalService;
import com.example.appsistema.service.MaterialSuelaService;
import com.example.appsistema.service.TallaService;
import com.example.appsistema.service.TipoItemService;
import com.example.appsistema.service.TipoPersonaService;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/admin/catalogo")
public class CatalogoController {
    @Autowired
    private MarcaService marcaService;
    
    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private MaterialPrincipalService materialPrincipalService;
    
    @Autowired
    private ColorService colorService;
    
    @Autowired
    private TipoItemService tipoItemService;
    
    @Autowired
    private TallaService tallaService;
    
    @Autowired
    private GeneroService generoService;
    
    @Autowired
    private TipoPersonaService tipoPersonaService;
    
    @Autowired
    private MaterialSuelaService materialSuelaService;
    
    @Autowired
    private EstadoProductoService estadoProductoService;

    @GetMapping("")
    public String vistaCatalogo(Model model) {
        // Paginación inicial (página 0, 5 elementos por página)
        Pageable pageable = PageRequest.of(0, 5);
        
        // Cargar todos los módulos con paginación
        model.addAttribute("marcas", marcaService.obtenerTodas(pageable));
        model.addAttribute("categorias", categoriaService.obtenerTodas(pageable));
        model.addAttribute("materialesPrincipales", materialPrincipalService.obtenerTodos(pageable));
        model.addAttribute("colores", colorService.obtenerTodos(pageable));
        model.addAttribute("tiposItem", tipoItemService.obtenerTodos(pageable));
        model.addAttribute("tallas", tallaService.obtenerTodas(pageable));
        model.addAttribute("generos", generoService.obtenerTodos(pageable));
        model.addAttribute("tiposPersona", tipoPersonaService.obtenerTodos(pageable));
        model.addAttribute("materialesSuela", materialSuelaService.obtenerTodos(pageable));
        model.addAttribute("estadosProducto", estadoProductoService.obtenerTodos(pageable));
        
        return "admin/vistaCatalogo";
    }

    // =================== MARCAS ===================
    @GetMapping("/marcas")
    @ResponseBody
    public Page<Marca> getMarcas(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return marcaService.obtenerTodas(pageable);
    }

    @PostMapping("/marcas")
    @ResponseBody
    public Marca saveMarca(@RequestBody Marca marca) {
        return marcaService.guardar(marca);
    }

    @GetMapping("/marcas/{id}")
    @ResponseBody
    public Marca getMarcaById(@PathVariable Integer id) {
        return marcaService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
    }

    @PutMapping("/marcas/{id}")
    @ResponseBody
    public Marca updateMarca(@PathVariable Integer id, @RequestBody Marca marca) {
        // Aseguramos que el ID del path se use para la actualización
        marca.setIdMarca(id);
        return marcaService.guardar(marca);
    }

    @DeleteMapping("/marcas/{id}")
    @ResponseBody
    public void deleteMarca(@PathVariable Integer id) {
        marcaService.eliminar(id);
    }

    // =================== CATEGORÍAS ===================
    @GetMapping("/categorias")
    @ResponseBody
    public Object getCategorias(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return categoriaService.obtenerTodas(pageable);
    }

    // NUEVO MÉTODO: Para obtener una sola categoría por su ID
    @GetMapping("/categorias/{id}")
    @ResponseBody
    public Categoria getCategoriaById(@PathVariable Integer id) {
        // Asumo que 'obtenerPorId' existe y devuelve una Categoria
        return categoriaService.obtenerPorId(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    @PostMapping("/categorias")
    @ResponseBody
    public Categoria saveCategoria(@RequestBody Categoria categoria) {
        return categoriaService.guardar(categoria);
    }

    @PutMapping("/categorias/{id}")
    @ResponseBody
    public Categoria updateCategoria(@PathVariable Integer id, @RequestBody Categoria categoria) {
        categoria.setIdCategoria(id);
        return categoriaService.guardar(categoria);
    }

    @DeleteMapping("/categorias/{id}")
    @ResponseBody
    public void deleteCategoria(@PathVariable Integer id) {
        categoriaService.eliminar(id);
    }

    // =================== MATERIALES PRINCIPALES ===================
    @GetMapping("/materialesPrincipales")
    @ResponseBody
    public Page<MaterialPrincipal> getMaterialesPrincipales(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return materialPrincipalService.obtenerTodos(pageable);
    }

    @PostMapping("/materialesPrincipales")
    @ResponseBody
    public MaterialPrincipal saveMaterialPrincipal(@RequestBody MaterialPrincipal materialPrincipal) {
        return materialPrincipalService.guardar(materialPrincipal);
    }

    @GetMapping("/materialesPrincipales/{id}")
    @ResponseBody
    public MaterialPrincipal getMaterialPrincipalById(@PathVariable Integer id) {
        return materialPrincipalService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "material Principal no encontrada"));
    }

    @PutMapping("/materialesPrincipales/{id}")
    @ResponseBody
    public MaterialPrincipal updateMaterialPrincipal(@PathVariable Integer id, @RequestBody MaterialPrincipal materialPrincipal) {
        // Es una buena práctica asegurar que el ID del path se use para la actualización
        materialPrincipal.setIdMaterialPrincipal(id);
        return materialPrincipalService.guardar(materialPrincipal);
    }

    @DeleteMapping("/materialesPrincipales/{id}")
    @ResponseBody
    public void deleteMaterialPrincipal(@PathVariable Integer id) {
        materialPrincipalService.eliminar(id);
    }

    // =================== COLORES ===================
    @GetMapping("/colores")
    @ResponseBody
    public Page<Color> getColores(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return colorService.obtenerTodos(pageable);
    }

    @PostMapping("/colores")
    @ResponseBody
    public Color saveColor(@RequestBody Color color) {
        return colorService.guardar(color);
    }

    @GetMapping("/colores/{id}")
    @ResponseBody
    public Color getcolorById(@PathVariable Integer id) {
        return colorService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Color no encontrada"));
    }

    @PutMapping("/colores/{id}")
    @ResponseBody
    public Color updateColor(@PathVariable Integer id, @RequestBody Color color) {
        // Set the ID from the path to the color object
        color.setIdColor(id);
        return colorService.guardar(color);
    }

    @DeleteMapping("/colores/{id}")
    @ResponseBody
    public void deleteColor(@PathVariable Integer id) {
        colorService.eliminar(id);
    }

    // =================== TIPOS DE ITEM ===================
    @GetMapping("/tiposItem")
    @ResponseBody
    public Page<TipoItem> getTiposItem(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return tipoItemService.obtenerTodos(pageable);
    }

    @PostMapping("/tiposItem")
    @ResponseBody
    public TipoItem saveTipoItem(@RequestBody TipoItem tipoItem) {
        return tipoItemService.guardar(tipoItem);
    }

    @GetMapping("/tiposItem/{id}")
    @ResponseBody
    public TipoItem getTipoItemById(@PathVariable Integer id) {
        return tipoItemService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tipo Item no encontrada"));
    }

    @PutMapping("/tiposItem/{id}")
    @ResponseBody
    public TipoItem updateTipoItem(@PathVariable Integer id, @RequestBody TipoItem tipoItem) {
        tipoItem.setIdTipoItem(id);
        return tipoItemService.guardar(tipoItem);
    }

    @DeleteMapping("/tiposItem/{id}")
    @ResponseBody
    public void deleteTipoItem(@PathVariable Integer id) {
        tipoItemService.eliminar(id);
    }

    // =================== TALLAS ===================
    @GetMapping("/tallas")
    @ResponseBody
    public Page<Talla> getTallas(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return tallaService.obtenerTodas(pageable);
    }

    @PostMapping("/tallas")
    @ResponseBody
    public Talla saveTalla(@RequestBody Talla talla) {
        return tallaService.guardar(talla);
    }

    @GetMapping("/tallas/{id}")
    @ResponseBody
    public Talla getTallaById(@PathVariable Integer id) {
        return tallaService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Talla no encontrada"));
    }

    @PutMapping("/tallas/{id}")
    @ResponseBody
    public Talla updateTalla(@PathVariable Integer id, @RequestBody Talla talla) {
        talla.setIdTalla(id);
        return tallaService.guardar(talla);
    }

    @DeleteMapping("/tallas/{id}")
    @ResponseBody
    public void deleteTalla(@PathVariable Integer id) {
        tallaService.eliminar(id);
    }

    // =================== GÉNEROS ===================
    @GetMapping("/generos")
    @ResponseBody
    public Page<Genero> getGeneros(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return generoService.obtenerTodos(pageable);
    }

    @PostMapping("/generos")
    @ResponseBody
    public Genero saveGenero(@RequestBody Genero genero) {
        return generoService.guardar(genero);
    }

    @GetMapping("/generos/{id}")
    @ResponseBody
    public Genero getGeneroById(@PathVariable Integer id) {
        return generoService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genero no encontrada"));
    }

    @PutMapping("/generos/{id}")
    @ResponseBody
    public Genero updateGenero(@PathVariable Integer id, @RequestBody Genero genero) {
        genero.setIdGenero(id);
        return generoService.guardar(genero);
    }

    @DeleteMapping("/generos/{id}")
    @ResponseBody
    public void deleteGenero(@PathVariable Integer id) {
        generoService.eliminar(id);
    }

    // =================== TIPOS DE PERSONA ===================
    @GetMapping("/tiposPersona")
    @ResponseBody
    public Page<TipoPersona> getTiposPersona(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return tipoPersonaService.obtenerTodos(pageable);
    }

    @PostMapping("/tiposPersona")
    @ResponseBody
    public TipoPersona saveTipoPersona(@RequestBody TipoPersona tipoPersona) {
        return tipoPersonaService.guardar(tipoPersona);
    }

    @GetMapping("/tiposPersona/{id}")
    @ResponseBody
    public TipoPersona getTipoPersonaById(@PathVariable Integer id) {
        return tipoPersonaService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo Persona no encontrada"));
    }

    @PutMapping("/tiposPersona/{id}")
    @ResponseBody
    public TipoPersona updateTipoPersona(@PathVariable Integer id, @RequestBody TipoPersona tipoPersona) {
        tipoPersona.setIdTipoPersona(id);
        return tipoPersonaService.guardar(tipoPersona);
    }

    @DeleteMapping("/tiposPersona/{id}")
    @ResponseBody
    public void deleteTipoPersona(@PathVariable Integer id) {
        tipoPersonaService.eliminar(id);
    }

    // =================== MATERIALES DE SUELA ===================
    @GetMapping("/materialesSuela")
    @ResponseBody
    public Page<MaterialSuela> getMaterialesSuela(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return materialSuelaService.obtenerTodos(pageable);
    }

    @PostMapping("/materialesSuela")
    @ResponseBody
    public MaterialSuela saveMaterialSuela(@RequestBody MaterialSuela materialSuela) {
        return materialSuelaService.guardar(materialSuela);
    }

    @GetMapping("/materialesSuela/{id}")
    @ResponseBody
    public MaterialSuela getMaterialSuelaById(@PathVariable Integer id) {
        return materialSuelaService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material Suela no encontrada"));
    }

    @PutMapping("/materialesSuela/{id}")
    @ResponseBody
    public MaterialSuela updateMaterialSuela(@PathVariable Integer id, @RequestBody MaterialSuela materialSuela) {
        materialSuela.setIdMaterialSuela(id);
        return materialSuelaService.guardar(materialSuela);
    }

    @DeleteMapping("/materialesSuela/{id}")
    @ResponseBody
    public void deleteMaterialSuela(@PathVariable Integer id) {
        materialSuelaService.eliminar(id);
    }

    // =================== ESTADOS DE PRODUCTO ===================
    @GetMapping("/estadosProducto")
    @ResponseBody
    public Page<EstadoProducto> getEstadosProducto(@RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return estadoProductoService.obtenerTodos(pageable);
    }

    @PostMapping("/estadosProducto")
    @ResponseBody
    public EstadoProducto saveEstadoProducto(@RequestBody EstadoProducto estadoProducto) {
        return estadoProductoService.guardar(estadoProducto);
    }

    @GetMapping("/estadosProducto/{id}")
    @ResponseBody
    public EstadoProducto getEstadoProductoById(@PathVariable Integer id) {
        return estadoProductoService.obtenerPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado Producto no encontrada"));
    }

    @PutMapping("/estadosProducto/{id}")
    @ResponseBody
    public EstadoProducto updateEstadoProducto(@PathVariable Integer id, @RequestBody EstadoProducto estadoProducto) {
        estadoProducto.setIdEstadoProducto(id);
        return estadoProductoService.guardar(estadoProducto);
    }

    @DeleteMapping("/estadosProducto/{id}")
    @ResponseBody
    public void deleteEstadoProducto(@PathVariable Integer id) {
        estadoProductoService.eliminar(id);
    }
}
