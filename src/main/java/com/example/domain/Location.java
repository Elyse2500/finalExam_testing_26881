package com.example.domain;

import com.example.domain.enums.LocationType;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "location")
public class Location {

    @Id
    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "location_code")
    private String locationCode;

    @Column(name = "location_name")
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type")
    private LocationType locationType;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Location parent;

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public Location getParent() { return parent; }
    public void setParent(Location parent) { this.parent = parent; }
}
