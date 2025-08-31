package com.itsqmet.dto;

import com.itsqmet.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class NegocioRegistroDTO {
    private Long idNegocio;
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre completo no debe exceder 100 caracteres")
    private String nombreCompleto;
    @NotBlank(message = "El email profesional es obligatorio")
    @Email(message = "Debe ingresar un email válido")
    private String email;
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") // Recomiendo un mínimo de caracteres
    private String password;
    @NotBlank(message = "El nombre del negocio es obligatorio")
    private String nombreNegocio;
    @NotBlank(message = "El tipo de negocio es obligatorio")
    private String tipoNegocio;
    private String direccion;
    private String telefono;
    @NotBlank(message = "El RUC es obligatorio.")
    @Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "Formato de RUC inválido (10 o 13 dígitos numéricos).")
    private String ruc;
    @NotBlank(message = "El plan del negocio es obligatorio")
    private String plan;
    @Pattern(regexp = "^[0-9]{13,16}$", message = "Número de tarjeta inválido (13-16 dígitos)")
    private String cardNumber;

    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/[0-9]{2}$", message = "Formato de fecha de vencimiento inválido (MM/AA)")
    private String expiryDate;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV inválido (3-4 dígitos)")
    private String cvv;

    private String billingAddress;
    private Rol rol;

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
    public Long getIdNegocio() {
        return idNegocio;
    }

    public void setIdNegocio(Long idNegocio) {
        this.idNegocio = idNegocio;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public void setNombreNegocio(String nombreNegocio) {
        this.nombreNegocio = nombreNegocio;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
    private String fechaHoraInicio;
    private String fechaHoraFin;

    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
}