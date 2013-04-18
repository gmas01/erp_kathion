/*******************************************************************************
 * Modulo: ENVASADO
 * Aplicativo: REENVASADO
 * @author Noe Martinez
 * gpmarsan@gmail.com
 * 12/abril/2012
 *******************************************************************************/
package com.agnux.kemikal.controllers;

import com.agnux.cfd.v2.Base64Coder;
import com.agnux.common.helpers.StringHelper;
import com.agnux.common.obj.DataPost;
import com.agnux.common.obj.ResourceProject;
import com.agnux.common.obj.UserSessionData;
import com.agnux.kemikal.interfacedaos.EnvInterfaceDao;
import com.agnux.kemikal.interfacedaos.GralInterfaceDao;
import com.agnux.kemikal.interfacedaos.HomeInterfaceDao;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;


@Controller
@SessionAttributes({"user"})
@RequestMapping("/envreenv/")
public class EnvReenvController {
    ResourceProject resource = new ResourceProject();
    private static final Logger log  = Logger.getLogger(EnvReenvController.class.getName());
    
    @Autowired
    @Qualifier("daoGral")
    private GralInterfaceDao gralDao;
    
    @Autowired
    @Qualifier("daoHome")
    private HomeInterfaceDao homeDao;
    
    //dao para Modulo de Envasado
    @Autowired
    @Qualifier("daoEnv")
    private EnvInterfaceDao envDao;

    public EnvInterfaceDao getEnvDao() {
        return envDao;
    }
    
    public GralInterfaceDao getGralDao() {
        return gralDao;
    }
    
    public HomeInterfaceDao getHomeDao() {
        return homeDao;
    }
    
    @RequestMapping(value="/startup.agnux")
    public ModelAndView startUp(HttpServletRequest request, HttpServletResponse response, 
            @ModelAttribute("user") UserSessionData user
        )throws ServletException, IOException {
        
        log.log(Level.INFO, "Ejecutando starUp de {0}", EnvReenvController.class.getName());
        LinkedHashMap<String,String> infoConstruccionTabla = new LinkedHashMap<String,String>();
        infoConstruccionTabla.put("id", "Acciones:70");
        infoConstruccionTabla.put("folio", "Folio:70");
        infoConstruccionTabla.put("almacen", "Almacen Origen:140");
        infoConstruccionTabla.put("codigo", "C&oacute;digo:120");
        infoConstruccionTabla.put("descripcion", "Descripci&oacute;n:250");
        infoConstruccionTabla.put("presentacion", "Presentaci&oacute;n:110");
        infoConstruccionTabla.put("empleado", "Empleado:180");
        infoConstruccionTabla.put("fecha", "Fecha:65");
        infoConstruccionTabla.put("hora", "Hora:60");
        infoConstruccionTabla.put("status", "Estado:70");
        
        ModelAndView x = new ModelAndView("envreenv/startup", "title", "Proceso de Re-Envasado");
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
    
    
    
    @RequestMapping(value="/getAllReenv.json", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, Object>>> getAllReenvJson(
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
        
        //Aplicativo de Reenvasado(ENV)
        Integer app_selected = 138;
        
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        
        //variables para el buscador
        String folio = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("folio")))+"%";
        String almacen = StringHelper.isNullString(String.valueOf(has_busqueda.get("almacen")));
        String codigo = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("codigo")))+"%";
        String descripcion = "%"+StringHelper.isNullString(String.valueOf(has_busqueda.get("descripcion")))+"%";
        String presentacion = StringHelper.isNullString(String.valueOf(has_busqueda.get("presentacion")));
        String empleado = StringHelper.isNullString(String.valueOf(has_busqueda.get("empleado")));
        String estado = StringHelper.isNullString(String.valueOf(has_busqueda.get("estado")));
        String fecha_inicial = ""+StringHelper.isNullString(String.valueOf(has_busqueda.get("fecha_inicial")))+"";
        String fecha_final = ""+StringHelper.isNullString(String.valueOf(has_busqueda.get("fecha_final")))+"";
        
        String data_string = app_selected+"___"+id_usuario+"___"+folio+"___"+almacen+"___"+codigo+"___"+descripcion+"___"+presentacion+"___"+empleado+"___"+estado+"___"+fecha_inicial+"___"+fecha_final;
        
        //obtiene total de registros en base de datos, con los parametros de busqueda
        int total_items = this.getEnvDao().countAll(data_string);
        
        //calcula el total de paginas
        int total_pags = resource.calculaTotalPag(total_items,items_por_pag);
        
        //variables que necesita el datagrid, para no tener que hacer uno por cada aplicativo
        DataPost dataforpos = new DataPost(orderby, desc, items_por_pag, pag_start, display_pag, input_json, cadena_busqueda,total_items,total_pags, id_user_cod);
        
        int offset = resource.__get_inicio_offset(items_por_pag, pag_start);
        
        //obtiene los registros para el grid, de acuerdo a los parametros de busqueda
        jsonretorno.put("Data", this.getEnvDao().getReEenv_PaginaGrid(data_string, offset, items_por_pag, orderby, desc));
        
        //obtiene el hash para los datos que necesita el datagrid
        jsonretorno.put("DataForGrid", dataforpos.formaHashForPos(dataforpos));
        
        return jsonretorno;
    }
    
    
    
    
    //obtiene datos Buscador principal
    @RequestMapping(method = RequestMethod.POST, value="/getDatosBuscadorPrincipal.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getDatosBuscadorPrincipalJson(
            @RequestParam(value="iu", required=true) String id_user_cod,
            Model model
        ) {
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        ArrayList<HashMap<String, String>> tiposProducto = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> presentaciones = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> empleados = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> almacenes = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> estatus = new ArrayList<HashMap<String, String>>();
        
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user_cod));
        userDat = this.getHomeDao().getUserById(id_usuario);
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        Integer id_sucursal = Integer.parseInt(userDat.get("sucursal_id"));
        
        tiposProducto = this.getEnvDao().getProductoTipos();
        
        //Se le pasa como parametro el cero para que devuelva todas las presentaciones 
        presentaciones = this.getEnvDao().getProductoPresentaciones(0, id_empresa);
        
        empleados = this.getEnvDao().getReEenv_Empleados(id_empresa);
        almacenes = this.getEnvDao().getAlmacenes(id_empresa, id_sucursal);
        estatus = this.getEnvDao().getEstatus();
        
        jsonretorno.put("ProdTipos", tiposProducto);
        jsonretorno.put("Presentaciones", presentaciones);
        jsonretorno.put("Empleados", empleados);
        jsonretorno.put("Almacenes", almacenes);
        jsonretorno.put("Estatus", estatus);
        return jsonretorno;
    }
    
    
    //Obtiene datos de Una COnfiguracion
    @RequestMapping(method = RequestMethod.POST, value="/getReenv.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getReenvJson(
            @RequestParam(value="id", required=true) Integer id,
            @RequestParam(value="iu", required=true) String id_user,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getConfJson de {0}", EnvReenvController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        ArrayList<HashMap<String, String>> datosenvreenv = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> datosGrid = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> presentaciones = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> empleados = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> almacenes = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> estatus = new ArrayList<HashMap<String, String>>();
        
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user));
        userDat = this.getHomeDao().getUserById(id_usuario);
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        Integer id_sucursal = Integer.parseInt(userDat.get("sucursal_id"));
        
        if( id != 0  ){
            datosenvreenv = this.getEnvDao().getReEenv_Datos(id);
            datosGrid = this.getEnvDao().getReEenv_DatosGrid(id);
            presentaciones=this.getEnvDao().getProductoPresentacionesON(Integer.parseInt(datosenvreenv.get(0).get("producto_id")));
        }
        
        empleados = this.getEnvDao().getReEenv_Empleados(id_empresa);
        almacenes = this.getEnvDao().getAlmacenes(id_empresa, id_sucursal);
        estatus = this.getEnvDao().getEstatus();
        
        jsonretorno.put("Datos", datosenvreenv);
        jsonretorno.put("DatosGrid", datosGrid);
        jsonretorno.put("Presentaciones", presentaciones);
        jsonretorno.put("Empleados", empleados);
        jsonretorno.put("Almacenes", almacenes);
        jsonretorno.put("Estatus", estatus);
        
        return jsonretorno;
    }
    
    
    
    //obtiene los productos para el buscador
    @RequestMapping(method = RequestMethod.POST, value="/getBuscadorProductos.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getBuscadorProductosJson(
            @RequestParam(value="sku", required=true) String sku,
            @RequestParam(value="tipo", required=true) String tipo,
            @RequestParam(value="descripcion", required=true) String descripcion,
            @RequestParam(value="iu", required=true) String id_user,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getProductosJson de {0}", EnvReenvController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> productos = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user));
        userDat = this.getHomeDao().getUserById(id_usuario);
        
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        productos = this.getEnvDao().getBuscadorProductos(sku, tipo, descripcion, id_empresa);
        
        jsonretorno.put("Productos", productos);
        
        return jsonretorno;
    }
    
    
    //obtiene los Datos de Un producto en especifico a partir del Codigo(SKU)
    @RequestMapping(method = RequestMethod.POST, value="/gatDatosProducto.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> gatDatosProductoJson(
            @RequestParam(value="codigo", required=true) String codigo,
            @RequestParam(value="iu", required=true) String id_user,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando gatDatosProductoJson de {0}", EnvReenvController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> producto = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> presentaciones = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> envases = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> userDat = new HashMap<String, String>();
        
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user));
        userDat = this.getHomeDao().getUserById(id_usuario);
        
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        
        producto = this.getEnvDao().getDataProductBySku(codigo, id_empresa);
        Integer idProducto=0;
        if(producto.size()>0){
            idProducto = Integer.parseInt(producto.get(0).get("id"));
            presentaciones=this.getEnvDao().getProductoPresentacionesON(idProducto);
            envases=this.getEnvDao().getEnvasesPorProducto(idProducto);
        }
        
        jsonretorno.put("Producto", producto);
        jsonretorno.put("Presentaciones", presentaciones);
        jsonretorno.put("Envases", envases);
        
        return jsonretorno;
    }
    
    
    //obtiene las presentaciones de un producto en especifico a partir de Id de Producto
    @RequestMapping(method = RequestMethod.POST, value="/getPresentacionesProducto.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getPresentacionesProductoJson(
            @RequestParam(value="id_prod", required=true) Integer id_prod,
            Model model
        ) {
        
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> presentaciones = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> envases = new ArrayList<HashMap<String, String>>();
        
        presentaciones=this.getEnvDao().getProductoPresentacionesON(id_prod);
        envases=this.getEnvDao().getEnvasesPorProducto(id_prod);
        
        jsonretorno.put("Presentaciones", presentaciones);
        jsonretorno.put("Envases", envases);
        
        return jsonretorno;
    }
    
    
    
    //Obtiene la existencia de un Producto en una presentacion en un almacen
    @RequestMapping(method = RequestMethod.POST, value="/getExisPres.json")
    public @ResponseBody HashMap<String,ArrayList<HashMap<String, String>>> getExisPresJson(
            @RequestParam(value="id_prod", required=true) Integer id_prod,
            @RequestParam(value="id_pres", required=true) Integer id_pres,
            @RequestParam(value="id_alm", required=true) Integer id_alm,
            @RequestParam(value="iu", required=true) String id_user,
            Model model
        ) {
        
        log.log(Level.INFO, "Ejecutando getExisPresJson de {0}", EnvReenvController.class.getName());
        HashMap<String,ArrayList<HashMap<String, String>>> jsonretorno = new HashMap<String,ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> existencias = new ArrayList<HashMap<String, String>>();
        //HashMap<String, String> userDat = new HashMap<String, String>();
        /*
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user));
        userDat = this.getHomeDao().getUserById(id_usuario);
        Integer id_empresa = Integer.parseInt(userDat.get("empresa_id"));
        */
        existencias = this.getEnvDao().getReEenv_Existencias(id_prod, id_pres, id_alm);
        
        jsonretorno.put("Exis", existencias);
        
        return jsonretorno;
    }
    
    





















    //edicion y nuevo
    @RequestMapping(method = RequestMethod.POST, value="/edit.json")
    public @ResponseBody HashMap<String, String> editJson(
            @RequestParam(value="identificador", required=true) Integer identificador,
            @RequestParam(value="select_estatus", required=true) String select_estatus,
            @RequestParam(value="fecha", required=true) String fecha,
            @RequestParam(value="hora", required=true) String hora,
            @RequestParam(value="select_empleado", required=true) String select_empleado,
            @RequestParam(value="select_almacen_orig", required=true) String select_almacen_orig,
            @RequestParam(value="producto_id", required=true) String producto_id,
            @RequestParam(value="select_presentacion_orig", required=true) String select_presentacion_orig,
            @RequestParam(value="exis_pres", required=true) String exis_pres,
            @RequestParam(value="disp_pres", required=true) String disp_pres,
            @RequestParam(value="eliminado", required=false) String[] eliminado,
            @RequestParam(value="iddetalle", required=false) String[] iddetalle,
            @RequestParam(value="select_aml_dest", required=false) String[] select_aml_dest,
            @RequestParam(value="cantpres", required=false) String[] cantpres,
            @RequestParam(value="idEnv", required=false) String[] idEnv,
            @RequestParam(value="noDec", required=false) String[] noDec,
            @RequestParam(value="notr", required=false) String[] notr,
            @ModelAttribute("user") UserSessionData user
        ) {
            
            System.out.println("Guardar del Pedido");
            HashMap<String, String> jsonretorno = new HashMap<String, String>();
            HashMap<String, String> succes = new HashMap<String, String>();
            
            //Aplicativo de Reenvasado(ENV)
            Integer app_selected = 138;
            String command_selected = "new";
            Integer id_usuario= user.getUserId();//variable para el id  del usuario
            
            String arreglo[];
            arreglo = new String[notr.length];
            
            for(int i=0; i<notr.length; i++) { 
                select_aml_dest[i] = StringHelper.verificarSelect(select_aml_dest[i]);
                if(idEnv[i].equals("")){ idEnv[i]="0"; }
                
                arreglo[i]= "'"+eliminado[i] +"___"+ notr[i] +"___" + iddetalle[i] +"___" + select_aml_dest[i] +"___" + idEnv[i] +"___" + cantpres[i] +"'";
                System.out.println(arreglo[i]);
            }
            
            //serializar el arreglo
            String extra_data_array = StringUtils.join(arreglo, ",");
            
            if( identificador==0 ){
                command_selected = "new";
            }else{
                command_selected = "edit";
            }
            
            select_empleado = StringHelper.verificarSelect(select_empleado);
            select_almacen_orig = StringHelper.verificarSelect(select_almacen_orig);
            select_presentacion_orig = StringHelper.verificarSelect(select_presentacion_orig);
            
            if(producto_id.equals("")){ producto_id="0"; }
            
            String data_string = 
                    app_selected+"___"+
                    command_selected+"___"+
                    id_usuario+"___"+
                    identificador+"___"+
                    select_estatus+"___"+
                    fecha+"___"+
                    hora+"___"+
                    select_empleado+"___"+
                    select_almacen_orig+"___"+
                    producto_id+"___"+
                    select_presentacion_orig;
            
            //System.out.println("data_string: "+data_string);
            
            succes = this.getEnvDao().selectFunctionValidateAplicativo(data_string,extra_data_array);
            
            log.log(Level.INFO, "despues de validacion {0}", String.valueOf(succes.get("success")));
            String actualizo = "0";
            
            if( String.valueOf(succes.get("success")).equals("true") ){
                actualizo = this.getEnvDao().selectFunctionForThisApp(data_string, extra_data_array);
                jsonretorno.put("actualizo",String.valueOf(actualizo));
            }
            
            String successValidation = succes.get("success").replace("SALTOLINEA", "<br>");
            jsonretorno.put("success",String.valueOf(successValidation));
            
            log.log(Level.INFO, "Salida json {0}", String.valueOf(jsonretorno.get("success")));
        return jsonretorno;
    }
    
    
    
    
    //cambiar a borrado logico un registro
    @RequestMapping(method = RequestMethod.POST, value="/logicDelete.json")
    public @ResponseBody HashMap<String, String> logicDeleteJson(
            @RequestParam(value="id", required=true) Integer id,
            @RequestParam(value="iu", required=true) String id_user,
            Model model
        ) {
        
        HashMap<String, String> jsonretorno = new HashMap<String, String>();
        //decodificar id de usuario
        Integer id_usuario = Integer.parseInt(Base64Coder.decodeString(id_user));
        
        //Aplicativo de Reenvasado(ENV)
        Integer app_selected = 138;
        String command_selected = "delete";
        String extra_data_array = "'sin datos'";
        String data_string = app_selected+"___"+command_selected+"___"+id_usuario+"___"+id;
        
        System.out.println("Ejecutando borrado logico de Registro de Re-Envasado");
        jsonretorno.put("success",String.valueOf( this.getEnvDao().selectFunctionForThisApp(data_string,extra_data_array)) );
        
        return jsonretorno;
    }
}