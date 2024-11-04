package pe.edu.upeu.sysalmacenfx.control;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.upeu.sysalmacenfx.componente.ColumnInfo;
import pe.edu.upeu.sysalmacenfx.componente.ComboBoxAutoComplete;
import pe.edu.upeu.sysalmacenfx.componente.TableViewHelper;
import pe.edu.upeu.sysalmacenfx.componente.Toast;
import pe.edu.upeu.sysalmacenfx.dto.ComboBoxOption;
import pe.edu.upeu.sysalmacenfx.modelo.Producto;
import pe.edu.upeu.sysalmacenfx.servicio.CategoriaService;
import pe.edu.upeu.sysalmacenfx.servicio.MarcaService;
import pe.edu.upeu.sysalmacenfx.servicio.ProductoService;
import pe.edu.upeu.sysalmacenfx.servicio.UnidadMedidaService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class ProductoController {

    @FXML
    TextField txtNombreProducto, txtPUnit, txtPUnitOld, txtUtilidad, txtStock, txtStockOld, txtFiltroDato;
    @FXML
    ComboBox<ComboBoxOption> cbxMarca, cbxCategoria, cbxUnidMedida, cbxProducto; // ComboBox para productos
    @FXML
    private TableView<Producto> tableView;
    @FXML
    Label lbnMsg;
    @FXML
    private AnchorPane miContenedor;
    Stage stage;

    @Autowired
    MarcaService ms;
    @Autowired
    CategoriaService cs;
    @Autowired
    ProductoService ps;
    @Autowired
    UnidadMedidaService ums;

    private Validator validator;
    ObservableList<Producto> listarProducto;
    Producto formulario;
    Long idProductoCE = 0L;

    public void initialize() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
            stage = (Stage) miContenedor.getScene().getWindow();
        }));
        timeline.setCycleCount(1);
        timeline.play();

        // Inicialización de ComboBoxes
        cbxMarca.setTooltip(new Tooltip());
        cbxMarca.getItems().addAll(ms.listarCombobox());
        cbxCategoria.setTooltip(new Tooltip());
        cbxCategoria.getItems().addAll(cs.listarCombobox());
        cbxUnidMedida.setTooltip(new Tooltip());
        cbxUnidMedida.getItems().addAll(ums.listarCombobox());

        // Inicialización del ComboBox de productos
        cbxProducto.setTooltip(new Tooltip());
        cbxProducto.getItems().addAll(ps.listarCombobox());
        cbxProducto.setOnAction(event -> {
            ComboBoxOption selectedProduct = cbxProducto.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                Long idProducto = Long.parseLong(selectedProduct.getKey());
                Producto productoSeleccionado = ps.searchById(idProducto);
                if (productoSeleccionado != null) {
                    txtPUnit.setText(String.valueOf(productoSeleccionado.getPu()));
                    txtUtilidad.setText(String.valueOf(productoSeleccionado.getUtilidad()));
                    txtStock.setText(String.valueOf(productoSeleccionado.getStock()));
                    // Deshabilitar campos si es necesario
                    txtPUnit.setDisable(true);
                    txtUtilidad.setDisable(true);
                    txtStock.setDisable(true);
                }
            }
        });

        new ComboBoxAutoComplete<>(cbxMarca);
        new ComboBoxAutoComplete<>(cbxCategoria);
        new ComboBoxAutoComplete<>(cbxUnidMedida);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Crear instancia de la clase genérica TableViewHelper
        TableViewHelper<Producto> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Pro.", new ColumnInfo("idProducto", 60.0));
        columns.put("Nombre Producto", new ColumnInfo("nombre", 200.0));
        columns.put("P. Unitario", new ColumnInfo("pu", 150.0));
        columns.put("Utilidad", new ColumnInfo("utilidad", 100.0));
        columns.put("Marca", new ColumnInfo("marca.nombre", 200.0));
        columns.put("Categoria", new ColumnInfo("categoria.nombre", 200.0));
        columns.put("Unid. Medida", new ColumnInfo("unidadMedida.nombreMedida", 150.0));

        Consumer<Producto> updateAction = (Producto producto) -> {
            editForm(producto);
        };
        Consumer<Producto> deleteAction = (Producto producto) -> {
            ps.delete(producto.getIdProducto());
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
        listar();
    }

    public void listar() {
        tableView.getItems().clear();
        listarProducto = FXCollections.observableArrayList(ps.list());
        tableView.getItems().addAll(listarProducto);
        txtFiltroDato.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarProductos(newValue);
        });
    }

    public void clearForm() {
        txtNombreProducto.setText("");
        txtPUnit.setText("");
        txtPUnitOld.setText("");
        txtUtilidad.setText("");
        txtStock.setText("");
        txtStockOld.setText("");
        cbxMarca.getSelectionModel().select(null);
        cbxCategoria.getSelectionModel().select(null);
        cbxUnidMedida.getSelectionModel().select(null);
        cbxProducto.getSelectionModel().select(null); // Limpiar el ComboBox de productos
        idProductoCE = 0L;
    }

    public void validarFormulario() {
        formulario = new Producto();
        formulario.setNombre(txtNombreProducto.getText());
        formulario.setPu(Double.parseDouble(txtPUnit.getText().isEmpty() ? "0" : txtPUnit.getText()));
        formulario.setPuOld(Double.parseDouble(txtPUnitOld.getText().isEmpty() ? "0" : txtPUnitOld.getText()));
        formulario.setUtilidad(Double.parseDouble(txtUtilidad.getText().isEmpty() ? "0" : txtUtilidad.getText()));
        formulario.setStock(Double.parseDouble(txtStock.getText().isEmpty() ? "0" : txtStock.getText()));
        formulario.setStockOld(Double.parseDouble(txtStockOld.getText().isEmpty() ? "0" : txtStockOld.getText()));

        String idxM = cbxMarca.getSelectionModel().getSelectedItem() == null ? "0" : cbxMarca.getSelectionModel().getSelectedItem().getKey();
        formulario.setMarca(ms.searchById(Long.parseLong(idxM)));
        String idxC = cbxCategoria.getSelectionModel().getSelectedItem() == null ? "0" : cbxCategoria.getSelectionModel().getSelectedItem().getKey();
        formulario.setCategoria(cs.searchById(Long.parseLong(idxC)));
        String idxUM = cbxUnidMedida.getSelectionModel().getSelectedItem() == null ? "0" : cbxUnidMedida.getSelectionModel().getSelectedItem().getKey();
        formulario.setUnidadMedida(ums.searchById(Long.parseLong(idxUM)));

        Set<ConstraintViolation<Producto>> violaciones = validator.validate(formulario);
        if (violaciones.isEmpty()) {
            if (idProductoCE != 0L) {
                formulario.setIdProducto(idProductoCE);
                ps.update(formulario);
                clearForm();
            } else {
                ps.save(formulario);
                clearForm();
            }
            listar();
        } else {
            // Manejo de errores
        }
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableView.getItems().clear();
            tableView.getItems().addAll(listarProducto);
        } else {
            String lowerCaseFilter = filtro.toLowerCase();
            List<Producto> productosFiltrados = listarProducto.stream()
                    .filter(producto -> producto.getNombre().toLowerCase().contains(lowerCaseFilter))
                    .collect(Collectors.toList());
            tableView.getItems().clear();
            tableView.getItems().addAll(productosFiltrados);
        }
    }

    public void editForm(Producto producto) {
        txtNombreProducto.setText(producto.getNombre());
        txtPUnit.setText(producto.getPu().toString());
        txtPUnitOld.setText(producto.getPuOld().toString());
        txtUtilidad.setText(producto.getUtilidad().toString());
        txtStock.setText(producto.getStock().toString());
        txtStockOld.setText(producto.getStockOld().toString());

        cbxMarca.getSelectionModel().select(cbxMarca.getItems().stream()
                .filter(marca -> Long.parseLong(marca.getKey()) == producto.getMarca().getIdMarca())
                .findFirst().orElse(null));
        cbxCategoria.getSelectionModel().select(cbxCategoria.getItems().stream()
                .filter(categoria -> Long.parseLong(categoria.getKey()) == producto.getCategoria().getIdCategoria())
                .findFirst().orElse(null));
        cbxUnidMedida.getSelectionModel().select(cbxUnidMedida.getItems().stream()
                .filter(unidadMedida -> Long.parseLong(unidadMedida.getKey()) == producto.getUnidadMedida().getIdUnidad())
                .findFirst().orElse(null));

        idProductoCE = producto.getIdProducto();
    }

}
