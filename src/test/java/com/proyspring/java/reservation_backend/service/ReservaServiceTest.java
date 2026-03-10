package com.proyspring.java.reservation_backend.service;

import com.proyspring.java.reservation_backend.exception.ReglaNegocioException;
import com.proyspring.java.reservation_backend.model.Reservation;
import com.proyspring.java.reservation_backend.model.ReservationStatus;
import com.proyspring.java.reservation_backend.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Reservation reserva;

    @BeforeEach
    void setUp() {
        reserva = new Reservation();
        reserva.setId(1L);
        reserva.setNombreCliente("Juan Perez");
        reserva.setFecha(LocalDate.of(2025, 6, 15));
        reserva.setHora(LocalTime.of(10, 0));
        reserva.setServicio("Corte de cabello");
        reserva.setEstado(ReservationStatus.ACTIVA);
    }

    @Test
    void listarReservas_devuelveListaDeReservas() {
        when(reservationRepository.findAll()).thenReturn(List.of(reserva));

        List<Reservation> resultado = reservaService.listarReservas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreCliente()).isEqualTo("Juan Perez");
    }

    @Test
    void crearReserva_guardaReservaConEstadoActiva() {
        Reservation nueva = new Reservation();
        nueva.setNombreCliente("Ana Lopez");
        nueva.setFecha(LocalDate.of(2025, 7, 20));
        nueva.setHora(LocalTime.of(14, 30));
        nueva.setServicio("Manicure");

        when(reservationRepository.existsByDateAndTime(nueva.getFecha(), nueva.getHora())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation resultado = reservaService.crearReserva(nueva);

        assertThat(resultado.getEstado()).isEqualTo(ReservationStatus.ACTIVA);
        verify(reservationRepository).save(nueva);
    }

    @Test
    void crearReserva_lanzaExcepcionSiFechaHoraOcupada() {
        when(reservationRepository.existsByDateAndTime(reserva.getFecha(), reserva.getHora())).thenReturn(true);

        assertThatThrownBy(() -> reservaService.crearReserva(reserva))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe una reserva para la misma fecha y hora");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelarReserva_actualizaEstadoACancelada() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation resultado = reservaService.cancelarReserva(1L);

        assertThat(resultado.getEstado()).isEqualTo(ReservationStatus.CANCELADA);
    }

    @Test
    void cancelarReserva_lanzaExcepcionSiNoExiste() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.cancelarReserva(99L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No existe una reserva con id: 99");
    }

    @Test
    void cancelarReserva_lanzaExcepcionSiYaCancelada() {
        reserva.setEstado(ReservationStatus.CANCELADA);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra cancelada");
    }
}
