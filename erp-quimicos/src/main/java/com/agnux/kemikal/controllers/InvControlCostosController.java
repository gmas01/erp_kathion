/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.agnux.kemikal.controllers;

import com.agnux.cfd.v2.Base64Coder;
import com.agnux.common.helpers.StringHelper;
import com.agnux.common.obj.DataPost;
import com.agnux.common.obj.ResourceProject;
import com.agnux.common.obj.UserSessionData;
import com.agnux.kemikal.interfacedaos.GralInterfaceDao;
import com.agnux.kemikal.interfacedaos.HomeInterfaceDao;
import com.agnux.kemikal.interfacedaos.InvInterfaceDao;
import com.agnux.kemikal.reportes.PdfOrdenSalida;
import com.itextpdf.text.DocumentException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * @author Noe Martinez
 * gpmarsan@gmail.com
 * 22/enero/2013
 * 
 */
@Controller
@SessionAttributes({"user"})
@RequestMapping("/invcontrolcostos/")
public class InvControlCostosController {
    private static final Logger log  = Logger.getLogger(InvControlCostosController.class.getName());
    ResourceProject resource = new ResourceProject();
    
    @Autowired
    @Qualifier("daoInv")
    private InvInterfaceDao invDao;
    
    public InvInterfaceDao getInvDao() {
        return invDao;
    }
    
    @Autowired
    @Qualifier("daoHome")
    private HomeInterfaceDao HomeDao;
    
    public HomeInterfaceDao getHomeDao() {
        return HomeDao;
    }
    
    @Autowired
    @Qualifier("daoGral")
    private GralInterfaceDao gralDao;
    
    public GralInterfaceDao getGralDao() {
        return gralDao;
    }
    
    @RequestMapping(value="/startup.agnux")
    public ModelAndView startUp(HttpServletRequest request, HttpServletResponse response, 
            @ModelAttribute("user") UserSessionData user)
            throws ServletException, IOException {
        
        log.log(Level.INFO, "Ejecutando starUp de {0}", InvControlCostosController.class.getName());
        LinkedHashMap<String,String> infoConstruccionTabla = new LinkedHashMap<String,String>();
        
        //infoConstruccionTabla.put("id", "Acciones:90");
        infoConstruccionTabla.put("id", "Acciones:70");
        infoConstruccionTabla.put("folio", "Folio:90");
        infoConstruccionTabla.put("fecha","Fecha:90");
        infoConstruccionTabla.put("familia", "Orden Compra:150");
        infoConstruccionTabla.put("marca", "Orden Compra:150");
        infoConstruccionTabla.put("tipo_prod", "Tipo de Producto:150");
        infoConstruccionTabla.put("tipo", "Tipo de Costo:120");
        
        ModelAndView x = new ModelAndView("invcontrolcostos/startup", "title", "Control de Costos");
        
        x = x.addObject("layoutheader", resource.getLayoutheader());
        x = x.addObject("layoutmenu", resource.getLayoutmenu());
        x = x.addObject("layoutfooter", resource.getLayoutfooter());
        x = x.addObject("grid", resource.generaGrid(infoConstruccionTabla));
        x = x.addObject("url", resource.getUrl(request));
        x = x.addObject("username", user.getUserName());
        x = x.addObject("empresa", user.getRazonSocialEmpresa());
        x = x.addObject("sucursal", user.getSucursal());
        
        String userId = String.valueOf(user.getUserId());
        
        String codificado = Base64Coder.encodeString(userId);
        
        //id de usuario codificado
        x = x.addObject("iu", codificado);
        
        return x;
    }
    
    
    
    //Metodo para el grid y el Paginado
    @RequestMapping(value="/getAllCostos.json", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, Object>>> getAllCostosJson(
           @RequestParam(value="orderby", required=true) String orderby,
           @RequestParam(value="desc", required=true) String desc,
           @RequestParam(value="items_por_pag", required=true) int items_por_pag,
           @RequestParam(value="pag_start", required=true) int pag_start,
           @RequestParam(value="display_pag", required=true) String display_pag,
           @RequestParam(value="input_json", required=true) String input_json,
           @RequestParam(value="cadena_busqueda", required=true) String cadena_busqueda,
           @RequestParam(value="iu", required=true) String id_user_cod,
           Model modcel) {
        
        HashMap<String,ArrayList<HashMap<String, Object>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, Object>>>();
        HashMap<String,String> has_busqueda = StringHelper.convert2hash(StringHelper.ascii2string(cadena_busqueda));
        
        //aplicativo Centro de Costos
        Integer app_selected = 94;
        //Integer app_selected = 125;
        
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        
        //variables para el buscador
        String folio = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("folio")))+"%";
        String orden_compra = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("orden_compra")))+"%";
        String folio_doc = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("folio_doc")))+"%";
        String cliente = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("cliente")))+"%";
        String fecha_inicial = ""+StringHelper.isNullString(String.valueOf(has_busqueda.get("fecha_inicial")))+"";
        String fecha_final = ""+StringHelper.isNullString(String.valueOf(has_busqueda.get("fecha_final")))+"";
        String tipo_doc = StringHelper.isNullString(String.valueOf(has_busqueda.get("tipo_doc")));
        String codigo = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("codigo")))+"%";
        
        String data_string = app_selected+"___"+id_usuario+"___"+folio+"___"+orden_compra+"___"+folio_doc+"___"+cliente+"___"+fecha_inicial+"___"+fecha_final+"___"+tipo_doc+"___"+codigo;
        
        //obtiene total de registros en base de datos, con los parametros de busqueda
        int total_items = this.getInvDao().countAll(data_string);
        
        //calcula el total de paginas
        int total_pags = resource.calculaTotalPag(total_items,items_por_pag);
        
        //variables que necesita el datagrid, para no tener que hacer uno por cada aplicativo
        DataPost dataforpos = new DataPost(orderby, desc, items_por_pag, pag_start, display_pag, input_json, cadena_busqueda,total_items,total_pags,id_user_cod);
        
        int offset = resource.__get_inicio_offset(items_por_pag, pag_start);
        
        //obtiene los registros para el grid, de acuerdo a los parametros de busqueda
        jsonretorno.put("Data", this.getInvDao().getInvOrdenSalida_PaginaGrid(data_string, offset, items_por_pag, orderby, desc));
        
        //obtiene el hash para los datos que necesita el datagrid
        jsonretorno.put("DataForGrid", dataforpos.formaHashForPos(dataforpos));
        
        return jsonretorno;
    }
    
    
    
    
    
    @RequestMapping(method = RequestMethod.POST, value="/getDatosCalculoCosto.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getDatosCalculoCostoJson(
            @RequestParam(value="identificador", required=true) Integer identificador,
            @RequestParam(value="iu", required=true) String id_user_cod,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getDatosCalculoCostoJson de {0}", InvControlCostosController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        ArrayList<HashMap<String, String>> datos = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> tiposProducto = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> marcas = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> familias = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> subfamilias = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> presentaciones = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> arrayExtra = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> extra = new HashMap<String, String>();
        String id_tipo_producto="";
        
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        userDat = this.getHomeDao().getUserById(id_usuario);
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        
        tiposProducto = this.getInvDao().getProducto_Tipos();
        
        if( identificador != 0  ){
            datos = this.getInvDao().getProducto_Datos(identificador);
            id_tipo_producto = datos.get(0).get("tipo_prod_id");
        }else{
            id_tipo_producto = "1";//Tipo de Producto TERMINADO(Para que se traiga por default las Familias de Productos Terminados)
        }
        
        marcas = this.getInvDao().getProducto_Marcas(id_empresa);
        familias = this.getInvDao().getInvProdSubFamiliasByTipoProd(id_empresa, id_tipo_producto);
        
        //Se le pasa como parametro el cero para que devuelva todas las presentaciones 
        presentaciones = this.getInvDao().getProducto_Presentaciones(0);
        
        jsonretorno.put("Datos",datos);
        jsonretorno.put("Marcas", marcas);
        jsonretorno.put("Familias", familias);
        jsonretorno.put("ProdTipos", tiposProducto);
        jsonretorno.put("Presentaciones", presentaciones);
        
        return jsonretorno;
    }
    
    
    
    
    
    //obtiene los Familias del producto seleccionado
    @RequestMapping(method = RequestMethod.POST, value="/getSubFamiliasByFamProd.json")
    //public @ResponseBody HashMap<java.lang.String,java.lang.Object> getProveedorJson(
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getSubFamiliasByFamProdJson(
            @RequestParam(value="fam", required=true) String familia_id,
            @RequestParam(value="iu", required=true) String id_user_cod,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getSubFamiliasByFamProdJson de {0}", InvControlCostosController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> subfamilias = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        userDat = this.getHomeDao().getUserById(id_usuario);
        
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        
        subfamilias = this.getInvDao().getProducto_Subfamilias(id_empresa,familia_id );
        
        jsonretorno.put("SubFamilias", subfamilias);
        
        return jsonretorno;
    }
    
    //obtiene los Familias del producto seleccionado
    @RequestMapping(method = RequestMethod.POST, value="/getFamiliasByTipoProd.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getFamiliasByTipoProdJson(
            @RequestParam(value="tipo_prod", required=true) String tipo_prod,
            @RequestParam(value="iu", required=true) String id_user_cod,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getFamiliasByTipoProdJson de {0}", InvControlCostosController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> familias = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        userDat = this.getHomeDao().getUserById(id_usuario);
        
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        
        familias = this.getInvDao().getInvProdSubFamiliasByTipoProd(id_empresa, tipo_prod);
        
        jsonretorno.put("Familias", familias);
        
        return jsonretorno;
    }
    
    
    //obtiene los productos para el buscador
    @RequestMapping(method = RequestMethod.POST, value="/getBuscadorProductos.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getBuscadorProductosJson(
            @RequestParam(value="marca", required=true) String marca,
            @RequestParam(value="familia", required=true) String familia,
            @RequestParam(value="subfamilia", required=true) String subfamilia,
            @RequestParam(value="sku", required=true) String sku,
            @RequestParam(value="tipo", required=true) String tipo,
            @RequestParam(value="descripcion", required=true) String descripcion,
            @RequestParam(value="iu", required=true) String id_user,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getBuscadorProductosJson de {0}", InvControlCostosController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> productos = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user));
        userDat = this.getHomeDao().getUserById(id_usuario);
        
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        
        productos = this.getInvDao().getBuscadorProductosParaControlCostos(marca, familia, subfamilia, sku, tipo, descripcion, id_empresa);
        
        jsonretorno.put("productos", productos);
        
        return jsonretorno;
    }
    
    
    
    
    //obtiene Productos para el Grid,
    //tambien calcula costos cuando es Simulacion
    @RequestMapping(method = RequestMethod.POST, value="/getBusquedaProductos.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getBusquedaProductosJson(
            @RequestParam(value="tipo_prod", required=true) String tipo_prod,
            @RequestParam(value="mar", required=true) String mar,
            @RequestParam(value="fam", required=true) String fam,
            @RequestParam(value="subfam", required=true) String subfam,
            @RequestParam(value="producto", required=true) String producto,
            @RequestParam(value="pres", required=true) String pres,
            @RequestParam(value="tipo_costo", required=true) String tipo_costo,
            @RequestParam(value="simulacion", required=true) String simulacion,
            @RequestParam(value="importacion", required=true) String importacion,
            @RequestParam(value="directo", required=true) String directo,
            @RequestParam(value="pminimo", required=true) String pminimo,
            @RequestParam(value="tc", required=true) String tc,
            @RequestParam(value="iu", required=true) String id_user_cod,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getBusquedaProductosJson de {0}", InvControlCostosController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> productos = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        userDat = this.getHomeDao().getUserById(id_usuario);
        
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        
        //aplicativo Control de Costos
        Integer app_selected = 125;
        
        if(importacion.equals("")){
            importacion="0";
        }
        
        if(directo.equals("")){
            directo="0";
        }
        
        if(pminimo.equals("")){
            pminimo="0";
        }
        
        String data_string = app_selected+"___"+id_usuario+"___"+tipo_prod+"___"+mar+"___"+fam+"___"+subfam+"___"+producto+"___"+pres+"___"+tipo_costo+"___"+simulacion+"___"+importacion+"___"+directo+"___"+pminimo+"___"+tc;
        
        productos = this.getInvDao().selectFunctionForInvReporte(app_selected, data_string);
        
        jsonretorno.put("Grid", productos);
        
        return jsonretorno;
    }
    
    

                    
                    
    //Actualizar registros
    @RequestMapping(method = RequestMethod.POST, value="/edit.json")
    public @ResponseBody HashMap<String, String> editJson(
            @RequestParam(value="select_tipo_prod", required=true) String select_tipo_prod,
            @RequestParam(value="select_marca", required=true) String select_marca,
            @RequestParam(value="select_familia", required=true) String select_familia,
            @RequestParam(value="select_subfamilia", required=true) String select_subfamilia,
            @RequestParam(value="id_producto", required=true) String id_producto,
            @RequestParam(value="select_presentacion", required=true) String select_presentacion,
            @RequestParam(value="tipo_costo", required=true) String tipo_costo,
            @RequestParam(value="costo_importacion", required=true) String costo_importacion,
            @RequestParam(value="costo_directo", required=true) String costo_directo,
            @RequestParam(value="precio_minimo", required=true) String precio_minimo,
            @RequestParam(value="tipo_cambio", required=true) String tipo_cambio,
            @ModelAttribute("user") UserSessionData user,
            Model model
        ) {
            
            HashMap<String, String> jsonretorno = new HashMap<String, String>();
            HashMap<String, String> succes = new HashMap<String, String>();
            String extra_data_array = "'sin datos'";
            String actualizo="0";
            
            
            Integer app_selected = 125; //aplicativo Control de Costos
            String command_selected = "new";
            Integer id_usuario= user.getUserId();//variable para el id  del usuario
            
            command_selected = "edit";
            
            //la accion es para confirmar
            String data_string = 
                    app_selected+"___"+
                    command_selected+"___"+
                    id_usuario+"___"+
                    select_tipo_prod+"___"+
                    select_marca+"___"+
                    select_familia+"___"+
                    select_subfamilia+"___"+
                    id_producto+"___"+
                    select_presentacion+"___"+
                    tipo_costo+"___"+
                    costo_importacion+"___"+
                    costo_directo+"___"+
                    precio_minimo;
            
            actualizo = this.getInvDao().selectFunctionForApp_MovimientosInventario(data_string, extra_data_array);
            
            
            if(actualizo.equals("1")){
                jsonretorno.put("success","true");
            }else{
                jsonretorno.put("success","false");
            }
            
            log.log(Level.INFO, "Salida json {0}", actualizo);
        return jsonretorno;
    }

    
    
    
    
    
    
    
    
    
    
    
}
