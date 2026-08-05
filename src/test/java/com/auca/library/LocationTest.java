package com.auca.library;

import com.auca.library.domain.Location;
import com.auca.library.domain.enums.LocationType;
import com.auca.library.service.LocationService;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class LocationTest {

    private LocationService locationService;

    @Before
    public void setUp() {
        locationService = new LocationService();
    }

    @Test
    public void createProvince_withNoParent_succeeds() {
        Location province = new Location();
        province.setLocationCode("PROV-" + UUID.randomUUID());
        province.setLocationName("Kigali Province");
        province.setLocationType(LocationType.PROVINCE);

        Location saved = locationService.createLocation(province, null);

        assertNotNull(saved.getLocationId());
        assertEquals(LocationType.PROVINCE, saved.getLocationType());
        assertNull(saved.getParent());
    }

    @Test
    public void createDistrict_withValidProvinceParent_succeeds() {
        // First create a province
        Location province = new Location();
        province.setLocationCode("PROV-" + UUID.randomUUID());
        province.setLocationName("Eastern Province");
        province.setLocationType(LocationType.PROVINCE);
        Location savedProvince = locationService.createLocation(province, null);

        // Then create a district under it
        Location district = new Location();
        district.setLocationCode("DIST-" + UUID.randomUUID());
        district.setLocationName("Gasabo District");
        district.setLocationType(LocationType.DISTRICT);

        Location savedDistrict = locationService.createLocation(district, savedProvince.getLocationId());

        assertNotNull(savedDistrict.getLocationId());
        assertEquals(LocationType.DISTRICT, savedDistrict.getLocationType());
        assertNotNull(savedDistrict.getParent());
        assertEquals(savedProvince.getLocationId(), savedDistrict.getParent().getLocationId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDistrict_withMissingParent_throwsException() {
        Location district = new Location();
        district.setLocationCode("DIST-" + UUID.randomUUID());
        district.setLocationName("Gasabo District");
        district.setLocationType(LocationType.DISTRICT);

        // Pass a random UUID that does not exist in DB
        locationService.createLocation(district, UUID.randomUUID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLocation_duplicateLocationCode_throwsException() {
        String sharedCode = "DUP-001";

        Location first = new Location();
        first.setLocationCode(sharedCode);
        first.setLocationName("First Province");
        first.setLocationType(LocationType.PROVINCE);
        locationService.createLocation(first, null);

        Location duplicate = new Location();
        duplicate.setLocationCode(sharedCode);
        duplicate.setLocationName("Duplicate Province");
        duplicate.setLocationType(LocationType.PROVINCE);
        locationService.createLocation(duplicate, null);
    }
}
