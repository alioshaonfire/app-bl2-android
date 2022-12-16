package com.buscalibre.app2.events;

import com.buscalibre.app2.models.Country;

public class CountrySelectedEvent {

    private final Country country;

    public CountrySelectedEvent(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }
}
