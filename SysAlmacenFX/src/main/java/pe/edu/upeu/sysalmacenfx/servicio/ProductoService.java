package pe.edu.upeu.sysalmacenfx.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysalmacenfx.dto.ComboBoxOption;
import pe.edu.upeu.sysalmacenfx.modelo.Producto;
import pe.edu.upeu.sysalmacenfx.repositorio.ProductoRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository repo;

    public Producto save(Producto producto) {
        return repo.save(producto);
    }

    public List<Producto> list() {
        return repo.findAll();
    }

    public Producto update(Producto producto) {
        return repo.save(producto);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Producto searchById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (Producto producto : repo.findAll()) {
            ComboBoxOption cb = new ComboBoxOption();
            cb.setKey(String.valueOf(producto.getIdProducto()));
            cb.setValue(producto.getNombre() + " - " + producto.getPu());
            listar.add(cb);
        }
        return listar;
    }
}
