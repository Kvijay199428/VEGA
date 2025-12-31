package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.IpoCalendarEntity;
import com.vegatrader.upstox.api.rms.entity.IpoCalendarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ipo_calendar.
 * 
 * @since 4.1.0
 */
@Repository
public interface IpoCalendarRepository extends JpaRepository<IpoCalendarEntity, IpoCalendarId> {

    Optional<IpoCalendarEntity> findBySymbolAndExchange(String symbol, String exchange);

    List<IpoCalendarEntity> findByListingDate(LocalDate listingDate);

    @Query("SELECT i FROM IpoCalendarEntity i WHERE i.listingDate = CURRENT_DATE")
    List<IpoCalendarEntity> findTodayListings();

    @Query("SELECT i FROM IpoCalendarEntity i WHERE i.listingDate >= :startDate AND i.listingDate <= :endDate")
    List<IpoCalendarEntity> findByListingDateBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM IpoCalendarEntity i " +
            "WHERE i.symbol = :symbol AND i.exchange = :exchange AND i.listingDate = CURRENT_DATE")
    boolean isListingDay(@Param("symbol") String symbol, @Param("exchange") String exchange);

    @Query("SELECT i FROM IpoCalendarEntity i WHERE i.isActive = true ORDER BY i.listingDate DESC")
    List<IpoCalendarEntity> findActiveIpos();
}
