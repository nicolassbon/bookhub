package com.tallerwebi.presentacion;

import com.tallerwebi.presentacion.controller.ControladorRegistro;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;



public class ControladorRegistroTest {

    /*
    * 1 - Usuario necesita mail y password para registrarse
    *
    *
     */

   // ControladorRegistro controladorRegistro = new ControladorRegistro();
//
//    @Test
//    public void siExisteMailYPasswordElRegistroEsExitoso(){
//
//        //preparacion --> given (no hace falta hacerlo siempre)
//        givenNoExisteUsuario();
//        //ejecucion --> when
//       ModelAndView mav = whenRegistroUsuario("julibernacchia@gmail.com", "1234", "1234");
//        //comprobacion --> then
//        thenElRegistroEsExitoso(mav);
//
//    }

    private void givenNoExisteUsuario() {
    }

//    private ModelAndView whenRegistroUsuario(String email, String contraseña, String segundaContraseña) {
//        ModelAndView mav = controladorRegistro.registrarUsuario(email, contraseña, segundaContraseña); //metodo que tiene que estar en otra clase (en este caso controlador registro)
//        return mav;
//
//    }

    private void thenElRegistroEsExitoso(ModelAndView mav) {
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
    }

//    //SEGUNDO TEST
//    @Test
//    public void siElEmailEstaVacioElRegistroFalla(){
//        //preparacion --> given (no hace falta hacerlo siempre)
//        givenNoExisteUsuario();
//        //ejecucion --> when
//        String emailVacio = "";
//        ModelAndView mav = whenRegistroUsuario(emailVacio, "1234", "1234");
//        //comprobacion --> then
//        thenElRegistroFalla(mav, "el email es obligatorio. Por favor complete el campo para continuar");
//    }

    private void thenElRegistroFalla(ModelAndView mav, String mensajeErrorEsperado) {
        assertThat(mav.getViewName(), equalToIgnoringCase("registro"));
        assertThat(mav.getModel().get("error").toString(), equalToIgnoringCase(mensajeErrorEsperado));
    }



    //TERCER TEST
//    @Test
//    public void siLaContraseñaEstaVaciaElRegistroFalla(){
//
//        givenNoExisteUsuario();
//
//        String contraseniaVacia = "";
//        ModelAndView mav = whenRegistroUsuario("julibernacchia@gmail.com", contraseniaVacia, "");
//
//        thenElRegistroFalla(mav, "la contraseña es obligatoria. Por favor complete el campo para continuar");
//
//    }

    //CUARTO TEST
//    @Test
//    public void siLasPasswordsSonDistintasElRegistroFalla(){
//        givenNoExisteUsuario();
//
//        String contraseñaErronea = "123";
//        ModelAndView mav = whenRegistroUsuario("julibernacchia@gmail.com", "1234", contraseñaErronea);
//
//        thenElRegistroFalla(mav, "las contraseñas deben coincidir. Intente nuevamente");
//
//    }
}
