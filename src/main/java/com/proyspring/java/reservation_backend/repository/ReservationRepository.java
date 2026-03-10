package com.proyspring.java.reservation_backend.repository;

import com.proyspring.java.reservation_backend.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for {@link Reservation} entities.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Checks whether a reservation exists for the given date and time.
     *
     * @param date the reservation date
     * @param time the reservation time
     * @return true if a reservation exists for the given date and time
     */
    @Query("select (count(r) > 0) from Reservation r where r.fecha = :date and r.hora = :time")
    boolean existsByDateAndTime(@Param("date") LocalDate date, @Param("time") LocalTime time);
}
