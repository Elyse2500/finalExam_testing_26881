package com.auca.library;

import com.auca.library.domain.Location;
import com.auca.library.domain.User;
import com.auca.library.domain.enums.Gender;
import com.auca.library.domain.enums.LocationType;
import com.auca.library.domain.enums.RoleType;
import com.auca.library.service.LocationService;
import com.auca.library.service.UserService;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class UserTest {

    private LocationService locationService;
    private UserService userService;

    @Before
    public void setUp() {
        locationService = new LocationService();
        userService = new UserService();
    }

    @Test
    public void validPersonId_returnsCorrectProvinceName() {
        // Build full hierarchy: Province -> District -> Sector -> Cell -> Village
        Location province = new Location();
        province.setLocationCode("PROV-" + UUID.randomUUID());
        province.setLocationName("Western Province");
        province.setLocationType(LocationType.PROVINCE);
        Location savedProvince = locationService.createLocation(province, null);

        Location district = new Location();
        district.setLocationCode("DIST-" + UUID.randomUUID());
        district.setLocationName("Rubavu District");
        district.setLocationType(LocationType.DISTRICT);
        Location savedDistrict = locationService.createLocation(district, savedProvince.getLocationId());

        Location sector = new Location();
        sector.setLocationCode("SECT-" + UUID.randomUUID());
        sector.setLocationName("Gisenyi Sector");
        sector.setLocationType(LocationType.SECTOR);
        Location savedSector = locationService.createLocation(sector, savedDistrict.getLocationId());

        Location cell = new Location();
        cell.setLocationCode("CELL-" + UUID.randomUUID());
        cell.setLocationName("Amahoro Cell");
        cell.setLocationType(LocationType.CELL);
        Location savedCell = locationService.createLocation(cell, savedSector.getLocationId());

        Location village = new Location();
        village.setLocationCode("VILL-" + UUID.randomUUID());
        village.setLocationName("Amahoro Village");
        village.setLocationType(LocationType.VILLAGE);
        Location savedVillage = locationService.createLocation(village, savedCell.getLocationId());

        // Create user assigned to the village
        User user = new User();
        user.setPersonId(UUID.randomUUID().toString());
        user.setFirstName("Alice");
        user.setLastName("Uwase");
        user.setGender(Gender.FEMALE);
        user.setPhoneNumber("0780000001");
        user.setUserName("alice_" + UUID.randomUUID());
        user.setPassword("pass1234");
        user.setRole(RoleType.STUDENT);
        user.setLocation(savedVillage);
        User savedUser = userService.save(user);

        String provinceName = userService.getProvinceNameByPersonId(savedUser.getPersonId());

        assertEquals("Western Province", provinceName);
    }
}
