package com.auca.library.service;

import com.auca.library.dao.LocationDAO;
import com.auca.library.domain.Location;
import com.auca.library.domain.enums.LocationType;
import java.util.UUID;

public class LocationService {

    private final LocationDAO locationDAO = new LocationDAO();

    public Location createLocation(Location location, UUID parentId) {
        if (location.getLocationType() == LocationType.PROVINCE && parentId != null) {
            throw new IllegalArgumentException("Province cannot have a parent.");
        }
        if (location.getLocationType() != LocationType.PROVINCE && parentId == null) {
            throw new IllegalArgumentException(location.getLocationType() + " must have a parent.");
        }
        return locationDAO.createLocation(location, parentId);
    }

    public String getProvinceNameByVillageId(UUID villageId) {
        String name = locationDAO.getProvinceNameByVillageId(villageId);
        if (name == null) {
            throw new IllegalArgumentException("province not found for village: " + villageId);
        }
        return name;
    }
}
