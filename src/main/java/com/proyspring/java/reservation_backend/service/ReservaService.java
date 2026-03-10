package com.proyspring.java.reservation_backend.service;

import com.proyspring.java.reservation_backend.exception.ReglaNegocioException;
import com.proyspring.java.reservation_backend.model.Reservation;
import com.proyspring.java.reservation_backend.model.ReservationStatus;
import com.proyspring.java.reservation_backend.repository.ReservationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {

    private final ReservationRepository reservationRepository;

    public ReservaService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> listarReservas() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation crearReserva(Reservation reserva) {
        boolean fechaHoraOcupada = reservationRepository.existsByDateAndTime(reserva.getFecha(), reserva.getHora());
        if (fechaHoraOcupada) {
            throw new ReglaNegocioException("Ya existe una reserva para la misma fecha y hora");
        }

        if (reserva.getEstado() == null) {
            reserva.setEstado(ReservationStatus.ACTIVA);
        }

        return reservationRepository.save(reserva);
    }

    @Transactional
    public Reservation cancelarReserva(Long id) {
        Reservation reserva = reservationRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe una reserva con id: " + id));

        if (ReservationStatus.CANCELADA.equals(reserva.getEstado())) {
            throw new ReglaNegocioException("La reserva ya se encuentra cancelada");
        }

        reserva.setEstado(ReservationStatus.CANCELADA);
        return reservationRepository.save(reserva);
    }
}
