package nz.ac.canterbury.seng302.gardenersgrove.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and managing Garden entities.
 */
@Repository
public interface GardenRepository extends JpaRepository<Garden, Long> {
    /**
     * Retrieves a garden by its unique identifier.
     *
     * @param id The unique identifier of the garden.
     * @return An Optional containing the garden if found, otherwise empty.
     */
    Optional<Garden> findById(long id);

    /**
     * Retrieves all gardens stored in the repository.
     *
     * @return A list of all gardens stored in the repository.
     */
    List<Garden> findAll();

    /**
     * Retrieves a page of gardens
     * @param pageable a pageable object
     * @return a page of gardens
     */
    Page<Garden> findAll(Pageable pageable);

    /**
     * Retrieves a list of gardens by their owner's gardener ID.
     *
     * @param gardenerId The identifier of the garden's owner.
     * @return A list of all gardens with the specified owner stored in the repository.
     */
    List<Garden> findByGardenerId(Long gardenerId);

    /**
     * Updates the last notified date of a garden by its id
     *
     * @param gardenId The identifier of the garden
     * @param date     The date to set as the last notified date
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE garden SET last_notified = ?2 WHERE id = ?1 ", nativeQuery = true)
    void updateLastNotifiedbyId(Long gardenId, LocalDate date);

    /**
     * Gets all public gardens paginated
     *
     * @param pageable The pageable specifying relevant information for pagination
     */
    @Query(value= "select * from garden where public_garden is true", nativeQuery = true)
    Page<Garden> findAllPublicGardens(Pageable pageable);

    /**
     * Gets all public gardens that have a name or plant name matching the search term paginated
     *
     * @param pageable The pageable specifying relevant information for pagination
     * @param searchTerm the term to search garden and plant names for
     */
    @Query(value = "SELECT DISTINCT g.* FROM garden g LEFT JOIN plant p ON g.id = p.garden_id WHERE g.public_garden IS TRUE AND (g.name LIKE %:searchTerm% OR p.name LIKE %:searchTerm%)", nativeQuery = true)
    Page<Garden> findGardensBySearchTerm(Pageable pageable, @Param("searchTerm") String searchTerm);
}
