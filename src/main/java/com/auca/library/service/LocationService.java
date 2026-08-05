package com.auca.library.service;

import com.auca.library.dao.LocationDAO;
import com.auca.library.domain.Location;
import com.auca.library.domain.enums.LocationType;
import java.util.UUID;

public class LocationService {

    private final LocationDAO locationDAO = new LocationDAO();

    public Location createLocation(Location location, UUID parentId) {
        // Province must have no parent
        if (location.getLocationType() == LocationType.PROVINCE && parentId != null) {
            throw new IllegalArgumentException("Province cannot have a parent location.");
        }
        // Non-province must have a parent
        if (location.getLocationType() != LocationType.PROVINCE && parentId == null) {
            throw new IllegalArgumentException(
                    location.getLocationType() + " must have a parent location.");
        }
        return locationDAO.createLocation(location, parentId);
    }
}
