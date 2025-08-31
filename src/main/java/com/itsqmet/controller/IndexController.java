package com.itsqmet.controller;

import com.itsqmet.entity.Demo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping ("/")
    public String mostrarHome() {
        return "index";
    }
    @GetMapping ("/salonesBelleza")
    public String mostrarSalonesBelleza() {
        return "/pages/salonesBelleza";
    }
    @GetMapping ("/clinicas")
    public String mostrarClinicas() {
        return "/pages/clinicas";
    }
    @GetMapping ("/consultorias")
    public String mostrarConsultorias() {
        return "/pages/consultorias";
    }
    @GetMapping ("/educacion")
    public String mostrarEducacion() {
        return "/pages/educacion";
    }
    @GetMapping ("/talleres")
    public String mostrarTalleres() {
        return "/pages/talleres";
    }
    @GetMapping("/demo")
    public String mostrarDemo(Model model) {
        model.addAttribute("demo", new Demo());
        return "pages/demo";
    }
    @GetMapping ("/gestionPersonal")
    public String mostrarGestionPersonal() {
        return "/pages/gestionPersonal";
    }
    @GetMapping ("/agendamiento")
    public String mostrarAgendamiento() {
        return "/pages/agendamiento";
    }
    @GetMapping ("/control")
    public String mostrarControl() {
        return "/pages/control";
    }
    @GetMapping ("/precios")
    public String mostrarPrecios() {
        return "/pages/precios";
    }
    @GetMapping ("/agradecimiento")
    public String mostrarAgradecimiento() {
        return "/pages/agradecimiento";
    }

}
