package com.proyspring.java.reservation_backend.controller;

import com.proyspring.java.reservation_backend.controller.advice.GlobalExceptionHandler;
import com.proyspring.java.reservation_backend.exception.ReglaNegocioException;
import com.proyspring.java.reservation_backend.model.Reservation;
import com.proyspring.java.reservation_backend.model.ReservationStatus;
import com.proyspring.java.reservation_backend.service.ReservaService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = JsonMapper.builder().build();
    }

    private Reservation reservaEjemplo() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setNombreCliente("Juan Perez");
        r.setFecha(LocalDate.of(2025, 6, 15));
        r.setHora(LocalTime.of(10, 0));
        r.setServicio("Corte de cabello");
        r.setEstado(ReservationStatus.ACTIVA);
        return r;
    }

    @Test
    void GET_reservas_devuelve200ConListaDeReservas() throws Exception {
        when(reservaService.listarReservas()).thenReturn(List.of(reservaEjemplo()));

        mockMvc.perform(get("/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCliente").value("Juan Perez"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));
    }

    @Test
    void POST_reservas_devuelve201AlCrearReservaValida() throws Exception {
        Reservation nueva = reservaEjemplo();
        nueva.setId(null);

        when(reservaService.crearReserva(any(Reservation.class))).thenReturn(reservaEjemplo());

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(nueva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void POST_reservas_devuelve400SiNombreClienteEsBlanco() throws Exception {
        Reservation invalida = reservaEjemplo();
        invalida.setNombreCliente("");

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void POST_reservas_devuelve409SiFechaHoraOcupada() throws Exception {
        when(reservaService.crearReserva(any(Reservation.class)))
                .thenThrow(new ReglaNegocioException("Ya existe una reserva para la misma fecha y hora"));

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(reservaEjemplo())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe una reserva para la misma fecha y hora"));
    }

    @Test
    void DELETE_reservas_devuelve204AlCancelar() throws Exception {
        when(reservaService.cancelarReserva(1L)).thenReturn(reservaEjemplo());

        mockMvc.perform(delete("/reservas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void DELETE_reservas_devuelve404SiNoExiste() throws Exception {
        when(reservaService.cancelarReserva(99L))
                .thenThrow(new ReglaNegocioException("No existe una reserva con id: 99"));

        mockMvc.perform(delete("/reservas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe una reserva con id: 99"));
    }
}
