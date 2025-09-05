package com.itsqmet.controller;

import com.itsqmet.entity.Negocio;
import com.itsqmet.entity.Profesional;
import com.itsqmet.service.NegocioServicio;
import com.itsqmet.service.ProfesionalServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping ("/admin")
public class AdminController {
    @Autowired
    private ProfesionalServicio profesionalServicio;
    @Autowired
    private NegocioServicio negocioServicio;
    @GetMapping
    public String admin() {
        return "pages/panelAdmin";
    }
    @GetMapping("/adminPanel")
    public String mostrarPanelAdmin(Model model) {
        // Traer todos los profesionales y negocios
        List<Profesional> profesionales = profesionalServicio.obtenerTodosLosProfesionales();
        List<Negocio> negocios = negocioServicio.obtenerTodosLosNegocios();

        model.addAttribute("profesionales", profesionales);
        model.addAttribute("negocios", negocios);

        return "pages/panelAdmin";
    }


}
